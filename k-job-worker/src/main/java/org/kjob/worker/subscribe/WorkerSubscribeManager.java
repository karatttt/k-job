package org.kjob.worker.subscribe;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


public class WorkerSubscribeManager {

    private static boolean splitStatus;

    @Getter
    private static String subAppName;

    @Getter
    private static List<String> serverIpList;

    public static void setServerIpList(List<String> serverIpList) {
        WorkerSubscribeManager.serverIpList = serverIpList;
    }

    public static boolean isSplit() {
        return splitStatus;
    }

    public static void setSplitStatus(boolean splitStatus) {
        WorkerSubscribeManager.splitStatus = splitStatus;
    }

    public static void setSubAppName(String subAppName) {
        WorkerSubscribeManager.subAppName = subAppName;
    }



}
