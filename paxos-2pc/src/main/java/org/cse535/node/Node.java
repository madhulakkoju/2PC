package org.cse535.node;

import org.cse535.configs.GlobalConfigs;
import org.cse535.configs.Utils;
import org.cse535.proto.Transaction;
import org.cse535.proto.TransactionInputConfig;
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
                    logger.log("Pausing Transaction Service until Commit");
                    Thread.sleep(10);
                    continue;
                }


                if( !this.isServerActive.get()){
                    logger.log("Pausing Transaction Service until Server is Active");
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
                    processCrossShardTransaction(transaction);
                }
                else {
                    processIntraShardTransaction(transaction);
                }

            } catch (InterruptedException e) {
                this.commandLogger.log("Line 143 ::: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }











    public boolean processIntraShardTransaction(Transaction transaction) {

        if(Utils.CheckTransactionBelongToMyCluster(transaction, this.serverNumber)){
            this.logger.log("Processing Transaction: " + transaction.getTransactionNum());

            IntraShardTnxProcessingThread intraShardTnxProcessingThread = new IntraShardTnxProcessingThread(this, transaction);




            intraShardTnxProcessingThread.start();

            return true;
        }
        else {
            this.logger.log("Transaction does not belong to my cluster: " + transaction.getTransactionNum());
        }
        return false;
    }


    public boolean processCrossShardTransaction(Transaction transaction) {









        return false;
    }


}
