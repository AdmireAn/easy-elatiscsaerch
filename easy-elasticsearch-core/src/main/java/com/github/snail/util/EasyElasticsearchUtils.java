package com.github.snail.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.google.common.net.HostAndPort;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class EasyElasticsearchUtils {

    private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    public static boolean check(HostAndPort hostAndPort) {
        return check(hostAndPort.getHost(), hostAndPort.getPort());
    }

    public static boolean check(String host, int port) {
        return check(host, port, DEFAULT_CONNECTION_TIMEOUT);
    }

    public static boolean check(String host, int port, int connectionTimeoutInMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), connectionTimeoutInMs);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
