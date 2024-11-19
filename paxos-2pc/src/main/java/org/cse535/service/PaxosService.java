package org.cse535.service;

import io.grpc.stub.StreamObserver;
import org.cse535.Main;
import org.cse535.proto.IntraPaxosGrpc;
import org.cse535.proto.PaxosGrpc;
import org.cse535.proto.TransactionInputConfig;
import org.cse535.proto.TxnResponse;

public class PaxosService extends PaxosGrpc.PaxosImplBase {

    @Override
    public void request(TransactionInputConfig request, StreamObserver<TxnResponse> responseObserver) {

        if(!Main.node.isServerActive.get()){
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






}
