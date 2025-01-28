package org.kjob.common.constant;

import org.springframework.beans.factory.annotation.Value;

public class RemoteConstant {

    public static final int DEFAULT_WORKER_GRPC_PORT = 9082;
    public static final int DEFAULT_SERVER_GRPC_PORT = 9081;
    public static final int DEFAULT_NAMESERVER_GRPC_PORT = 9081;



    public static final int SUCCESS = 200;
    public static final int FAULT = 500;



    public static final String SERVER = "SERVER";
    public static final String WORKER = "WORKER";
    public static final String NAMESERVER = "NAMESERVER";


    // when kjobserver register
    public static final String INCREMENTAL_ADD_SERVER = "INCREMENTAL_ADD_SERVER";
    // when kjobworker subscribe
    public static final String INCREMENTAL_ADD_WORKER = "INCREMENTAL_ADD_WORKER";

    // when client shutdown or timeout
    public static final String INCREMENTAL_REMOVE_SERVER = "INCREMENTAL_REMOVE_SERVER";
    public static final String INCREMENTAL_REMOVE_WORKER = "INCREMENTAL_REMOVE_WORKER";
    // data check
    public static final String FULL_SYNC = "FULL_SYNC";

}
