package org.kjob.common.utils.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MyNetUtils {

    public static String address;
    private static void setAddress() throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        address = inetAddress.getHostAddress();
    }

}
