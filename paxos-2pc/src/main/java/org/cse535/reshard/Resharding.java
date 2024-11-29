package org.cse535.reshard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cse535.configs.GlobalConfigs;
import org.cse535.loggers.LogUtils;
import org.cse535.proto.Transaction;

import java.util.*;

public class Resharding {

    private static final Logger log = LogManager.getLogger(Resharding.class);

    public static HashMap<Integer, Integer> reshardUsingHotKeyAndRoundRobin(List<Transaction> transactionHistory, HashMap<Integer, Integer> config, int numShards, LogUtils logger) {
        if (transactionHistory == null || transactionHistory.isEmpty()) {
            System.out.println("Transaction history is empty. Resharding not performed.");
            return;
        }

        if (numShards <= 0) {
            throw new IllegalArgumentException("Number of shards must be greater than zero.");
        }

        Map<Integer, Integer> frequencyMap = new HashMap<>();

        Map<ItemPair, Integer> coAccessFrequency = new HashMap<>();

        for (Transaction tx : transactionHistory) {

            frequencyMap.put(tx.getSender(), frequencyMap.getOrDefault(tx.getSender(), 0) + 1);
            frequencyMap.put(tx.getReceiver(), frequencyMap.getOrDefault(tx.getReceiver(), 0) + 1);

            ItemPair pair = new ItemPair(tx.getSender(), tx.getReceiver());
            coAccessFrequency.put(pair, coAccessFrequency.getOrDefault(pair, 0) + 1);
        }

        PriorityQueue<Map.Entry<Integer, Integer>> hotKeyHeap = new PriorityQueue<>(
                (a, b) -> b.getValue() - a.getValue()
        );
        hotKeyHeap.addAll(frequencyMap.entrySet());

        // Prepare new shard assignments
        List<List<Integer>> shards = new ArrayList<>();
        for (int i = 0; i < numShards; i++) {
            shards.add(new ArrayList<>());
        }

        Set<Integer> assignedItems = new HashSet<>(); // Track assigned items

        while (!hotKeyHeap.isEmpty()) {
            Map.Entry<Integer, Integer> hotKey = hotKeyHeap.poll();
            int hotItem = hotKey.getKey();

            if (assignedItems.contains(hotItem)) continue;

            int shardId = getLeastFilledShard(shards);
            shards.get(shardId).add(hotItem);
            assignedItems.add(hotItem);

            for (Map.Entry<ItemPair, Integer> entry : coAccessFrequency.entrySet()) {
                ItemPair pair = entry.getKey();
                if (pair.item1 == hotItem && !assignedItems.contains(pair.item2)) {
                    shards.get(shardId).add(pair.item2);
                    assignedItems.add(pair.item2);
                } else if (pair.item2 == hotItem && !assignedItems.contains(pair.item1)) {
                    shards.get(shardId).add(pair.item1);
                    assignedItems.add(pair.item1);
                }
            }
        }

        // balance shards with all items
        for (int item = 1; item <= 3000; item++) {
            if (!assignedItems.contains(item)) {
                int shardId = getLeastFilledShard(shards);
                shards.get(shardId).add(item);
            }
        }



        HashMap<Integer, Integer> newConfig = new HashMap<>(config.size());

        for (int shardId = 0; shardId < numShards; shardId++) {
            for (int item : shards.get(shardId)) {
                newConfig.put(item, shardId+1);
            }
        }

        logger.log("Resharding completed. Updated config: ");
        System.out.println("Resharding completed. Updated config: ");

        for(Map.Entry<Integer, Integer> a : newConfig.entrySet()){
            logger.log(a.getKey() + " C"+ a.getValue());
        }

        return newConfig;
    }

    public static int getLeastFilledShard(List<List<Integer>> shards) {
        int leastFilledShard = 0;
        int minSize = Integer.MAX_VALUE;

        for (int i = 0; i < shards.size(); i++) {
            int size = shards.get(i).size();
            if (size < minSize) {
                minSize = size;
                leastFilledShard = i;
            }
        }

        return leastFilledShard;
    }


