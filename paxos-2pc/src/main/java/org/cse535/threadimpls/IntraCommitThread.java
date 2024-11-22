package org.cse535.threadimpls;

import org.cse535.node.Node;
import org.cse535.proto.CommitRequest;
import org.cse535.proto.CommitResponse;
import org.cse535.proto.PrepareRequest;
import org.cse535.proto.PrepareResponse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IntraCommitThread extends Thread{

    public AtomicInteger successCommits;
    public ConcurrentHashMap<Integer, CommitResponse> commitResponses;
    public CommitRequest request;

    public int targetServer;
    public Node node;

    public IntraCommitThread(Node node, CommitRequest request, ConcurrentHashMap<Integer, CommitResponse> commitResponses, int targetServer, AtomicInteger successCommits) {
        this.node = node;
        this.request = request;
        this.targetServer = targetServer;
        this.commitResponses = commitResponses;
        this.successCommits = successCommits;
    }

    public void run() {
        CommitResponse response = this.node.serversToPaxosStub.get(targetServer).commit(this.request);
        this.commitResponses.put(targetServer, response);

        if(response.getSuccess()){
            this.successCommits.incrementAndGet();
        }

        this.node.logger.log("Commit response from " + targetServer + " : " + response.getSuccess());

    }


}
