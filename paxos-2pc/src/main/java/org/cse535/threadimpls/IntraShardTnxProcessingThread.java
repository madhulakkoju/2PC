package org.cse535.threadimpls;

import org.cse535.configs.GlobalConfigs;
import org.cse535.node.Node;
import org.cse535.proto.*;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IntraShardTnxProcessingThread extends Thread {

    public Transaction tnx;
    public Node node;
    public int ballotNumber;

    public IntraShardTnxProcessingThread(Node node, Transaction tnx, int ballotNumber) {
        this.tnx = tnx;
        this.node = node;
        this.ballotNumber = ballotNumber;

        this.node.database.transactionMap.put(ballotNumber, tnx);
    }



    public void run() {
        try {
        //Wait until locks released if locked.
        if( this.node.lockedDataItemsWithTransactionNum.containsKey(this.tnx.getSender())
                || this.node.lockedDataItemsWithTransactionNum.containsKey(this.tnx.getReceiver())) {
                Thread.sleep(10);
        }


            System.out.println("Processing transaction " + this.tnx.getTransactionNum() + " "
                    + this.tnx.getSender() + " -> "
                    + this.tnx.getReceiver() + " = "
                    + this.tnx.getAmount());

        //Acquire the locks
        this.node.lockedDataItemsWithTransactionNum.put(this.tnx.getSender(), this.tnx.getTransactionNum());
        this.node.lockedDataItemsWithTransactionNum.put(this.tnx.getReceiver(), this.tnx.getTransactionNum());

        //Check if the transaction is valid
        if(this.node.database.isValidTransaction(this.tnx)) {
            // Valid transaction

            // Initiate Prepare phase

            IntraPrepareThread[] intraPrepareThreads = new IntraPrepareThread[GlobalConfigs.numServersPerCluster];

            ConcurrentHashMap<Integer, PrepareResponse> prepareResponses = new ConcurrentHashMap<>();
            AtomicInteger successPrepares = new AtomicInteger(1);

            PrepareRequest.Builder prepareBuilder = PrepareRequest.newBuilder();

            prepareBuilder.setBallotNumber(this.ballotNumber)
                    .setProcessId(this.node.serverName)
                    .setTransaction(this.tnx);

            if(this.node.database.lastCommittedTransaction != null) {
                    prepareBuilder.setLatestCommittedTransaction(this.node.database.lastCommittedTransaction);
            }

            prepareBuilder.setLatestCommittedBallotNumber( this.node.database.lastCommittedBallotNumber)
                    .setClusterId(this.node.clusterNumber);


            PrepareRequest prepareRequest = prepareBuilder.build();

            for (int i = 0; i < GlobalConfigs.numServersPerCluster; i++) {
                if (Objects.equals(GlobalConfigs.clusterToServersMap.get(this.node.clusterNumber).get(i), this.node.serverNumber)) {
                    continue;
                }
                intraPrepareThreads[i] = new IntraPrepareThread( this.node, prepareRequest, prepareResponses, GlobalConfigs.clusterToServersMap.get(this.node.clusterNumber).get(i), successPrepares);
                intraPrepareThreads[i].start();
            }

            this.node.database.transactionStatusMap.put(ballotNumber, TransactionStatus.PREPARED);

            for (int i = 0; i < GlobalConfigs.numServersPerCluster; i++) {
                if(intraPrepareThreads[i] == null) continue;
                intraPrepareThreads[i].join();
            }


            if( successPrepares.get() < GlobalConfigs.ShardConsesusThreshold) {
                // Retry Prepare phase if Synced
                PrepareResponse syncPrepareResponse = null;

                int maxCommittedBallotNumber = -1;

                for ( PrepareResponse r : prepareResponses.values() ) {
                    if( r.getNeedToSync() ) {
                        if( r.getLastCommittedBallotNumber() > maxCommittedBallotNumber) {
                            maxCommittedBallotNumber = r.getLastCommittedBallotNumber();
                            syncPrepareResponse = r;
                        }
                    }
                }

                if( syncPrepareResponse != null) {
                    // Sync the data
                    this.node.syncData(syncPrepareResponse);
                    this.ballotNumber = this.node.database.ballotNumber.incrementAndGet();
                    Thread.sleep(10);

                    this.node.logger.log("Synced data ... Now Retrying Prepare phase");

                    this.node.logger.log(syncPrepareResponse.toString());


                    intraPrepareThreads = new IntraPrepareThread[GlobalConfigs.numServersPerCluster];

                    prepareResponses = new ConcurrentHashMap<>();
                    successPrepares = new AtomicInteger(1);

                    prepareBuilder = PrepareRequest.newBuilder();

                    prepareBuilder.setBallotNumber(this.ballotNumber)
                            .setProcessId(this.node.serverName)
                            .setTransaction(this.tnx);

                    if (this.node.database.lastCommittedTransaction != null) {
                        prepareBuilder.setLatestCommittedTransaction(this.node.database.lastCommittedTransaction);
                    }

                    prepareBuilder.setLatestCommittedBallotNumber(this.node.database.lastCommittedBallotNumber)
                            .setClusterId(this.node.clusterNumber);


                    prepareRequest = prepareBuilder.build();

                    for (int i = 0; i < GlobalConfigs.numServersPerCluster; i++) {
                        if (Objects.equals(GlobalConfigs.clusterToServersMap.get(this.node.clusterNumber).get(i), this.node.serverNumber)) {
                            continue;
                        }
                        intraPrepareThreads[i] = new IntraPrepareThread(this.node, prepareRequest, prepareResponses, GlobalConfigs.clusterToServersMap.get(this.node.clusterNumber).get(i), successPrepares);
                        intraPrepareThreads[i].start();
                    }

                    this.node.database.transactionStatusMap.put(ballotNumber, TransactionStatus.PREPARED);

                    for (int i = 0; i < GlobalConfigs.numServersPerCluster; i++) {
                        if (intraPrepareThreads[i] == null) continue;
                        intraPrepareThreads[i].join();
                    }
                }

            }





            // If f+1 Prepare responses received, Initiate Commit phase
            if( successPrepares.get() >= GlobalConfigs.ShardConsesusThreshold) {

                this.node.database.transactionStatusMap.put(ballotNumber, TransactionStatus.ACCEPTED);
                this.node.database.lastCommittedBallotNumber = this.ballotNumber;

                CommitRequest commitRequest = CommitRequest.newBuilder()
                        .setBallotNumber(this.ballotNumber)
                        .setProcessId(this.node.serverName)
                        .setTransaction(this.tnx)
                        .setClusterId(this.node.clusterNumber)
                        .build();

                ConcurrentHashMap<Integer, CommitResponse> commitResponses = new ConcurrentHashMap<>();
                AtomicInteger successCommits = new AtomicInteger(1);

                // Initiate Commit phase

                IntraCommitThread[] intraCommitThreads = new IntraCommitThread[GlobalConfigs.numServersPerCluster];

                for (int i = 0; i < GlobalConfigs.numServersPerCluster; i++) {
                    if (Objects.equals(GlobalConfigs.clusterToServersMap.get(this.node.clusterNumber).get(i), this.node.serverNumber)) {
                        continue;
                    }
                    intraCommitThreads[i] = new IntraCommitThread( this.node, commitRequest, commitResponses, GlobalConfigs.clusterToServersMap.get(this.node.clusterNumber).get(i), successCommits);
                    intraCommitThreads[i].start();
                }

                for (int i = 0; i < GlobalConfigs.numServersPerCluster; i++) {
                    if(intraCommitThreads[i] == null) continue;
                    intraCommitThreads[i].join();
                }

            } else {
                // Abort the transaction
                System.out.println("Transaction aborted");
            }

            // Commit the transaction
            this.node.database.executeTransaction(this.tnx);

            // If f+1 Commit responses received, Commit the transaction



            //Release the locks
            this.node.lockedDataItemsWithTransactionNum.remove(this.tnx.getSender());
            this.node.lockedDataItemsWithTransactionNum.remove(this.tnx.getReceiver());

            //Send reply to Client
            System.out.println("Transaction processed successfully");

        }

        } catch (InterruptedException e) {
            e.printStackTrace();
            this.node.logger.log("Error processing transaction " + this.tnx.getTransactionNum() + " " + e.getMessage());
        }
        finally {
            this.node.lockedDataItemsWithTransactionNum.remove(this.tnx.getSender());
            this.node.lockedDataItemsWithTransactionNum.remove(this.tnx.getReceiver());
        }

    }


}