    public static HashMap<Integer, Integer> reshardWithSwapHotKey(List<Transaction> transactionHistory, HashMap<Integer, Integer> beginConfig, int numShards, LogUtils logger) {

        if (transactionHistory == null || transactionHistory.isEmpty()) {
            System.out.println("Transaction history is empty. Resharding not performed.");
            return beginConfig;
        }

        if (numShards <= 0) {
            System.out.println("Number of shards must be greater than zero.");
            return beginConfig;
        }

        HashMap<Integer, Integer> config = new HashMap<>(beginConfig);

        Map<Integer, Integer> frequencyMap = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> clusterFrequencyMap = new HashMap<>();

        // frequency maps
        for (Transaction tx : transactionHistory) {
            int sender = tx.getSender();
            int receiver = tx.getReceiver();


            frequencyMap.put(sender, frequencyMap.getOrDefault(sender, 0) + 1);
            frequencyMap.put(receiver, frequencyMap.getOrDefault(receiver, 0) + 1);


            int senderCluster = config.get(sender);
            int receiverCluster = config.get(receiver);

            clusterFrequencyMap.putIfAbsent(senderCluster, new HashMap<>());
            clusterFrequencyMap.putIfAbsent(receiverCluster, new HashMap<>());

            clusterFrequencyMap.get(senderCluster).put(sender, clusterFrequencyMap.get(senderCluster).getOrDefault(sender, 0) + 1);
            clusterFrequencyMap.get(receiverCluster).put(receiver, clusterFrequencyMap.get(receiverCluster).getOrDefault(receiver, 0) + 1);
        }

        // finding least used items in each cluster and swap with frequently accessed items from other clusters
        for (Map.Entry<Integer, Map<Integer, Integer>> clusterEntry : clusterFrequencyMap.entrySet()) {
            int clusterId = clusterEntry.getKey();
            Map<Integer, Integer> clusterData = clusterEntry.getValue();

            int leastUsedItem = getLeastUsedItem(clusterData);

            int mostFrequentItem = findMostFrequentItemNotInCluster(clusterId, frequencyMap, clusterFrequencyMap);

            if (mostFrequentItem != -1) {
                swapItems(clusterId, leastUsedItem, mostFrequentItem, config, clusterData, logger);
            }
        }


        System.out.println("Resharding completed. Updated config: " );

        config.entrySet().forEach(entry -> {
            logger.log(entry.getKey() + " SWAP-C" + entry.getValue());
        });


    }

