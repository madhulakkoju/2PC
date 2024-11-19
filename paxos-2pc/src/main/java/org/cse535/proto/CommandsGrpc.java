package org.cse535.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.15.0)",
    comments = "Source: paxos-2pc.proto")
public final class CommandsGrpc {

  private CommandsGrpc() {}

  public static final String SERVICE_NAME = "Commands";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.cse535.proto.CommandInput,
      org.cse535.proto.CommandOutput> getPrintBalanceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "printBalance",
      requestType = org.cse535.proto.CommandInput.class,
      responseType = org.cse535.proto.CommandOutput.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.cse535.proto.CommandInput,
      org.cse535.proto.CommandOutput> getPrintBalanceMethod() {
    io.grpc.MethodDescriptor<org.cse535.proto.CommandInput, org.cse535.proto.CommandOutput> getPrintBalanceMethod;
    if ((getPrintBalanceMethod = CommandsGrpc.getPrintBalanceMethod) == null) {
      synchronized (CommandsGrpc.class) {
        if ((getPrintBalanceMethod = CommandsGrpc.getPrintBalanceMethod) == null) {
          CommandsGrpc.getPrintBalanceMethod = getPrintBalanceMethod = 
              io.grpc.MethodDescriptor.<org.cse535.proto.CommandInput, org.cse535.proto.CommandOutput>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "Commands", "printBalance"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.cse535.proto.CommandInput.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.cse535.proto.CommandOutput.getDefaultInstance()))
                  .setSchemaDescriptor(new CommandsMethodDescriptorSupplier("printBalance"))
                  .build();
          }
        }
     }
     return getPrintBalanceMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.cse535.proto.CommandInput,
      org.cse535.proto.CommandOutput> getPrintLogMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "printLog",
      requestType = org.cse535.proto.CommandInput.class,
      responseType = org.cse535.proto.CommandOutput.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.cse535.proto.CommandInput,
      org.cse535.proto.CommandOutput> getPrintLogMethod() {
    io.grpc.MethodDescriptor<org.cse535.proto.CommandInput, org.cse535.proto.CommandOutput> getPrintLogMethod;
    if ((getPrintLogMethod = CommandsGrpc.getPrintLogMethod) == null) {
      synchronized (CommandsGrpc.class) {
        if ((getPrintLogMethod = CommandsGrpc.getPrintLogMethod) == null) {
          CommandsGrpc.getPrintLogMethod = getPrintLogMethod = 
              io.grpc.MethodDescriptor.<org.cse535.proto.CommandInput, org.cse535.proto.CommandOutput>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "Commands", "printLog"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.cse535.proto.CommandInput.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.cse535.proto.CommandOutput.getDefaultInstance()))
                  .setSchemaDescriptor(new CommandsMethodDescriptorSupplier("printLog"))
                  .build();
          }
        }
     }
     return getPrintLogMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.cse535.proto.CommandInput,
      org.cse535.proto.CommandOutput> getPrintDBMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "printDB",
      requestType = org.cse535.proto.CommandInput.class,
      responseType = org.cse535.proto.CommandOutput.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.cse535.proto.CommandInput,
      org.cse535.proto.CommandOutput> getPrintDBMethod() {
    io.grpc.MethodDescriptor<org.cse535.proto.CommandInput, org.cse535.proto.CommandOutput> getPrintDBMethod;
    if ((getPrintDBMethod = CommandsGrpc.getPrintDBMethod) == null) {
      synchronized (CommandsGrpc.class) {
        if ((getPrintDBMethod = CommandsGrpc.getPrintDBMethod) == null) {
          CommandsGrpc.getPrintDBMethod = getPrintDBMethod = 
              io.grpc.MethodDescriptor.<org.cse535.proto.CommandInput, org.cse535.proto.CommandOutput>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "Commands", "printDB"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.cse535.proto.CommandInput.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.cse535.proto.CommandOutput.getDefaultInstance()))
                  .setSchemaDescriptor(new CommandsMethodDescriptorSupplier("printDB"))
                  .build();
          }
        }
     }
     return getPrintDBMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.cse535.proto.CommandInput,
      org.cse535.proto.CommandOutput> getPerformanceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Performance",
      requestType = org.cse535.proto.CommandInput.class,
      responseType = org.cse535.proto.CommandOutput.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.cse535.proto.CommandInput,
      org.cse535.proto.CommandOutput> getPerformanceMethod() {
    io.grpc.MethodDescriptor<org.cse535.proto.CommandInput, org.cse535.proto.CommandOutput> getPerformanceMethod;
    if ((getPerformanceMethod = CommandsGrpc.getPerformanceMethod) == null) {
      synchronized (CommandsGrpc.class) {
        if ((getPerformanceMethod = CommandsGrpc.getPerformanceMethod) == null) {
          CommandsGrpc.getPerformanceMethod = getPerformanceMethod = 
              io.grpc.MethodDescriptor.<org.cse535.proto.CommandInput, org.cse535.proto.CommandOutput>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "Commands", "Performance"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.cse535.proto.CommandInput.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.cse535.proto.CommandOutput.getDefaultInstance()))
                  .setSchemaDescriptor(new CommandsMethodDescriptorSupplier("Performance"))
                  .build();
          }
        }
     }
     return getPerformanceMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CommandsStub newStub(io.grpc.Channel channel) {
    return new CommandsStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CommandsBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new CommandsBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CommandsFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new CommandsFutureStub(channel);
  }

  /**
   */
  public static abstract class CommandsImplBase implements io.grpc.BindableService {

    /**
     */
    public void printBalance(org.cse535.proto.CommandInput request,
        io.grpc.stub.StreamObserver<org.cse535.proto.CommandOutput> responseObserver) {
      asyncUnimplementedUnaryCall(getPrintBalanceMethod(), responseObserver);
    }

    /**
     */
    public void printLog(org.cse535.proto.CommandInput request,
        io.grpc.stub.StreamObserver<org.cse535.proto.CommandOutput> responseObserver) {
      asyncUnimplementedUnaryCall(getPrintLogMethod(), responseObserver);
    }

    /**
     */
    public void printDB(org.cse535.proto.CommandInput request,
        io.grpc.stub.StreamObserver<org.cse535.proto.CommandOutput> responseObserver) {
      asyncUnimplementedUnaryCall(getPrintDBMethod(), responseObserver);
    }

    /**
     */
    public void performance(org.cse535.proto.CommandInput request,
        io.grpc.stub.StreamObserver<org.cse535.proto.CommandOutput> responseObserver) {
      asyncUnimplementedUnaryCall(getPerformanceMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getPrintBalanceMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.cse535.proto.CommandInput,
                org.cse535.proto.CommandOutput>(
                  this, METHODID_PRINT_BALANCE)))
          .addMethod(
            getPrintLogMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.cse535.proto.CommandInput,
                org.cse535.proto.CommandOutput>(
                  this, METHODID_PRINT_LOG)))
          .addMethod(
            getPrintDBMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.cse535.proto.CommandInput,
                org.cse535.proto.CommandOutput>(
                  this, METHODID_PRINT_DB)))
          .addMethod(
            getPerformanceMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.cse535.proto.CommandInput,
                org.cse535.proto.CommandOutput>(
                  this, METHODID_PERFORMANCE)))
          .build();
    }
  }

  /**
   */
  public static final class CommandsStub extends io.grpc.stub.AbstractStub<CommandsStub> {
    private CommandsStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CommandsStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CommandsStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CommandsStub(channel, callOptions);
    }

    /**
     */
    public void printBalance(org.cse535.proto.CommandInput request,
        io.grpc.stub.StreamObserver<org.cse535.proto.CommandOutput> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPrintBalanceMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void printLog(org.cse535.proto.CommandInput request,
        io.grpc.stub.StreamObserver<org.cse535.proto.CommandOutput> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPrintLogMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void printDB(org.cse535.proto.CommandInput request,
        io.grpc.stub.StreamObserver<org.cse535.proto.CommandOutput> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPrintDBMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void performance(org.cse535.proto.CommandInput request,
        io.grpc.stub.StreamObserver<org.cse535.proto.CommandOutput> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPerformanceMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class CommandsBlockingStub extends io.grpc.stub.AbstractStub<CommandsBlockingStub> {
    private CommandsBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CommandsBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CommandsBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CommandsBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.cse535.proto.CommandOutput printBalance(org.cse535.proto.CommandInput request) {
      return blockingUnaryCall(
          getChannel(), getPrintBalanceMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.cse535.proto.CommandOutput printLog(org.cse535.proto.CommandInput request) {
      return blockingUnaryCall(
          getChannel(), getPrintLogMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.cse535.proto.CommandOutput printDB(org.cse535.proto.CommandInput request) {
      return blockingUnaryCall(
          getChannel(), getPrintDBMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.cse535.proto.CommandOutput performance(org.cse535.proto.CommandInput request) {
      return blockingUnaryCall(
          getChannel(), getPerformanceMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class CommandsFutureStub extends io.grpc.stub.AbstractStub<CommandsFutureStub> {
    private CommandsFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CommandsFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CommandsFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CommandsFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.cse535.proto.CommandOutput> printBalance(
        org.cse535.proto.CommandInput request) {
      return futureUnaryCall(
          getChannel().newCall(getPrintBalanceMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.cse535.proto.CommandOutput> printLog(
        org.cse535.proto.CommandInput request) {
      return futureUnaryCall(
          getChannel().newCall(getPrintLogMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.cse535.proto.CommandOutput> printDB(
        org.cse535.proto.CommandInput request) {
      return futureUnaryCall(
          getChannel().newCall(getPrintDBMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.cse535.proto.CommandOutput> performance(
        org.cse535.proto.CommandInput request) {
      return futureUnaryCall(
          getChannel().newCall(getPerformanceMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PRINT_BALANCE = 0;
  private static final int METHODID_PRINT_LOG = 1;
  private static final int METHODID_PRINT_DB = 2;
  private static final int METHODID_PERFORMANCE = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CommandsImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(CommandsImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PRINT_BALANCE:
          serviceImpl.printBalance((org.cse535.proto.CommandInput) request,
              (io.grpc.stub.StreamObserver<org.cse535.proto.CommandOutput>) responseObserver);
          break;
        case METHODID_PRINT_LOG:
          serviceImpl.printLog((org.cse535.proto.CommandInput) request,
              (io.grpc.stub.StreamObserver<org.cse535.proto.CommandOutput>) responseObserver);
          break;
        case METHODID_PRINT_DB:
          serviceImpl.printDB((org.cse535.proto.CommandInput) request,
              (io.grpc.stub.StreamObserver<org.cse535.proto.CommandOutput>) responseObserver);
          break;
        case METHODID_PERFORMANCE:
          serviceImpl.performance((org.cse535.proto.CommandInput) request,
              (io.grpc.stub.StreamObserver<org.cse535.proto.CommandOutput>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class CommandsBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CommandsBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.cse535.proto.Paxos2Pc.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Commands");
    }
  }

  private static final class CommandsFileDescriptorSupplier
      extends CommandsBaseDescriptorSupplier {
    CommandsFileDescriptorSupplier() {}
  }

  private static final class CommandsMethodDescriptorSupplier
      extends CommandsBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    CommandsMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (CommandsGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CommandsFileDescriptorSupplier())
              .addMethod(getPrintBalanceMethod())
              .addMethod(getPrintLogMethod())
              .addMethod(getPrintDBMethod())
              .addMethod(getPerformanceMethod())
              .build();
        }
      }
    }
    return result;
  }
}
