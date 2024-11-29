package org.cse535.database;

import org.cse535.configs.GlobalConfigs;
import org.cse535.configs.Utils;
import org.cse535.node.Node;
import org.cse535.node.NodeServer;
import org.cse535.proto.*;
import org.cse535.threadimpls.IntraShardTnxProcessingThread;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

public class DatabaseService {

    public Integer serverNumber;

    public PriorityBlockingQueue<TransactionInputConfig> incomingTransactionsQueue;



    public HashSet<Integer> processedTransactionsSet;

    public Connection connection;
    public Statement statement;

    public Lock lock;

    public AtomicInteger ballotNumber;

    public int lastAcceptedUncommittedBallotNumber;
    public Transaction lastAcceptedUncommittedTransaction;

    public int lastCommittedBallotNumber;
    public Transaction lastCommittedTransaction;




    public AtomicReference<ArrayList<String>> dataStore;



    public NodeServer node;


    public DatabaseService( Integer serverNum, NodeServer node) {
        this.ballotNumber = new AtomicInteger(0);
        this.lastAcceptedUncommittedBallotNumber = -1;
        this.lastAcceptedUncommittedTransaction = null;
        this.lastCommittedTransaction = null;
        this.lastCommittedBallotNumber = -1;

        this.serverNumber = serverNum;

        this.incomingTransactionsQueue = new PriorityBlockingQueue<>(100, new Comparator<TransactionInputConfig>() {
            @Override
            public int compare(TransactionInputConfig o1, TransactionInputConfig o2) {
                return o1.getTransaction().getTransactionNum() - o2.getTransaction().getTransactionNum();
            }
        });

        this.transactionMap = new HashMap<>();
        this.processedTransactionsSet = new HashSet<>();


        this.dataStore = new AtomicReference<>(new ArrayList<>());
        this.node = node;

        this.lockedDataItemsWithTransactionNum = new ConcurrentHashMap<>();
        this.writeAheadLog = new ConcurrentHashMap<>();

        this.transactionStatusMap = new HashMap<>();

        initializeSQLiteDatabase();
    }


