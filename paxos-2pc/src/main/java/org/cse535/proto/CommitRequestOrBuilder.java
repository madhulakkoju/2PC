// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: paxos-2pc.proto

package org.cse535.proto;

public interface CommitRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:CommitRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 ballotNumber = 1;</code>
   */
  int getBallotNumber();

  /**
   * <pre>
   * processId is Server Name
   * </pre>
   *
   * <code>string processId = 2;</code>
   */
  java.lang.String getProcessId();
  /**
   * <pre>
   * processId is Server Name
   * </pre>
   *
   * <code>string processId = 2;</code>
   */
  com.google.protobuf.ByteString
      getProcessIdBytes();

  /**
   * <code>.Transaction transaction = 3;</code>
   */
  boolean hasTransaction();
  /**
   * <code>.Transaction transaction = 3;</code>
   */
  org.cse535.proto.Transaction getTransaction();
  /**
   * <code>.Transaction transaction = 3;</code>
   */
  org.cse535.proto.TransactionOrBuilder getTransactionOrBuilder();

  /**
   * <code>int32 clusterId = 8;</code>
   */
  int getClusterId();
}
