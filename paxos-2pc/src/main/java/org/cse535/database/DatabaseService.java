package org.cse535.database;

import org.cse535.proto.Transaction;
import org.cse535.proto.TransactionInputConfig;
import org.cse535.threadimpls.IntraShardTnxProcessingThread;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.PriorityBlockingQueue;

public class DatabaseService {

    public Integer serverNumber;

    public PriorityBlockingQueue<TransactionInputConfig> incomingTransactionsQueue;


    public HashMap<Integer, Transaction> transactionMap;

    public HashSet<Integer> processedTransactionsSet;



    public DatabaseService( Integer serverNum) {
        this.serverNumber = serverNum;

        this.incomingTransactionsQueue = new PriorityBlockingQueue<>(100, new Comparator<TransactionInputConfig>() {
            @Override
            public int compare(TransactionInputConfig o1, TransactionInputConfig o2) {
                return o1.getTransaction().getTransactionNum() - o2.getTransaction().getTransactionNum();
            }
        });

        this.transactionMap = new HashMap<>();
        this.processedTransactionsSet = new HashSet<>();
    }
}