    public void initializeSQLiteDatabase() {


        try {
            Class.forName("org.sqlite.JDBC");


            connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\mlakkoju\\2pc-madhulakkoju\\paxos-2pc\\Databases\\Database-"+this.serverNumber+".db");
            statement = connection.createStatement();

            //Account Table - to store balances

            statement.executeUpdate("DELETE FROM accounts;");

            String createTableSQL = "CREATE TABLE IF NOT EXISTS accounts (" +
                    "id INTEGER PRIMARY KEY, " +
                    "amount INTEGER NOT NULL" +
                    ");";

            statement.executeUpdate(createTableSQL);

            int cluster = Utils.FindMyCluster(this.serverNumber);

            for( Integer i :  Utils.GetAllDataItemsInCluster(cluster)){
                String insertSQL = "INSERT INTO accounts (id, amount) VALUES (" + i + ", "+GlobalConfigs.InitialBalance+");";
                statement.executeUpdate(insertSQL);
            }


            //Transactions Table - to store transactions and ballot numbers

            statement.executeUpdate("DELETE FROM transactions;");

            createTableSQL = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "ballot INTEGER PRIMARY KEY, " +
                    "transactionNum INTEGER NOT NULL, " +
                    "sender INTEGER NOT NULL, " +
                    "receiver INTEGER NOT NULL, " +
                    "amount INTEGER NOT NULL, " +
                    "isCrossShard BOOLEAN NOT NULL" +
                    ");";


            statement.executeUpdate(createTableSQL);


            //Transaction Status Table - to store status of transactions

            statement.executeUpdate("DELETE FROM transactionstatus;");

            createTableSQL = "CREATE TABLE IF NOT EXISTS transactionstatus (" +
                    "ballot INTEGER PRIMARY KEY, " +
                    "status INTEGER NOT NULL " +
                    ");";

            statement.executeUpdate(createTableSQL);




        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


    }


    public synchronized boolean isValidTransaction(Transaction transaction) {
        int sender = transaction.getSender();
        int amount = transaction.getAmount();
        int senderBalance = getBalance(sender);

        if(senderBalance < amount){
            return false;
        }
        return true;
    }

    public boolean executeTransaction(Transaction transaction) {

        this.node.walLogger.log( transaction.getTransactionNum() + " - BEGIN TNX : " + Utils.toDataStoreString(transaction));

        int sender = transaction.getSender();
        int receiver = transaction.getReceiver();
        int amount = transaction.getAmount();

        this.node.walLogger.log( transaction.getTransactionNum() + " - READ : " + sender );

        int senderBalance = getBalance(sender);

        if(senderBalance < amount){
            this.node.walLogger.log( transaction.getTransactionNum() + " - ABORTED : " + Utils.toDataStoreString(transaction));
            return false;
        }

        this.node.walLogger.log( transaction.getTransactionNum() + " - READ : " + receiver );

        int receiverBalance = getBalance(receiver);

        this.node.walLogger.log( transaction.getTransactionNum() + " - BEFORE TNX : " + Utils.toDataStoreString(transaction));

        if(Utils.FindClusterOfDataItem(receiver) == this.node.clusterNumber ) {
            updateBalance(sender, senderBalance - amount);
            this.node.walLogger.log(transaction.getTransactionNum() + " - WRITE : " + sender + " : " + (senderBalance - amount));
        }


        if(Utils.FindClusterOfDataItem(receiver) == this.node.clusterNumber ){
            updateBalance(receiver, receiverBalance + amount);
            this.node.walLogger.log( transaction.getTransactionNum() + " - WRITE : " + receiver + " : " + (receiverBalance + amount));
        }

        this.node.walLogger.log( transaction.getTransactionNum() + " - COMMIT : " + Utils.toDataStoreString(transaction));

        return true;
    }

    public synchronized void updateBalance(int account, int amount) {
        try {
            String updateSQL = "UPDATE accounts SET amount = " + amount + " WHERE id = " + account + ";";
            statement.executeUpdate(updateSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized int getBalance(int account) {
        try {
            String selectSQL = "SELECT amount FROM accounts WHERE id = " + account + ";";
            return statement.executeQuery(selectSQL).getInt("amount");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public synchronized HashMap<Integer, Integer> getAllBalances() {
        HashMap<Integer, Integer> balances = new HashMap<>();
        try {
            String selectSQL = "SELECT * FROM accounts;";
            while (statement.executeQuery(selectSQL).next()) {

                balances.put(
                        statement.executeQuery(selectSQL).getInt("id"),
                        statement.executeQuery(selectSQL).getInt("amount")
                );

            }
            return balances;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return balances;

    }

    public synchronized void updateBalances(Map<Integer, Integer> balances){
        try {
            for (Integer account : balances.keySet()) {
                String updateSQL = "UPDATE accounts SET amount = " + balances.get(account) + " WHERE id = " + account + ";";
                statement.executeUpdate(updateSQL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public synchronized void addToDataStore(PrepareRequest entry){
        this.dataStore.get().add(Utils.toDataStoreString(entry));
    }

    public synchronized void addToDataStore(CommitRequest entry){
        this.dataStore.get().add(Utils.toDataStoreString(entry));
    }





    public ConcurrentHashMap<Integer, Integer> lockedDataItemsWithTransactionNum;


    public synchronized boolean lockDataItem(int dataItem, int transactionNum) {
        this.node.walLogger.log(transactionNum+" - LOCK : " + dataItem + " : " + transactionNum);
        lockedDataItemsWithTransactionNum.put(dataItem, transactionNum);
        return true;
    }

    public synchronized boolean unlockDataItem(int dataItem, int transactionNum) {
        this.node.walLogger.log(transactionNum+" - UNLOCK : " + dataItem + " : " + transactionNum);
        lockedDataItemsWithTransactionNum.remove(dataItem);
//        if(lockedDataItemsWithTransactionNum.containsKey(dataItem) && lockedDataItemsWithTransactionNum.get(dataItem) == transactionNum){
//            return true;
//        }
        return false;
    }

    public boolean isDataItemLocked(int dataItem) {
        return lockedDataItemsWithTransactionNum.containsKey(dataItem);
    }

    public synchronized boolean isDataItemLockedWithTnx(int dataItem, int transactionNum) {
        return lockedDataItemsWithTransactionNum.containsKey(dataItem) && lockedDataItemsWithTransactionNum.get(dataItem) == transactionNum;
    }












    public static class WALEntry{
        public int transactionNum;
        public int sender;
        public int receiver;
        public int senderOldBalance;
        public int receiverOldBalance;

        public WALEntry(int transactionNum, int sender, int receiver, int senderOldBalance, int receiverOldBalance){
            this.transactionNum = transactionNum;
            this.sender = sender;
            this.receiver = receiver;
            this.senderOldBalance = senderOldBalance;
            this.receiverOldBalance = receiverOldBalance;

        }

    }


    public ConcurrentHashMap<Integer, WALEntry> writeAheadLog;

    public void writeToWAL(Transaction transaction) {
        int sender = transaction.getSender();
        int receiver = transaction.getReceiver();
        int amount = transaction.getAmount();

        int senderBalance = getBalance(sender);
        int receiverBalance = getBalance(receiver);

        if(Utils.FindClusterOfDataItem(sender) == this.node.clusterNumber ) {
            updateBalance(sender, senderBalance - amount);
            this.node.walLogger.log(transaction.getTransactionNum() + " - WRITE TO WAL: " + sender + " :  new :: " + (senderBalance - amount) + " : old :: " + senderBalance);
        }


        if(Utils.FindClusterOfDataItem(receiver) == this.node.clusterNumber ){
            updateBalance(receiver, receiverBalance + amount);
            this.node.walLogger.log( transaction.getTransactionNum() + " - WRITE TO WAL : " + receiver + " : " + (receiverBalance + amount));
        }


        writeAheadLog.put(transaction.getTransactionNum(), new WALEntry(transaction.getTransactionNum(), sender, receiver, senderBalance, receiverBalance));

    }

    public synchronized void rollbackWAL(int transactionNum) {

        WALEntry entry = writeAheadLog.get(transactionNum);

        if(entry == null){
            return;
        }

        int sender = entry.sender;
        int receiver = entry.receiver;
        int senderOldBalance = entry.senderOldBalance;
        int receiverOldBalance = entry.receiverOldBalance;

        if(sender != -1){
            updateBalance(sender, senderOldBalance);
            this.node.walLogger.log(transactionNum + " - ROLLBACK : " + sender + " : " + senderOldBalance);
        }

        if(receiver != -1){
            updateBalance(receiver, receiverOldBalance);
            this.node.walLogger.log(transactionNum + " - ROLLBACK : " + receiver + " : " + receiverOldBalance);
        }
    }

    public synchronized void commitbackWAL(int transactionNum) {
        writeAheadLog.remove(transactionNum);
    }












    //BallotNumber, Transaction
    public HashMap<Integer, Transaction> transactionMap;

    public synchronized void addTransaction( int ballotNumber, Transaction transaction ) {

        //Transaction tnx = getTransaction(ballotNumber);

        if( !this.transactionMap.containsKey(ballotNumber) ){

            try {
                String insertSQL = "INSERT INTO transactions (ballot, transactionNum, sender, receiver, amount, isCrossShard ) VALUES ("
                        + ballotNumber + ", " + transaction.getTransactionNum() + ", "
                        + transaction.getSender() + ", " + transaction.getReceiver() + ", "
                        + transaction.getAmount() + ", " + transaction.getIsCrossShard()
                        + ");";
                statement.executeUpdate(insertSQL);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }

        }
        else{
            try {
                String insertSQL = "UPDATE transactions SET" +
                        " transactionNum = " + transaction.getTransactionNum()
                        + ", sender = " + transaction.getSender()
                        + ", receiver = " + transaction.getReceiver()
                        + ", amount = " + transaction.getAmount()
                        + ", isCrossShard = " + transaction.getIsCrossShard()
                        + " WHERE ballot = " + ballotNumber + ";";

                statement.executeUpdate(insertSQL);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

        this.transactionMap.put(ballotNumber, transaction);
    }

    public Transaction getTransaction(int ballotNumber) {
        Transaction.Builder transactionBuilder = Transaction.newBuilder().setTransactionNum(-1);

        try {
            String selectSQL = "SELECT * FROM transactions WHERE ballot = " + ballotNumber + ";";

            ResultSet resultSet = statement.executeQuery(selectSQL);

            transactionBuilder.setTransactionNum(resultSet.getInt("transactionNum"));
            transactionBuilder.setSender(resultSet.getInt("sender"));
            transactionBuilder.setReceiver(resultSet.getInt("receiver"));
            transactionBuilder.setAmount(resultSet.getInt("amount"));
            transactionBuilder.setIsCrossShard(resultSet.getBoolean("isCrossShard"));

            return transactionBuilder.build();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactionBuilder.build();
    }






    //BallotNumber, TransactionStatus
    public HashMap<Integer, TransactionStatus> transactionStatusMap;

    public synchronized void addTransactionStatus( int ballotNumber, TransactionStatus status ) {

        transactionStatusMap.put(ballotNumber, status);
        return;

//        TransactionStatus transactionStatus = getTransactionStatus(ballotNumber);
//
//        if(transactionStatus != TransactionStatus.PENDING){
//
//            try {
//                String insertSQL = "UPDATE transactionstatus SET status =" + status.getNumber() + " WHERE ballot = " + ballotNumber + ";";
//                statement.executeUpdate(insertSQL);
//            }
//            catch (SQLException e) {
//                e.printStackTrace();
//            }
//
//        }
//        else{
//            try {
//                String insertSQL = "INSERT INTO transactionstatus (ballot, status ) VALUES ("
//                        + ballotNumber + ", " + status.getNumber()
//                        + ");";
//                statement.executeUpdate(insertSQL);
//            }
//            catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//


    }

    public TransactionStatus getTransactionStatus(int ballotNumber) {

        if(transactionStatusMap.containsKey(ballotNumber)){
            return transactionStatusMap.get(ballotNumber);
        }
        else{
            return TransactionStatus.PENDING;
        }
//
//        try {
//            String selectSQL = "SELECT * FROM transactionstatus WHERE ballot = " + ballotNumber + ";";
//
//            ResultSet resultSet = statement.executeQuery(selectSQL);
//
//            return TransactionStatus.forNumber(resultSet.getInt("status"));
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return TransactionStatus.PENDING;
    }

}
