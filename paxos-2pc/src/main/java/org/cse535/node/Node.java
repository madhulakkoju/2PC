package org.cse535.node;

import org.cse535.configs.GlobalConfigs;
import org.cse535.configs.Utils;
import org.cse535.proto.*;
import org.cse535.threadimpls.IntraPrepareThread;
import org.cse535.threadimpls.IntraShardTnxProcessingThread;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Node extends NodeServer {


    public Thread transactionWorkerThread;


    public boolean pauseTnxServiceUntilCommit;
    public boolean pauseTnxServiceDueToCrossShard;


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
                if(pauseTnxServiceDueToCrossShard){
                    //logger.log("Pausing Transaction Service until Cross Shard Commit");
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
                    processCrossShardTransaction(transactionInput);
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




    public CrossTxnResponse processCrossShardTransaction(TransactionInputConfig transactionInputConfig) {

        this.pauseTnxServiceDueToCrossShard = true;

        this.logger.log("Processing Cross Shard Transaction: " + transactionInputConfig.getTransaction().getTransactionNum());
        //Sync data from other servers in the cluster.
        // TODO: Sync Data with sync request

        this.logger.log("Syncing Data with other servers in the cluster");


        this.logger.log("Data Sync Done");

        CrossTxnResponse.Builder crossTxnResponse = CrossTxnResponse.newBuilder();
        crossTxnResponse.setClusterId(this.clusterNumber);
        crossTxnResponse.setSuccess(false);
        crossTxnResponse.setServerName(this.serverName);
        crossTxnResponse.setSuccessPreparesCount(0);
        crossTxnResponse.setFailureReason("");
        crossTxnResponse.setBallotNumber(-1);

        int sender = transactionInputConfig.getTransaction().getSender();
        int receiver = transactionInputConfig.getTransaction().getReceiver();
        int amount = transactionInputConfig.getTransaction().getAmount();

        if(this.lockedDataItemsWithTransactionNum.containsKey(sender) || this.lockedDataItemsWithTransactionNum.containsKey(receiver)){
            crossTxnResponse.setFailureReason("Data Item Locked");
            this.logger.log("Data Item Locked.... so rejecting the transaction");
            return crossTxnResponse.build();
        }



        boolean belongsToCluster = false;

        int itemToLock = sender;

        if(Utils.FindClusterOfDataItem(sender) == this.clusterNumber){
            this.logger.log("Processing Cross Shard Transaction - Sender : " + transactionInputConfig.getTransaction().getTransactionNum());
            belongsToCluster = true;

            if(!this.database.isValidTransaction(transactionInputConfig.getTransaction())){
                crossTxnResponse.setFailureReason("Insufficient Balance");
                return crossTxnResponse.build();
            }

        }
        else if( Utils.FindClusterOfDataItem(receiver) == this.clusterNumber ){
            this.logger.log("Processing Cross Shard Transaction - Receiver : " + transactionInputConfig.getTransaction().getTransactionNum());
            belongsToCluster = true;
            itemToLock = receiver;

        }
        else {
            this.logger.log("Transaction does not belong to my cluster: " + transactionInputConfig.getTransaction().getTransactionNum());

            return crossTxnResponse.build();
        }




        // Lock the data item

        this.lockedDataItemsWithTransactionNum.put(itemToLock, transactionInputConfig.getTransaction().getTransactionNum());
        this.logger.log("Locked Data Item: " + itemToLock);

        // Prepare Request to cluster servers and return Response

        int ballotNumber = this.database.ballotNumber.incrementAndGet();
        crossTxnResponse.setBallotNumber(ballotNumber);

        this.logger.log("Ballot Number: " + ballotNumber);

        IntraPrepareThread[] intraPrepareThreads = new IntraPrepareThread[GlobalConfigs.numServersPerCluster];

        ConcurrentHashMap<Integer, PrepareResponse> prepareResponses = new ConcurrentHashMap<>();
        AtomicInteger successPrepares = new AtomicInteger(1);

        PrepareRequest.Builder prepareBuilder = PrepareRequest.newBuilder();

        prepareBuilder.setBallotNumber(ballotNumber)
                .setProcessId(this.serverName)
                .setTransaction(transactionInputConfig.getTransaction());

        if(this.database.lastCommittedTransaction != null) {
            prepareBuilder.setLatestCommittedTransaction(this.database.lastCommittedTransaction);
        }

        prepareBuilder.setLatestCommittedBallotNumber( this.database.lastCommittedBallotNumber)
                .setClusterId(this.clusterNumber);


        PrepareRequest prepareRequest = prepareBuilder.build();
        this.logger.log("Prepare Request: " + prepareRequest.toString());
        try {

            for (int i = 0; i < GlobalConfigs.numServersPerCluster; i++) {
                if (Objects.equals(GlobalConfigs.clusterToServersMap.get(this.clusterNumber).get(i), this.serverNumber)) {
                    continue;
                }
                intraPrepareThreads[i] = new IntraPrepareThread(this, prepareRequest, prepareResponses, GlobalConfigs.clusterToServersMap.get(this.clusterNumber).get(i), successPrepares);
                intraPrepareThreads[i].start();
            }

            this.database.transactionStatusMap.put(ballotNumber, TransactionStatus.PREPARED);

            for (int i = 0; i < GlobalConfigs.numServersPerCluster; i++) {
                if (intraPrepareThreads[i] == null) continue;
                intraPrepareThreads[i].join();
            }

        } catch (Exception e) {
            this.logger.log("Error " + e.getMessage());
        }


        if( successPrepares.get() < GlobalConfigs.ShardConsesusThreshold) {
            this.logger.log("Consensus FAILED for Cross Shard Prepare Phase");
            crossTxnResponse.setSuccessPreparesCount(successPrepares.get());
            crossTxnResponse.setFailureReason("Consensus not reached");
            return crossTxnResponse.build();
        }
        else{
            this.logger.log("Consensus Reached for Cross Shard Prepare Phase");
            crossTxnResponse.setSuccess(true);
            crossTxnResponse.setSuccessPreparesCount(successPrepares.get());

        }
        return crossTxnResponse.build();
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


            if(request.getTransaction().getIsCrossShard())
                this.database.addToDataStore(request);
        }

        this.logger.log("Prepare Request Accepted from " + request.getProcessId() );



        return prepareResponse.build();
    }

    public CommitResponse handleCommit(CommitRequest request) {
        CommitResponse.Builder commitResponse = CommitResponse.newBuilder();
        commitResponse.setProcessId(this.serverName);
        commitResponse.setBallotNumber(request.getBallotNumber());
        commitResponse.setSuccess(false);

        if(request.getAbort()){

            this.logger.log("Abort Request Received from " + request.getProcessId() );
            this.database.transactionStatusMap.put(request.getBallotNumber(), TransactionStatus.ABORTED);

            if(this.lockedDataItemsWithTransactionNum.containsKey(request.getTransaction().getSender()) &&
                    this.lockedDataItemsWithTransactionNum.get(request.getTransaction().getSender()) == request.getTransaction().getTransactionNum()){
                this.lockedDataItemsWithTransactionNum.remove(request.getTransaction().getSender());
            }

            if(this.lockedDataItemsWithTransactionNum.containsKey(request.getTransaction().getReceiver()) &&
                    this.lockedDataItemsWithTransactionNum.get(request.getTransaction().getReceiver()) == request.getTransaction().getTransactionNum()){
                this.lockedDataItemsWithTransactionNum.remove(request.getTransaction().getReceiver());
            }

            commitResponse.setSuccess(true);
            this.database.addToDataStore(request);
        }
        else {

            this.logger.log("Commit Request Received from " + request.getProcessId());

            if (request.getBallotNumber() == this.database.lastAcceptedUncommittedBallotNumber) {
                this.database.lastCommittedBallotNumber = request.getBallotNumber();
                this.database.lastCommittedTransaction = request.getTransaction();
                this.database.lastAcceptedUncommittedBallotNumber = -1;
                this.database.lastAcceptedUncommittedTransaction = null;
                commitResponse.setSuccess(true);
                this.database.transactionStatusMap.put(request.getBallotNumber(), TransactionStatus.COMMITTED);
                this.logger.log("Commit Request Accepted from " + request.getProcessId());
                this.database.addToDataStore(request);
            }

        }

        //Finally remove locks

        if(this.lockedDataItemsWithTransactionNum.containsKey(request.getTransaction().getSender()) &&
                this.lockedDataItemsWithTransactionNum.get(request.getTransaction().getSender()) == request.getTransaction().getTransactionNum()){
            this.lockedDataItemsWithTransactionNum.remove(request.getTransaction().getSender());
        }

        if(this.lockedDataItemsWithTransactionNum.containsKey(request.getTransaction().getReceiver()) &&
                this.lockedDataItemsWithTransactionNum.get(request.getTransaction().getReceiver()) == request.getTransaction().getTransactionNum()){
            this.lockedDataItemsWithTransactionNum.remove(request.getTransaction().getReceiver());
        }

        return commitResponse.build();
    }



    public String PrintDataStore(){
        return this.serverName + " : " + String.join("->", this.database.dataStore.get());
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