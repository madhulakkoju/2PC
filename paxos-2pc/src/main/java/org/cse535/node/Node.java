package org.cse535.node;

import org.cse535.configs.GlobalConfigs;
import org.cse535.configs.Utils;
import org.cse535.proto.Transaction;
import org.cse535.proto.TransactionInputConfig;

public class Node extends NodeServer {


    public Thread transactionWorkerThread;


    public boolean pauseTnxServiceUntilCommit;







    public Node(Integer serverNum, int port) {
        super(serverNum, port);



        this.transactionWorkerThread = new Thread(this::processTnxsInQueue);


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

                if( ! transactionInput.getTransaction().getSender().equals(this.serverName) ){
                    this.logger.log("Transaction not for this client / server");
                    this.database.incomingTransactionsQueue.remove();
                    continue;
                }



                Transaction transaction = transactionInput.getTransaction();

                if(this.database.processedTransactionsSet.contains(transaction.getTransactionNum())){
                    this.database.incomingTransactionsQueue.remove();
                    continue;
                }

                // Process the Transaction now.



            } catch (InterruptedException e) {
                this.commandLogger.log("Line 143 ::: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }





}
