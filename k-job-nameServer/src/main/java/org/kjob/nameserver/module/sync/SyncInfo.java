package org.kjob.nameserver.module.sync;

import lombok.Data;

@Data
public class SyncInfo {
   SyncInfo(String clientIP){
      this.clientIp = clientIP;
   }
   public String clientIp;
}
