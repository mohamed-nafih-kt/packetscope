package com.packetscope.desktop.repository;

import com.packetscope.desktop.DbConnectionDev;
import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class PacketInserterDao {

    private static final String query = "INSERT INTO packets "
            + "(user_id, timestamp, source_ip, destination_ip, protocol, size, raw_data) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    public static void insertPacket(int userId, Timestamp timestamp,String srcIp, String dstIp, String protocol , Packet packet) {
        DbConnectionDev dbCon = new DbConnectionDev();
        try (Connection conn = dbCon.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setTimestamp(2, timestamp); 
            stmt.setString(3, srcIp);
            stmt.setString(4, dstIp);
            stmt.setString(5, protocol);
            stmt.setInt(6, packet.length());
            stmt.setBytes(7, packet.getRawData());

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
