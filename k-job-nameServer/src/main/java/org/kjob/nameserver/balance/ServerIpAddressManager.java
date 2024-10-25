package org.kjob.nameserver.balance;

import lombok.Getter;

import java.util.*;

public class ServerIpAddressManager {

    @Getter
    private static Set<String> serverIpAddressSet = new HashSet<>();

    public static void add2ServerIpAddressSet(String ip) {
        serverIpAddressSet.add(ip);
    }

}
