// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: causa/cgrpc.proto

package org.kjob.remote.api;

public final class CausaGrpcClientGen {
  private CausaGrpcClientGen() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\021causa/cgrpc.proto\032!causa/server_discov" +
      "er_causa.proto\032\032causa/schedule_causa.pro" +
      "to\032\030causa/common_causa.proto\032\033google/pro" +
      "tobuf/empty.proto2\206\001\n\016ServerDiscover\022\"\n\t" +
      "assertApp\022\010.AppName\032\t.Response\"\000\022.\n\016hear" +
      "tbeatCheck\022\017.HeartbeatCheck\032\t.Response\"\000" +
      "\022 \n\npingServer\022\005.Ping\032\t.Response\"\0002{\n\010Sc" +
      "hedule\0226\n\025reportWorkerHeartbeat\022\020.Worker" +
      "Heartbeat\032\t.Response\"\000\0227\n\021serverSchedule" +
      "Job\022\025.ServerScheduleJobReq\032\t.Response\"\000B",
      ")\n\023org.kjob.remote.apiB\022CausaGrpcClientG" +
      "enb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          org.kjob.remote.protos.ServerDiscoverCausa.getDescriptor(),
          org.kjob.remote.protos.ScheduleCausa.getDescriptor(),
          org.kjob.remote.protos.CommonCausa.getDescriptor(),
          com.google.protobuf.EmptyProto.getDescriptor(),
        }, assigner);
    org.kjob.remote.protos.ServerDiscoverCausa.getDescriptor();
    org.kjob.remote.protos.ScheduleCausa.getDescriptor();
    org.kjob.remote.protos.CommonCausa.getDescriptor();
    com.google.protobuf.EmptyProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
