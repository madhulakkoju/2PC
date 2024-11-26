package org.cse535.threadimpls;

import org.cse535.configs.GlobalConfigs;
import org.cse535.node.ViewServer;
import org.cse535.proto.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CrossShardTnxProcessingThread extends Thread {

    public ViewServer viewServer;
    public TransactionInputConfig transactionInputConfig;
    public String senderServerId;
    public String receiverServerId;

    public CrossShardTnxProcessingThread(ViewServer viewServer, TransactionInputConfig transactionInputConfig,
                                         String senderServerId, String receiverServerId) {
        this.viewServer = viewServer;
        this.transactionInputConfig = transactionInputConfig;
        this.senderServerId = senderServerId;
        this.receiverServerId = receiverServerId;
    }

    public void run(){

        AtomicBoolean senderSuccess = new AtomicBoolean(false);
        AtomicBoolean receiverSuccess = new AtomicBoolean(false);


        //Send request to both cluster contact servers
        AtomicReference<CrossTxnResponse> receiverResponse = new AtomicReference<>();
//                = viewServer.serversToPaxosStub
//                .get(Integer.parseInt(receiverServerId.replaceAll("S","")))
//                .crossShardRequest(transactionInputConfig);

        AtomicReference<CrossTxnResponse> senderResponse = new AtomicReference<>();
//        = viewServer.serversToPaxosStub
//                .get(Integer.parseInt(senderServerId.replaceAll("S","")))
//                .crossShardRequest(transactionInputConfig);



        // Create a CountDownLatch to wait for both requests to complete
        CountDownLatch latch = new CountDownLatch(2);

        // Create a thread to handle the sender response
        Thread senderThread = new Thread(() -> {
            try {
                // Send request to the sender server
                senderResponse.set(viewServer.serversToPaxosStub
                        .get(Integer.parseInt(senderServerId.replaceAll("S", "")))
                        .crossShardRequest(transactionInputConfig));

                // Check the response status
                senderSuccess.set(senderResponse.get() != null && senderResponse.get().getSuccess());

            } catch (Exception e) {
                System.err.println("Error occurred while processing sender response: " + e.getMessage());
                this.viewServer.logger.log("Error occurred while processing sender response: " + e.getMessage());
                senderSuccess.set(false);
            } finally {
                latch.countDown(); // Signal that the sender request has completed
            }
        });

        // Create a thread to handle the receiver response
        Thread receiverThread = new Thread(() -> {
            try {
                // Send request to the receiver server
                receiverResponse.set(viewServer.serversToPaxosStub
                        .get(Integer.parseInt(receiverServerId.replaceAll("S", "")))
                        .crossShardRequest(transactionInputConfig));

                // Check the response status
                receiverSuccess.set(receiverResponse.get() != null && receiverResponse.get().getSuccess());

            } catch (Exception e) {
                System.err.println("Error occurred while processing receiver response: " + e.getMessage());
                this.viewServer.logger.log("Error occurred while processing receiver response: " + e.getMessage());
                receiverSuccess.set(false);
            } finally {
                latch.countDown(); // Signal that the receiver request has completed
            }
        });

        // Start both threads
        senderThread.start();
        receiverThread.start();

        try {
            // Wait for both threads to finish processing the requests
            latch.await();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.viewServer.logger.log("Thread interrupted while waiting for responses: " + e.getMessage());
        }

        //wait for response from both servers

        //if both servers respond with success, then commit the transaction

        if (senderSuccess.get() && receiverSuccess.get()) {
            // Commit the transaction
            this.viewServer.logger.log("Transaction successful on both sender and receiver side, committing now....");


            CommitRequest senderCommitRequest = CommitRequest.newBuilder()
                    .setBallotNumber(senderResponse.get().getBallotNumber())
                    .setProcessId(senderResponse.get().getServerName())
                    .setClusterId(senderResponse.get().getClusterId())
                    .setTransaction(transactionInputConfig.getTransaction())
                    .build();

            GlobalConfigs.clusterToServersMap.get(senderResponse.get().getClusterId())
                    .forEach(serverId -> {
                        try {
                            viewServer.serversToPaxosStub.get(serverId).commit(senderCommitRequest);
                        } catch (Exception e) {
                            System.err.println("Error occurred while sending commit request to server " + serverId + ": " + e.getMessage());
                            this.viewServer.logger.log("Error occurred while sending commit request to server " + serverId + ": " + e.getMessage());
                        }
                    });


            CommitRequest receiverCommitRequest = CommitRequest.newBuilder()
                    .setBallotNumber(receiverResponse.get().getBallotNumber())
                    .setProcessId(receiverResponse.get().getServerName())
                    .setClusterId(receiverResponse.get().getClusterId())
                    .setTransaction(transactionInputConfig.getTransaction())
                    .build();

            GlobalConfigs.clusterToServersMap.get(receiverResponse.get().getClusterId())
                    .forEach(serverId -> {
                        try {
                            viewServer.serversToPaxosStub.get(serverId).commit(receiverCommitRequest);
                        } catch (Exception e) {
                            System.err.println("Error occurred while sending commit request to server " + serverId + ": " + e.getMessage());
                            this.viewServer.logger.log("Error occurred while sending commit request to server " + serverId + ": " + e.getMessage());
                        }
                    });



        } else {
            // Abort the transaction if any request failed
            if (!senderSuccess.get()) {
               this.viewServer.logger.log("Transaction failed on sender side, aborting.");
            }
            if (!receiverSuccess.get()) {
                this.viewServer.logger.log("Transaction failed on receiver side, aborting.");
            }

            // Abort transaction logic here


            CommitRequest senderCommitRequest = CommitRequest.newBuilder()
                    .setBallotNumber(senderResponse.get().getBallotNumber())
                    .setProcessId(senderResponse.get().getServerName())
                    .setClusterId(senderResponse.get().getClusterId())
                    .setTransaction(transactionInputConfig.getTransaction())
                    .setAbort(true)
                    .build();

            GlobalConfigs.clusterToServersMap.get(senderResponse.get().getClusterId())
                    .forEach(serverId -> {
                        try {
                            viewServer.serversToPaxosStub.get(serverId).commit(senderCommitRequest);
                        } catch (Exception e) {
                            System.err.println("Error occurred while sending commit request to server " + serverId + ": " + e.getMessage());
                            this.viewServer.logger.log("Error occurred while sending commit request to server " + serverId + ": " + e.getMessage());
                        }
                    });


            CommitRequest receiverCommitRequest = CommitRequest.newBuilder()
                    .setBallotNumber(receiverResponse.get().getBallotNumber())
                    .setProcessId(receiverResponse.get().getServerName())
                    .setClusterId(receiverResponse.get().getClusterId())
                    .setTransaction(transactionInputConfig.getTransaction())
                    .setAbort(true)
                    .build();

            GlobalConfigs.clusterToServersMap.get(receiverResponse.get().getClusterId())
                    .forEach(serverId -> {
                        try {
                            viewServer.serversToPaxosStub.get(serverId).commit(receiverCommitRequest);
                        } catch (Exception e) {
                            System.err.println("Error occurred while sending commit request to server " + serverId + ": " + e.getMessage());
                            this.viewServer.logger.log("Error occurred while sending commit request to server " + serverId + ": " + e.getMessage());
                        }
                    });


        }


        //if any one of the server fails, then abort the transaction


    }

}
