package org.cse535;

import org.cse535.configs.GlobalConfigs;
import org.cse535.node.Node;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {


    public static Node node;

    public static int serverNum;



    public static void main(String[] args) throws InterruptedException {

        serverNum = Integer.parseInt(args[0]);
        GlobalConfigs.TotalServers = Integer.parseInt(args[1]);
        GlobalConfigs.numServersPerCluster = Integer.parseInt(args[2]);


        GlobalConfigs.LoadConfigs( );

        node = new Node( serverNum, GlobalConfigs.ServerToPortMap.get(serverNum));
        node.server.awaitTermination();

    }
}