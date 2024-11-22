package org.cse535.threadimpls;

import org.cse535.configs.GlobalConfigs;
import org.cse535.node.Node;
import org.cse535.proto.PrepareRequest;
import org.cse535.proto.PrepareResponse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IntraPrepareThread extends Thread{

    public ConcurrentHashMap<Integer, PrepareResponse> prepareResponses;
    public PrepareRequest request;

    public int targetServer;
    public AtomicInteger successPrepares;
    public Node node;


    public IntraPrepareThread(Node node, PrepareRequest request, ConcurrentHashMap<Integer, PrepareResponse> prepareResponses, int targetServer, AtomicInteger successPrepares) {
        this.request = request;
        this.node = node;
        this.prepareResponses = prepareResponses;
        this.targetServer = targetServer;
        this.successPrepares = successPrepares;
    }


    public void run() {

        PrepareResponse response = this.node.serversToPaxosStub.get(targetServer).prepare(this.request);

        this.prepareResponses.put(targetServer, response);

        if(response.getSuccess()){
            this.successPrepares.incrementAndGet();
        }

        this.node.logger.log("Prepare response from " + targetServer + " : " + response.getSuccess());


    }

}
