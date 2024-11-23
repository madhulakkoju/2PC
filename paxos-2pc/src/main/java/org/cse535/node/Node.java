package org.cse535.node;

import org.cse535.configs.GlobalConfigs;
import org.cse535.configs.Utils;
import org.cse535.proto.*;
import org.cse535.threadimpls.IntraShardTnxProcessingThread;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class Node extends NodeServer {


    public Thread transactionWorkerThread;


    public boolean pauseTnxServiceUntilCommit;


    public ConcurrentHashMap<Integer, Integer> lockedDataItemsWithTransactionNum;

    public PriorityBlockingQueue<IntraShardTnxProcessingThread> intraTransactionThreadQueue;




    public Node(Integer serverNum, int port) {
        super(serverNum, port);

        this.transactionWorkerThread = new Thread(this::processTnxsInQueue);
        this.lockedDataItemsWithTransactionNum = new ConcurrentHashMap<>();



        try {
            this.server.start();
            this.transactionWorkerThread.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    public void processTnxsInQueue() {
        logger.log("Transaction Worker Thread Started");
        while (true) {
            try {
                Thread.sleep(5);
                if(pauseTnxServiceUntilCommit){
                    //logger.log("Pausing Transaction Service until Commit");
                    Thread.sleep(10);
                    continue;
                }


                if( !this.isServerActive.get()){
                    //logger.log("Pausing Transaction Service until Server is Active");
                    Thread.sleep(100);
                    continue;
                }

                // TransactionInputConfig transactionInput = this.database.incomingTransactionsQueue.take();

                TransactionInputConfig transactionInput = this.database.incomingTransactionsQueue.peek();
                if(transactionInput == null){
                    Thread.sleep(10);
                    continue;
                }





                Transaction transaction = transactionInput.getTransaction();

                if(this.database.processedTransactionsSet.contains(transaction.getTransactionNum())){
                    this.database.incomingTransactionsQueue.remove();
                    continue;
                }

                // Process the Transaction now.

                if(transaction.getIsCrossShard()){
                    processCrossShardTransaction(transaction, this.database.ballotNumber.incrementAndGet());
                }
                else {
                    processIntraShardTransaction(transaction, this.database.ballotNumber.incrementAndGet());
                }

                this.database.incomingTransactionsQueue.remove();

            } catch (InterruptedException e) {
                this.commandLogger.log("Line 143 ::: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }











    public boolean processIntraShardTransaction(Transaction transaction, int ballotNumber) {

        if(Utils.CheckTransactionBelongToMyCluster(transaction, this.serverNumber)){
            this.logger.log("Processing Transaction: " + transaction.getTransactionNum());

            try {

                IntraShardTnxProcessingThread intraShardTnxProcessingThread = new IntraShardTnxProcessingThread(this, transaction, ballotNumber);
                intraShardTnxProcessingThread.start();
                intraShardTnxProcessingThread.join();

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return true;
        }
        else {
            this.logger.log("Transaction does not belong to my cluster: " + transaction.getTransactionNum());
        }
        return false;
    }

    public boolean processCrossShardTransaction(Transaction transaction, int ballotNumber) {









        return false;
    }








    public void addSyncDataToPrepareResponse( PrepareResponse.Builder prepareResponse, PrepareRequest request){
        // add all Transactions & statuses after request's committed tnx
        for (int i = request.getLatestCommittedBallotNumber()+1; i <= this.database.ballotNumber.get() ; i++) {
            if(this.database.transactionMap.containsKey(i)){
                prepareResponse.putSyncTransactionsMap(i, this.database.transactionMap.get(i));

            }
            if(this.database.transactionStatusMap.containsKey(i)){
                prepareResponse.putSyncTransactionStatusMap(i, this.database.transactionStatusMap.get(i));
            }
        }

        // add total balances map after request's committed tnx
        //prepareResponse.putAllSyncBalancesMap(this.database.getAllBalances());
        prepareResponse.setLatestBallotNumber(this.database.ballotNumber.get());
        prepareResponse.setLastCommittedBallotNumber(this.database.lastCommittedBallotNumber);
        if(this.database.lastCommittedTransaction != null)
            prepareResponse.setLastCommittedTransaction(this.database.lastCommittedTransaction);

        prepareResponse.setNeedToSync(true);
    }

    public void syncData(PrepareResponse syncPrepareResponse) {
        this.logger.log("Data Post Sync -------------");


        this.database.ballotNumber.set( Math.max(syncPrepareResponse.getLatestBallotNumber() , this.database.ballotNumber.get()) );

        // Sync Transactions
        for (int i = syncPrepareResponse.getLastAcceptedUncommittedBallotNumber()+1; i <= syncPrepareResponse.getLatestBallotNumber() ; i++) {
            if(syncPrepareResponse.getSyncTransactionsMapMap().containsKey(i)){
                this.database.transactionMap.put(i, syncPrepareResponse.getSyncTransactionsMapMap().get(i));
                this.logger.log("Synced Transaction: " + i);
            }
            if(syncPrepareResponse.getSyncTransactionStatusMapMap().containsKey(i)){
                this.database.transactionStatusMap.put(i, syncPrepareResponse.getSyncTransactionStatusMapMap().get(i));
                this.logger.log("Synced Transaction Status: " + i);
            }
        }

        this.database.lastCommittedBallotNumber = syncPrepareResponse.getLastCommittedBallotNumber();
        this.database.lastCommittedTransaction = syncPrepareResponse.getLastCommittedTransaction();

        this.database.lastAcceptedUncommittedTransaction = syncPrepareResponse.getLastAcceptedUncommittedTransaction();
        this.database.lastAcceptedUncommittedBallotNumber = syncPrepareResponse.getLastAcceptedUncommittedBallotNumber();

        // RunUpdateBalancesWithTheseNewTransactions

        // Sync Balances
        //this.database.updateBalances(syncPrepareResponse.getSyncBalancesMapMap());
    }

    public PrepareResponse handlePreparePhase(PrepareRequest request) {
        PrepareResponse.Builder prepareResponse = PrepareResponse.newBuilder();
        prepareResponse.setProcessId(this.serverName);
        prepareResponse.setBallotNumber(request.getBallotNumber());
        prepareResponse.setSuccess(false);
        addSyncDataToPrepareResponse(prepareResponse, request);
        prepareResponse.setLastAcceptedUncommittedBallotNumber(this.database.lastAcceptedUncommittedBallotNumber);
        if(this.database.lastAcceptedUncommittedTransaction != null)
            prepareResponse.setLastAcceptedUncommittedTransaction(this.database.lastAcceptedUncommittedTransaction);

        this.logger.log("Prepare Request Received from " + request.getProcessId() );

        this.logger.log(request.toString());

        this.logger.log("-------------------");

        this.logger.log("DB -> Last Committed Ballot Number: " + this.database.lastCommittedBallotNumber);
        this.logger.log("Req -> getLatestCommittedBallotNumber" + request.getLatestCommittedBallotNumber());
        this.logger.log("Ahead check");

        this.logger.log("Last Accepted Uncommitted Ballot Number: " + this.database.lastAcceptedUncommittedBallotNumber);
        this.logger.log("Req Ballot Number " + request.getBallotNumber());
        this.logger.log("Success condition");

        this.logger.log("Last Accepted Uncommitted Transaction: " + Utils.toString(this.database.lastAcceptedUncommittedTransaction));
        this.logger.log("Last Committed Transaction: " + Utils.toString(this.database.lastCommittedTransaction));

        if(request.getLatestCommittedBallotNumber() < this.database.lastCommittedBallotNumber){

            this.logger.log("Prepare Request Rejected from " + request.getProcessId() + " Because Current Server is ahead of Leader " );
            return prepareResponse.build();
        }

        if(request.getBallotNumber() >= this.database.lastAcceptedUncommittedBallotNumber){
            this.database.lastAcceptedUncommittedBallotNumber = request.getBallotNumber();
            this.database.lastAcceptedUncommittedTransaction = request.getTransaction();
            this.database.ballotNumber.set( Math.max(request.getBallotNumber() , this.database.ballotNumber.get()) );

            this.database.transactionMap.put(request.getBallotNumber(), request.getTransaction());
            this.database.transactionStatusMap.put(request.getBallotNumber(), TransactionStatus.PREPARED);

            prepareResponse.setSuccess(true);
            prepareResponse.setNeedToSync(false);
        }

        this.logger.log("Prepare Request Accepted from " + request.getProcessId() );

        return prepareResponse.build();
    }

    public CommitResponse handleCommit(CommitRequest request) {
        CommitResponse.Builder commitResponse = CommitResponse.newBuilder();
        commitResponse.setProcessId(this.serverName);
        commitResponse.setBallotNumber(request.getBallotNumber());
        commitResponse.setSuccess(false);

        this.logger.log("Commit Request Received from " + request.getProcessId() );

        if(request.getBallotNumber() == this.database.lastAcceptedUncommittedBallotNumber){
            this.database.lastCommittedBallotNumber = request.getBallotNumber();
            this.database.lastCommittedTransaction = request.getTransaction();
            this.database.lastAcceptedUncommittedBallotNumber = -1;
            this.database.lastAcceptedUncommittedTransaction = null;
            commitResponse.setSuccess(true);
            this.database.transactionStatusMap.put(request.getBallotNumber(), TransactionStatus.COMMITTED);
            this.logger.log("Commit Request Accepted from " + request.getProcessId() );
        }

        return commitResponse.build();
    }



}



/*


        if((this.database.transactionMap.containsKey(request.getBallotNumber()) &&
                this.database.transactionMap.get(request.getBallotNumber()).getTransactionNum() != request.getTransaction().getTransactionNum() )) {
            // This server is ahead of Leader
            this.logger.log("Prepare Request Rejected from " + request.getProcessId() );
            return prepareResponse.build();
        }

        if(request.getLatestCommittedBallotNumber() == this.database.lastCommittedBallotNumber){
            //in sync -- perfect

            this.database.transactionMap.put(request.getBallotNumber(), request.getTransaction());
            this.database.lastAcceptedUncommittedBallotNumber = request.getBallotNumber();
            this.database.lastAcceptedUncommittedTransaction = request.getTransaction();

            this.logger.log("Prepare Request Accepted from " + request.getProcessId() );
            prepareResponse.setSuccess(true);
        }
        else if (request.getLatestCommittedBallotNumber() > this.database.lastCommittedBallotNumber){
            //Leader is ahead

            //Sync from Leader



            this.database.transactionMap.put(request.getBallotNumber(), request.getTransaction());
            this.database.lastAcceptedUncommittedBallotNumber = request.getBallotNumber();
            this.database.lastAcceptedUncommittedTransaction = request.getTransaction();

            this.logger.log("Prepare Request Accepted from " + request.getProcessId() );
            prepareResponse.setSuccess(true);
        }



 */