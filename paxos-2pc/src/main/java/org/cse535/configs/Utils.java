package org.cse535.configs;

import org.cse535.proto.CommitRequest;
import org.cse535.proto.PrepareRequest;
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

        int myCluster = FindMyCluster(serverNum);
        int senderCluster = FindClusterOfDataItem(transaction.getSender());
        int receiverCluster = FindClusterOfDataItem(transaction.getReceiver());

        if(myCluster == senderCluster && myCluster == receiverCluster){
            return true;
        }

        return false;
    }



























    public static String toString(Transaction transaction) {
        if(transaction == null) return "";
        return "Transaction ( " + transaction.getSender() + " -> " + transaction.getReceiver() + " = " + transaction.getAmount() + " ) ; ";
    }

    public static String toDataStoreString(Transaction transaction){
        if(transaction == null) return "";
        return "(" + transaction.getSender() + ", " + transaction.getReceiver() + ", " + transaction.getAmount() + ") ";
    }

    public static String toDataStoreString(PrepareRequest prepareRequest) {
        if(prepareRequest == null) return "";

        if(prepareRequest.getTransaction().getIsCrossShard()){
            return "[<"+ prepareRequest.getBallotNumber() + ","+ prepareRequest.getProcessId() +">, P, "+ toDataStoreString(prepareRequest.getTransaction()) +"]";
        }

        return "[<"+ prepareRequest.getBallotNumber() + ","+ prepareRequest.getProcessId() +">,"+ toDataStoreString(prepareRequest.getTransaction()) +"]";
    }

    public static String toDataStoreString(CommitRequest commitRequest) {
        if(commitRequest == null) return "";

        if(commitRequest.getTransaction().getIsCrossShard()){

            if(commitRequest.getAbort()){
                return "[<"+ commitRequest.getBallotNumber() + ","+ commitRequest.getProcessId() +">, A, "+ toDataStoreString(commitRequest.getTransaction()) +"]";
            }

            return "[<"+ commitRequest.getBallotNumber() + ","+ commitRequest.getProcessId() +">, C, "+ toDataStoreString(commitRequest.getTransaction()) +"]";
        }

        return "[<"+ commitRequest.getBallotNumber() + ","+ commitRequest.getProcessId() +">, "+ toDataStoreString(commitRequest.getTransaction()) +"]";
    }






}
