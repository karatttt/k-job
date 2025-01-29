package org.kjob.nameserver.module;

// 元信息类（包含服务名称、校验和、版本号）
public class Metadata {
    private String serviceName;
    private String checksum; // 数据校验和
    private long version;    // 数据版本号

    public Metadata(String serviceName, String checksum, long version) {
        this.serviceName = serviceName;
        this.checksum = checksum;
        this.version = version;
    }

    // Getters
    public String getServiceName() { return serviceName; }
    public String getChecksum() { return checksum; }
    public long getVersion() { return version; }
}