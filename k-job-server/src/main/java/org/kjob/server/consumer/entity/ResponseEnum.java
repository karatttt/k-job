package org.kjob.server.consumer.entity;

public enum ResponseEnum {
    SUCCESS("success"),
    FLUSH_ERROR("flush to disk error");

    private final String v;


    public String getV() {
        return v;
    }

    ResponseEnum(String v) {
        this.v = v;
    }
}
