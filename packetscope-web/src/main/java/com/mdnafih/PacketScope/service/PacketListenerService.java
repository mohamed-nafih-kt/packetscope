//package com.mdnafih.PacketScope.service;
//
//
//import org.pcap4j.core.*;
//import org.pcap4j.util.NifSelector;
//import org.springframework.stereotype.Service;
//
//import java.net.InetAddress;
//
//@Service
//public class PacketListenerService {
//    InetAddress addr;
//    PcapNetworkInterface device = null;
//
//    public PacketListenerService() {
//        try {
//            addr = InetAddress.getByName("10.227.178.25");
//            device = new NifSelector().selectNetworkInterface();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println("You chose: " + device);
//    }
//}
