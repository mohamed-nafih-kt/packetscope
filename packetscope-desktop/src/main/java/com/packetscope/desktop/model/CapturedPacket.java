package com.packetscope.desktop.model;

import java.time.Instant;

public class CapturedPacket {

    public Instant timestamp;

    public int ipVersion;
    
    public byte[] sourceIp;        
    public byte[] destinationIp;   

    public TransportProtocol protocol;
    public Integer sourcePort;
    public Integer destinationPort;
    
    public int packetSize;
    public String interfaceName;
    
    public PacketDirection direction;
    
}
