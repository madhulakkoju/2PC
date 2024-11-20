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

        //Wait until locks released if locked.
//        while( this.node.lockedDataItemsWithTransactionNum.containsKey(this.tnx.getSender())
//                || this.node.lockedDataItemsWithTransactionNum.containsKey(this.tnx.getReceiver())) {
//            //Thread.sleep(5);
//        }

        //Acquire the locks

        //Check if the transaction is valid

        //If valid, update the data items

        //If valid, Initiate Prepare phase

        //If f+1 Prepare responses received, Initiate Commit phase

        //If f+1 Commit responses received, Commit the transaction

        //Release the locks

    }


}
