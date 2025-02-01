package org.kjob.nameserver.module.sync;

import lombok.Getter;
import org.kjob.remote.protos.DistroCausa;

@Getter
public class ServerRemoveSyncInfo extends SyncInfo{
    public ServerRemoveSyncInfo(String serverIpAddress){
        super(serverIpAddress);
        this.serverIpAddress = serverIpAddress;
    }
    String serverIpAddress;
}
