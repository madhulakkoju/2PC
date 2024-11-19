package org.cse535.configs;

import java.util.HashMap;

public class GlobalConfigs {

    public static final HashMap<Integer, Integer> ServerToPortMap = new HashMap<Integer, Integer>(); // server number to port map

    public static final Integer basePort = 8000;

    public static final Integer ViewServerPort = 8000;
    public static final String ViewServerName = "vs";

    public static Integer TotalServers;

    public static int numServersPerCluster;
    public static int numClusters;




    public static void LoadConfigs( ){

        numClusters = GlobalConfigs.TotalServers / GlobalConfigs.numServersPerCluster;

        for (int i = 1; i <= TotalServers ; i++) {
            ServerToPortMap.put(i, basePort + i);
        }

    }


}