    private static int getLeastUsedItem(Map<Integer, Integer> clusterData) {
        return clusterData.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("Cluster is empty"));
    }

    private static int findMostFrequentItemNotInCluster(int currentCluster, Map<Integer, Integer> frequencyMap,
                                                        Map<Integer, Map<Integer, Integer>> clusterFrequencyMap) {
        return frequencyMap.entrySet().stream()
                .filter(entry -> clusterFrequencyMap.get(currentCluster).get(entry.getKey()) == null)  // Not in current cluster
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1); // Return -1 if no item is found
    }


    private static void swapItems(int currentCluster, int leastUsedItem, int mostFrequentItem,
                                  HashMap<Integer, Integer> config, Map<Integer, Integer> clusterData, LogUtils logger) {

        clusterData.remove(leastUsedItem);
        clusterData.put(mostFrequentItem, clusterData.getOrDefault(mostFrequentItem, 0) + 1);
        config.put(mostFrequentItem, currentCluster);
        config.put(leastUsedItem, getLeastFilledCluster(config));
        logger.log("Swapped " + leastUsedItem + " with " + mostFrequentItem);
    }


    private static int getLeastFilledCluster(HashMap<Integer, Integer> config) {
        Map<Integer, Long> clusterCount = new HashMap<>();
        for (int shardId : config.values()) {
            clusterCount.put(shardId, clusterCount.getOrDefault(shardId, 0L) + 1);
        }

        return clusterCount.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("No clusters available"));
    }












    public static void main(String[] args){
        int viewServerNum = 0;
        GlobalConfigs.TotalServers = 9;
        GlobalConfigs.numServersPerCluster = 3;
        GlobalConfigs.TotalDataItems = 3000;

        GlobalConfigs.LoadConfigs();

        LogUtils logger = new LogUtils("ReSharding-test",viewServerNum);



        HashMap<Integer, Transaction> transactionMap = new HashMap<>();

        // Adding some transactions to test resharding
        transactionMap.put(51, Transaction.newBuilder().setSender(100).setReceiver(501).setAmount(8).build());
        transactionMap.put(52, Transaction.newBuilder().setSender(1001).setReceiver(1650).setAmount(2).build());
        transactionMap.put(53, Transaction.newBuilder().setSender(2800).setReceiver(2150).setAmount(7).build());
        transactionMap.put(54, Transaction.newBuilder().setSender(1201).setReceiver(1111).setAmount(5).build());
        transactionMap.put(55, Transaction.newBuilder().setSender(501).setReceiver(299).setAmount(13).build());
        transactionMap.put(56, Transaction.newBuilder().setSender(2596).setReceiver(2297).setAmount(3).build());
        transactionMap.put(57, Transaction.newBuilder().setSender(796).setReceiver(1997).setAmount(9).build());
        transactionMap.put(58, Transaction.newBuilder().setSender(2196).setReceiver(2397).setAmount(3).build());
        transactionMap.put(59, Transaction.newBuilder().setSender(796).setReceiver(1997).setAmount(7).build());
        transactionMap.put(510, Transaction.newBuilder().setSender(1998).setReceiver(2998).setAmount(19).build());
        transactionMap.put(1, Transaction.newBuilder().setSender(100).setReceiver(501).setAmount(8).build());
        transactionMap.put(2, Transaction.newBuilder().setSender(1001).setReceiver(1650).setAmount(2).build());
        transactionMap.put(3, Transaction.newBuilder().setSender(2800).setReceiver(2150).setAmount(7).build());
        transactionMap.put(4, Transaction.newBuilder().setSender(1201).setReceiver(1111).setAmount(5).build());
        transactionMap.put(5, Transaction.newBuilder().setSender(501).setReceiver(299).setAmount(13).build());
        transactionMap.put(6, Transaction.newBuilder().setSender(2596).setReceiver(2297).setAmount(3).build());
        transactionMap.put(7, Transaction.newBuilder().setSender(796).setReceiver(1997).setAmount(9).build());
        transactionMap.put(8, Transaction.newBuilder().setSender(2196).setReceiver(2397).setAmount(3).build());
        transactionMap.put(9, Transaction.newBuilder().setSender(796).setReceiver(1997).setAmount(7).build());
        transactionMap.put(10, Transaction.newBuilder().setSender(1998).setReceiver(2998).setAmount(19).build());
        transactionMap.put(11, Transaction.newBuilder().setSender(100).setReceiver(501).setAmount(8).build());
        transactionMap.put(12, Transaction.newBuilder().setSender(1001).setReceiver(1650).setAmount(2).build());
        transactionMap.put(13, Transaction.newBuilder().setSender(2800).setReceiver(2150).setAmount(7).build());
        transactionMap.put(14, Transaction.newBuilder().setSender(1201).setReceiver(1111).setAmount(5).build());
        transactionMap.put(15, Transaction.newBuilder().setSender(501).setReceiver(299).setAmount(13).build());
        transactionMap.put(16, Transaction.newBuilder().setSender(2596).setReceiver(2297).setAmount(3).build());
        transactionMap.put(17, Transaction.newBuilder().setSender(796).setReceiver(1997).setAmount(9).build());
        transactionMap.put(18, Transaction.newBuilder().setSender(2196).setReceiver(2397).setAmount(3).build());
        transactionMap.put(19, Transaction.newBuilder().setSender(796).setReceiver(1997).setAmount(7).build());
        transactionMap.put(20, Transaction.newBuilder().setSender(1998).setReceiver(2998).setAmount(19).build());
        transactionMap.put(21, Transaction.newBuilder().setSender(100).setReceiver(501).setAmount(8).build());
        transactionMap.put(22, Transaction.newBuilder().setSender(1001).setReceiver(1650).setAmount(2).build());
        transactionMap.put(23, Transaction.newBuilder().setSender(2800).setReceiver(2150).setAmount(7).build());
        transactionMap.put(24, Transaction.newBuilder().setSender(1201).setReceiver(1111).setAmount(5).build());
        transactionMap.put(25, Transaction.newBuilder().setSender(501).setReceiver(299).setAmount(13).build());
        transactionMap.put(26, Transaction.newBuilder().setSender(2596).setReceiver(2297).setAmount(3).build());
        transactionMap.put(27, Transaction.newBuilder().setSender(796).setReceiver(1997).setAmount(9).build());
        transactionMap.put(28, Transaction.newBuilder().setSender(2196).setReceiver(2397).setAmount(3).build());
        transactionMap.put(29, Transaction.newBuilder().setSender(796).setReceiver(1997).setAmount(7).build());
        transactionMap.put(30, Transaction.newBuilder().setSender(1998).setReceiver(2998).setAmount(19).build());
        transactionMap.put(31, Transaction.newBuilder().setSender(100).setReceiver(501).setAmount(8).build());
        transactionMap.put(32, Transaction.newBuilder().setSender(1001).setReceiver(1650).setAmount(2).build());
        transactionMap.put(33, Transaction.newBuilder().setSender(2800).setReceiver(2150).setAmount(7).build());
        transactionMap.put(34, Transaction.newBuilder().setSender(1201).setReceiver(1111).setAmount(5).build());
        transactionMap.put(35, Transaction.newBuilder().setSender(501).setReceiver(299).setAmount(13).build());
        transactionMap.put(36, Transaction.newBuilder().setSender(2596).setReceiver(2297).setAmount(3).build());
        transactionMap.put(37, Transaction.newBuilder().setSender(796).setReceiver(1997).setAmount(9).build());
        transactionMap.put(38, Transaction.newBuilder().setSender(2196).setReceiver(2397).setAmount(3).build());
        transactionMap.put(39, Transaction.newBuilder().setSender(796).setReceiver(1997).setAmount(7).build());
        transactionMap.put(40, Transaction.newBuilder().setSender(1998).setReceiver(2998).setAmount(19).build());



        List<Transaction> transactionList = new ArrayList<>(transactionMap.values());


        logger.log("Resharding using HotKey and RoundRobin");

        reshardUsingHotKeyAndRoundRobin(transactionList, GlobalConfigs.DataItemToClusterMap, GlobalConfigs.numClusters, logger);


        logger.log("----------------------------------------------------------------------");
        logger.log("----------------------------------------------------------------------");
        logger.log("----------------------------------------------------------------------");
        logger.log("----------------------------------------------------------------------");
        logger.log("----------------------------------------------------------------------");
        logger.log(" Resharding based on minimal Swapping and Hotkeys");

        reshardWithSwapHotKey(transactionList, GlobalConfigs.DataItemToClusterMap, GlobalConfigs.numClusters, logger);



    }

}
