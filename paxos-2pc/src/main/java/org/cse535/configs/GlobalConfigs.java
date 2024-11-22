package org.cse535.configs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GlobalConfigs {

    public static final HashMap<Integer, Integer> ServerToPortMap = new HashMap<Integer, Integer>(); // server number to port map

    public static final Integer basePort = 8000;

    public static final Integer ViewServerPort = 8000;
    public static final String ViewServerName = "vs";

    public static Integer TotalServers;

    public static int numServersPerCluster;
    public static int numClusters;

    public static int TotalDataItems;

    public static int InitialBalance = 10;


    public static int f = 1;
    public static int ShardConsesusThreshold = f + 1;


    // Cluster Number: Max Data Item Number
    public static HashMap<Integer, Integer> clusterShardMap;

    public static HashMap<Integer, List<Integer>> clusterToServersMap;

    public static void LoadConfigs( ){

        numClusters = GlobalConfigs.TotalServers / GlobalConfigs.numServersPerCluster;
        clusterShardMap = new HashMap<Integer, Integer>();
        clusterToServersMap = new HashMap<Integer, List<Integer>>();
        clusterShardMap.put(0, 0);


        ServerToPortMap.put(0, ViewServerPort);

        for (int i = 1; i <= TotalServers ; i++) {
            ServerToPortMap.put(i, basePort + i);
        }

        int dataItemsPerCluster = TotalDataItems / numClusters;

        int starter = 0;
        int counter = 1;

        for (int i = 1; i <= numClusters; i++) {
            starter += dataItemsPerCluster;
            clusterShardMap.put(i, starter);
            clusterToServersMap.put(i, new ArrayList<Integer>());

            for(int j = 0; j < numServersPerCluster; j++){
                clusterToServersMap.get(i).add(counter++);
            }
            System.out.println("Cluster " + i + " has servers: " + clusterToServersMap.get(i).toString());

        }
        clusterShardMap.put(numClusters, TotalDataItems);






    }






}
