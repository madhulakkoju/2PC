package org.cse535.database;

import org.cse535.configs.GlobalConfigs;
import org.cse535.configs.Utils;
import org.cse535.proto.Transaction;
import org.cse535.proto.TransactionInputConfig;
import org.cse535.proto.TransactionStatus;
import org.cse535.threadimpls.IntraShardTnxProcessingThread;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

public class DatabaseService {

    public Integer serverNumber;

    public PriorityBlockingQueue<TransactionInputConfig> incomingTransactionsQueue;

    //BallotNumber, Transaction
    public HashMap<Integer, Transaction> transactionMap;
    public HashMap<Integer, TransactionStatus> transactionStatusMap;

    public HashSet<Integer> processedTransactionsSet;

    public Connection connection;
    public Statement statement;

    public Lock lock;

    public AtomicInteger ballotNumber;

    public int lastAcceptedUncommittedBallotNumber;
    public Transaction lastAcceptedUncommittedTransaction;

    public int lastCommittedBallotNumber;
    public Transaction lastCommittedTransaction;


    public DatabaseService( Integer serverNum) {
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
        this.transactionStatusMap = new HashMap<>();

        initializeSQLiteDatabase();
    }


    public void initializeSQLiteDatabase() {


        try {
            Class.forName("org.sqlite.JDBC");


            connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\mlakkoju\\2pc-madhulakkoju\\paxos-2pc\\Databases\\Database-"+this.serverNumber+".db");
            statement = connection.createStatement();

            statement.executeUpdate("DELETE FROM accounts;");

            String createTableSQL = "CREATE TABLE IF NOT EXISTS accounts (" +
                    "id INTEGER PRIMARY KEY, " +
                    "amount INTEGER NOT NULL" +
                    ");";

            statement.executeUpdate(createTableSQL);

            int cluster = Utils.FindMyCluster(this.serverNumber);
            for (int i = GlobalConfigs.clusterShardMap.get(cluster); i > GlobalConfigs.clusterShardMap.get(cluster - 1) ; i--) {
                String insertSQL = "INSERT INTO accounts (id, amount) VALUES (" + i + ", "+GlobalConfigs.InitialBalance+");";
                statement.executeUpdate(insertSQL);
            }


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

    public synchronized boolean executeTransaction(Transaction transaction) {

        int sender = transaction.getSender();
        int receiver = transaction.getReceiver();
        int amount = transaction.getAmount();

        int senderBalance = getBalance(sender);

        if(senderBalance < amount){
            return false;
        }

        int receiverBalance = getBalance(receiver);

        updateBalance(sender, senderBalance - amount);
        updateBalance(receiver, receiverBalance + amount);

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




}
