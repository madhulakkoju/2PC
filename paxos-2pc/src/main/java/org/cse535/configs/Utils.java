package org.cse535.configs;

import org.cse535.proto.Transaction;

public class Utils {

    public static int FindMyCluster(int serverNum){
        return (serverNum-1)/GlobalConfigs.numServersPerCluster + 1;
    }

    public static int FindMyCluster(String serverName){
        int serverNum = Integer.parseInt(serverName.substring(1));
        return FindMyCluster(serverNum);
    }

    public static boolean IsTransactionIntraCluster(Transaction transaction){
        return FindMyCluster(transaction.getSender()) == FindMyCluster(transaction.getReceiver());
    }

    public static boolean IsTransactionCrossCluster(Transaction transaction){
        return IsTransactionCrossCluster(transaction.getSender(), transaction.getReceiver());
    }

    public static boolean IsTransactionCrossCluster(int sender, int receiver){
        for(int i = 1; i <= GlobalConfigs.numClusters; i++){
            if( (sender <= GlobalConfigs.clusterShardMap.get(i) && sender > GlobalConfigs.clusterShardMap.get(i-1)) &&
                    (receiver <= GlobalConfigs.clusterShardMap.get(i) && receiver > GlobalConfigs.clusterShardMap.get(i-1)) ){
                return false;
            }
        }
        return true;
    }

    public static int FindClusterOfDataItem(int dataItem){
        for(int i = 1; i <= GlobalConfigs.numClusters; i++){
            if( dataItem <= GlobalConfigs.clusterShardMap.get(i) && dataItem > GlobalConfigs.clusterShardMap.get(i-1) ){
                return i;
            }
        }
        return -1;
    }


    public static boolean CheckTransactionBelongToMyCluster(Transaction transaction, int serverNum){
        boolean isIntraCluster = IsTransactionIntraCluster(transaction);
        if(isIntraCluster) {
            return FindClusterOfDataItem(transaction.getSender()) == FindMyCluster(serverNum);
        }
        return false;
    }



























    public static String toString(Transaction transaction) {
        if(transaction == null) return "";
        return "Transaction ( " + transaction.getSender() + " -> " + transaction.getReceiver() + " = " + transaction.getAmount() + " ) ; ";
    }
}
