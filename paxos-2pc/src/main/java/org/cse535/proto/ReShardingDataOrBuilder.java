// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: paxos-2pc.proto

package org.cse535.proto;

public interface ReShardingDataOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ReShardingData)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 clusterId = 1;</code>
   */
  int getClusterId();

  /**
   * <code>map&lt;int32, int32&gt; accountBalances = 2;</code>
   */
  int getAccountBalancesCount();
  /**
   * <code>map&lt;int32, int32&gt; accountBalances = 2;</code>
   */
  boolean containsAccountBalances(
      int key);
  /**
   * Use {@link #getAccountBalancesMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.Integer, java.lang.Integer>
  getAccountBalances();
  /**
   * <code>map&lt;int32, int32&gt; accountBalances = 2;</code>
   */
  java.util.Map<java.lang.Integer, java.lang.Integer>
  getAccountBalancesMap();
  /**
   * <code>map&lt;int32, int32&gt; accountBalances = 2;</code>
   */

  int getAccountBalancesOrDefault(
      int key,
      int defaultValue);
  /**
   * <code>map&lt;int32, int32&gt; accountBalances = 2;</code>
   */

  int getAccountBalancesOrThrow(
      int key);

  /**
   * <code>map&lt;int32, int32&gt; newDataItemClusterConfig = 3;</code>
   */
  int getNewDataItemClusterConfigCount();
  /**
   * <code>map&lt;int32, int32&gt; newDataItemClusterConfig = 3;</code>
   */
  boolean containsNewDataItemClusterConfig(
      int key);
  /**
   * Use {@link #getNewDataItemClusterConfigMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.Integer, java.lang.Integer>
  getNewDataItemClusterConfig();
  /**
   * <code>map&lt;int32, int32&gt; newDataItemClusterConfig = 3;</code>
   */
  java.util.Map<java.lang.Integer, java.lang.Integer>
  getNewDataItemClusterConfigMap();
  /**
   * <code>map&lt;int32, int32&gt; newDataItemClusterConfig = 3;</code>
   */

  int getNewDataItemClusterConfigOrDefault(
      int key,
      int defaultValue);
  /**
   * <code>map&lt;int32, int32&gt; newDataItemClusterConfig = 3;</code>
   */

  int getNewDataItemClusterConfigOrThrow(
      int key);
}