package org.cse535.service;

import io.grpc.stub.StreamObserver;
import org.cse535.proto.*;

public class CommandsService extends CommandsGrpc.CommandsImplBase {

    @Override
    public void performance(CommandInput request, StreamObserver<CommandOutput> responseObserver) {

    }

    @Override
    public void printLog(CommandInput request, StreamObserver<CommandOutput> responseObserver) {

    }

    @Override
    public void printDB(CommandInput request, StreamObserver<CommandOutput> responseObserver) {

    }

    @Override
    public void printBalance(CommandInput request, StreamObserver<CommandOutput> responseObserver) {

    }
}
