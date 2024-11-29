package org.cse535.threadimpls;

import org.cse535.node.ViewServer;
import org.cse535.proto.CommandOutput;
import org.cse535.proto.ReShardingData;

public class ReShardThread extends Thread {

    public int server;
    public ReShardingData data;
    public ViewServer vs;


    public ReShardThread(ViewServer vs, int server, ReShardingData data) {
        this.server = server;
        this.data = data;
        this.vs = vs;
    }


    public void run() {

        CommandOutput output = this.vs.serversToPaxosStub.get(server).reShardingProcess(data);

        System.out.println("ReSharding Process Completed for Server: " + server + " with output: " + output.getOutput());


    }
}
