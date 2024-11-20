package org.cse535.threadimpls;

import org.cse535.node.Node;
import org.cse535.proto.Transaction;

public class IntraShardTnxProcessingThread extends Thread {

    public Transaction tnx;
    public Node node;

    public IntraShardTnxProcessingThread(Node node, Transaction tnx) {
        this.tnx = tnx;
        this.node = node;
    }



    public void run() {
        try {
        //Wait until locks released if locked.
        if( this.node.lockedDataItemsWithTransactionNum.containsKey(this.tnx.getSender())
                || this.node.lockedDataItemsWithTransactionNum.containsKey(this.tnx.getReceiver())) {

                Thread.sleep(10);
        }


            System.out.println("Processing transaction " + this.tnx.getTransactionNum() + " "
                    + this.tnx.getSender() + " -> "
                    + this.tnx.getReceiver() + " = "
                    + this.tnx.getAmount());

        //Acquire the locks
        this.node.lockedDataItemsWithTransactionNum.put(this.tnx.getSender(), this.tnx.getTransactionNum());
        this.node.lockedDataItemsWithTransactionNum.put(this.tnx.getReceiver(), this.tnx.getTransactionNum());

        //Check if the transaction is valid
        if(this.node.database.isValidTransaction(this.tnx)) {
            // Valid transaction

            // Initiate Prepare phase

            // If f+1 Prepare responses received, Initiate Commit phase


            // Commit the transaction
            this.node.database.executeTransaction(this.tnx);

            // If f+1 Commit responses received, Commit the transaction



            //Release the locks
            this.node.lockedDataItemsWithTransactionNum.remove(this.tnx.getSender());
            this.node.lockedDataItemsWithTransactionNum.remove(this.tnx.getReceiver());

            //Send reply to Client
            System.out.println("Transaction processed successfully");

        }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            this.node.lockedDataItemsWithTransactionNum.remove(this.tnx.getSender());
            this.node.lockedDataItemsWithTransactionNum.remove(this.tnx.getReceiver());
        }

    }


}
