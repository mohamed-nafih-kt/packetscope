//package com.packetscope.network;
//
//import org.springframework.stereotype.Component;
//
//import jakarta.annotation.PostConstruct;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Set;
//
//@Component
//public class LocalIpService {
//
//    private final Set<byte[]> localIps = new HashSet<>();
//
//    @PostConstruct
//    public void init() {
//        try {
//            for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
//                if (!nif.isUp() || nif.isLoopback()) continue;
//
//                for (InetAddress addr : Collections.list(nif.getInetAddresses())) {
//                    byte[] raw = addr.getAddress();
//
//                    // Only IPv4 / IPv6
//                    if (raw.length == 4 || raw.length == 16) {
//                        localIps.add(raw);
//                    }
//                }
//            }
//
//            System.out.println("Discovered local IPs: " + localIps.size());
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to discover local IPs", e);
//        }
//    }
//
//    public Set<byte[]> getLocalIps() {
//        return localIps;
//    }
//}