package org.cse535.configs;

import org.cse535.proto.Transaction;

public class Utils {

    public static int FindMyCluster(int serverNum){
        return (serverNum-1)/GlobalConfigs.numServersPerCluster;
    }




























    public static String toString(Transaction transaction) {
        if(transaction == null) return "";
        return "Transaction ( " + transaction.getSender() + " -> " + transaction.getReceiver() + " = " + transaction.getAmount() + " ) ; ";
    }
}
