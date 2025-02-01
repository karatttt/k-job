package org.kjob.nameserver.module.sync;

import lombok.Getter;
import org.kjob.remote.protos.DistroCausa;

@Getter
public class WorkerRemoveSyncInfo extends SyncInfo{
    public WorkerRemoveSyncInfo(String clientIp, String appName){
        super(clientIp);
        this.appName = appName;
    }
    String appName;
}
