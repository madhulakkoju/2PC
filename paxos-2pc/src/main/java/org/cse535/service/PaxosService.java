package org.cse535.service;

import io.grpc.stub.StreamObserver;
import org.cse535.Main;
import org.cse535.configs.Utils;
import org.cse535.proto.*;

public class PaxosService extends PaxosGrpc.PaxosImplBase {

    @Override
    public void request(TransactionInputConfig request, StreamObserver<TxnResponse> responseObserver) {

        if(!Main.node.isServerActive.get()){
            //Inactive server
            TxnResponse response = TxnResponse.newBuilder().setSuccess(false).setServerName(Main.node.serverName).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        Main.node.database.incomingTransactionsQueue.add(request);
        TxnResponse response = TxnResponse.newBuilder().setSuccess(true).setServerName(Main.node.serverName).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void crossShardRequest(TransactionInputConfig request, StreamObserver<CrossTxnResponse> responseObserver) {

        if(!Main.node.isServerActive.get()){
            //Inactive server
            CrossTxnResponse response = CrossTxnResponse.newBuilder()
                    .setSuccess(false)
                    .setServerName(Main.node.serverName)
                    .setBallotNumber(-1)
                    .setClusterId(Main.node.clusterNumber)
                    .setSuccessPreparesCount(0)
                    .setFailureReason("Server Inactive")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        Main.node.logger.log("Received cross shard request " + Utils.toString(request.getTransaction()) );

        CrossTxnResponse response = Main.node.processCrossShardTransaction(request);

        Main.node.logger.log("Sending cross shard response \n**************\n" + response.toString() + "\n**************" );

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void prepare(PrepareRequest request, StreamObserver<PrepareResponse> responseObserver) {
        Main.node.logger.log("Received prepare request from " + request.getProcessId() + " with ballot number " + request.getBallotNumber());
        if( ! Main.node.isServerActive.get() ){
            //Inactive server
            PrepareResponse response = PrepareResponse.newBuilder()
                    .setSuccess(false)
                    .setBallotNumber(request.getBallotNumber())
                    .setProcessId(Main.node.serverName)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        PrepareResponse resp = Main.node.handlePreparePhase(request);
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }


    @Override
    public void commit(CommitRequest request, StreamObserver<CommitResponse> responseObserver) {
        Main.node.logger.log("Received Commit request from " + request.getProcessId() + " with ballot number " + request.getBallotNumber());

        if( ! Main.node.isServerActive.get() ){
            //Inactive server
            CommitResponse response = CommitResponse.newBuilder()
                    .setSuccess(false)
                    .setBallotNumber(request.getBallotNumber())
                    .setProcessId(Main.node.serverName)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        CommitResponse resp = Main.node.handleCommit(request);
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}
