package org.javalite.activeweb.async;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Igor Polevoy on 3/4/15.
 */
public class Host {

    public static String get() {
        String res = "127.0.0.1";
        try {
            res = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignored) {}
        return res;
    }
}
