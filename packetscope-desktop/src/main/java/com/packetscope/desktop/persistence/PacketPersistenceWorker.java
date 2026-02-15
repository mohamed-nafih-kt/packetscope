package com.packetscope.desktop.persistence;

import com.packetscope.desktop.model.CapturedPacket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PacketPersistenceWorker implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(PacketPersistenceWorker.class.getName());
    private static final int BATCH_SIZE = 200;

    private final BlockingQueue<CapturedPacket> queue;
    private final Connection connection;
    private volatile boolean running = true;

    public PacketPersistenceWorker(
            BlockingQueue<CapturedPacket> queue,
            Connection connection
    ) {
        this.queue = queue;
        this.connection = connection;
    }

    public void shutdown() {
        running = false;
    }

    @Override
    public void run() {
        try {
            while (running || !queue.isEmpty()) {
                List<CapturedPacket> batch = new ArrayList<>(BATCH_SIZE);
                queue.drainTo(batch, BATCH_SIZE);

                if (batch.isEmpty()) {
                    Thread.sleep(50);
                    continue;
                }

                writeBatch(batch);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.INFO, "Persistence worker interrupted, shutting down...");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Critical error in persistence worker", e);
        }
    }

    private void writeBatch(List<CapturedPacket> batch) throws Exception {

        String sql = """
            INSERT INTO packets
            (user_id, captured_at, ip_version, protocol,
             source_ip, destination_ip,
             source_port, destination_port,
             packet_size, interface_name, direction)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        boolean autoCommit = connection.getAutoCommit();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);

            for (CapturedPacket p : batch) {
                ps.setInt(1, 1); // Default user context
                ps.setObject(2, p.timestamp);
                ps.setInt(3, p.ipVersion);
                ps.setInt(4, p.protocol.getNumber());
                ps.setBytes(5, p.sourceIp);
                ps.setBytes(6, p.destinationIp);
                ps.setObject(7, p.sourcePort);
                ps.setObject(8, p.destinationPort);
                ps.setInt(9, p.packetSize);
                ps.setString(10, p.interfaceName);
                ps.setInt(11, p.direction.getCode());

                ps.addBatch();
            }

            ps.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }
}
