package com.packetscope.desktop.service;

import java.util.Set;
import java.util.stream.Collectors;
import java.net.InetAddress;
import java.net.NetworkInterface;


public final class LocalIpResolver {

    public static Set<InetAddress> resolve() {
        try {
            return NetworkInterface.networkInterfaces()
                .flatMap(ni -> ni.inetAddresses())
                .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}