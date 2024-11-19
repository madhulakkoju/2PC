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
public final class ActivateServersGrpc {

  private ActivateServersGrpc() {}

  public static final String SERVICE_NAME = "ActivateServers";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.cse535.proto.ActivateServerRequest,
      org.cse535.proto.ActivateServerResponse> getActivateServerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "activateServer",
      requestType = org.cse535.proto.ActivateServerRequest.class,
      responseType = org.cse535.proto.ActivateServerResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.cse535.proto.ActivateServerRequest,
      org.cse535.proto.ActivateServerResponse> getActivateServerMethod() {
    io.grpc.MethodDescriptor<org.cse535.proto.ActivateServerRequest, org.cse535.proto.ActivateServerResponse> getActivateServerMethod;
    if ((getActivateServerMethod = ActivateServersGrpc.getActivateServerMethod) == null) {
      synchronized (ActivateServersGrpc.class) {
        if ((getActivateServerMethod = ActivateServersGrpc.getActivateServerMethod) == null) {
          ActivateServersGrpc.getActivateServerMethod = getActivateServerMethod = 
              io.grpc.MethodDescriptor.<org.cse535.proto.ActivateServerRequest, org.cse535.proto.ActivateServerResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ActivateServers", "activateServer"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.cse535.proto.ActivateServerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.cse535.proto.ActivateServerResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ActivateServersMethodDescriptorSupplier("activateServer"))
                  .build();
          }
        }
     }
     return getActivateServerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.cse535.proto.DeactivateServerRequest,
      org.cse535.proto.DeactivateServerResponse> getDeactivateServerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "deactivateServer",
      requestType = org.cse535.proto.DeactivateServerRequest.class,
      responseType = org.cse535.proto.DeactivateServerResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.cse535.proto.DeactivateServerRequest,
      org.cse535.proto.DeactivateServerResponse> getDeactivateServerMethod() {
    io.grpc.MethodDescriptor<org.cse535.proto.DeactivateServerRequest, org.cse535.proto.DeactivateServerResponse> getDeactivateServerMethod;
    if ((getDeactivateServerMethod = ActivateServersGrpc.getDeactivateServerMethod) == null) {
      synchronized (ActivateServersGrpc.class) {
        if ((getDeactivateServerMethod = ActivateServersGrpc.getDeactivateServerMethod) == null) {
          ActivateServersGrpc.getDeactivateServerMethod = getDeactivateServerMethod = 
              io.grpc.MethodDescriptor.<org.cse535.proto.DeactivateServerRequest, org.cse535.proto.DeactivateServerResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "ActivateServers", "deactivateServer"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.cse535.proto.DeactivateServerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.cse535.proto.DeactivateServerResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new ActivateServersMethodDescriptorSupplier("deactivateServer"))
                  .build();
          }
        }
     }
     return getDeactivateServerMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ActivateServersStub newStub(io.grpc.Channel channel) {
    return new ActivateServersStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ActivateServersBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new ActivateServersBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ActivateServersFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new ActivateServersFutureStub(channel);
  }

  /**
   */
  public static abstract class ActivateServersImplBase implements io.grpc.BindableService {

    /**
     */
    public void activateServer(org.cse535.proto.ActivateServerRequest request,
        io.grpc.stub.StreamObserver<org.cse535.proto.ActivateServerResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getActivateServerMethod(), responseObserver);
    }

    /**
     */
    public void deactivateServer(org.cse535.proto.DeactivateServerRequest request,
        io.grpc.stub.StreamObserver<org.cse535.proto.DeactivateServerResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getDeactivateServerMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getActivateServerMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.cse535.proto.ActivateServerRequest,
                org.cse535.proto.ActivateServerResponse>(
                  this, METHODID_ACTIVATE_SERVER)))
          .addMethod(
            getDeactivateServerMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.cse535.proto.DeactivateServerRequest,
                org.cse535.proto.DeactivateServerResponse>(
                  this, METHODID_DEACTIVATE_SERVER)))
          .build();
    }
  }

  /**
   */
  public static final class ActivateServersStub extends io.grpc.stub.AbstractStub<ActivateServersStub> {
    private ActivateServersStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ActivateServersStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ActivateServersStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ActivateServersStub(channel, callOptions);
    }

    /**
     */
    public void activateServer(org.cse535.proto.ActivateServerRequest request,
        io.grpc.stub.StreamObserver<org.cse535.proto.ActivateServerResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getActivateServerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deactivateServer(org.cse535.proto.DeactivateServerRequest request,
        io.grpc.stub.StreamObserver<org.cse535.proto.DeactivateServerResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDeactivateServerMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ActivateServersBlockingStub extends io.grpc.stub.AbstractStub<ActivateServersBlockingStub> {
    private ActivateServersBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ActivateServersBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ActivateServersBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ActivateServersBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.cse535.proto.ActivateServerResponse activateServer(org.cse535.proto.ActivateServerRequest request) {
      return blockingUnaryCall(
          getChannel(), getActivateServerMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.cse535.proto.DeactivateServerResponse deactivateServer(org.cse535.proto.DeactivateServerRequest request) {
      return blockingUnaryCall(
          getChannel(), getDeactivateServerMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ActivateServersFutureStub extends io.grpc.stub.AbstractStub<ActivateServersFutureStub> {
    private ActivateServersFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private ActivateServersFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ActivateServersFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new ActivateServersFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.cse535.proto.ActivateServerResponse> activateServer(
        org.cse535.proto.ActivateServerRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getActivateServerMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.cse535.proto.DeactivateServerResponse> deactivateServer(
        org.cse535.proto.DeactivateServerRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getDeactivateServerMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_ACTIVATE_SERVER = 0;
  private static final int METHODID_DEACTIVATE_SERVER = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ActivateServersImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ActivateServersImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ACTIVATE_SERVER:
          serviceImpl.activateServer((org.cse535.proto.ActivateServerRequest) request,
              (io.grpc.stub.StreamObserver<org.cse535.proto.ActivateServerResponse>) responseObserver);
          break;
        case METHODID_DEACTIVATE_SERVER:
          serviceImpl.deactivateServer((org.cse535.proto.DeactivateServerRequest) request,
              (io.grpc.stub.StreamObserver<org.cse535.proto.DeactivateServerResponse>) responseObserver);
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

  private static abstract class ActivateServersBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ActivateServersBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.cse535.proto.Paxos2Pc.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ActivateServers");
    }
  }

  private static final class ActivateServersFileDescriptorSupplier
      extends ActivateServersBaseDescriptorSupplier {
    ActivateServersFileDescriptorSupplier() {}
  }

  private static final class ActivateServersMethodDescriptorSupplier
      extends ActivateServersBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ActivateServersMethodDescriptorSupplier(String methodName) {
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
      synchronized (ActivateServersGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ActivateServersFileDescriptorSupplier())
              .addMethod(getActivateServerMethod())
              .addMethod(getDeactivateServerMethod())
              .build();
        }
      }
    }
    return result;
  }
}
