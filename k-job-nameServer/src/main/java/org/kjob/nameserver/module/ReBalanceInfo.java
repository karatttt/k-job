package org.kjob.nameserver.module;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class ReBalanceInfo {

    List<String> ServerIpList;
    boolean isSplit;
    String subAppName;

}
