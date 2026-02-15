package com.packetscope.desktop.persistence;

import com.packetscope.desktop.model.CapturedPacket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A thread-safe bounded buffer that holds captured packets before they are 
 * persisted to the database by the PersistenceWorker.
 */
public class PacketWriteQueue {

    private final BlockingQueue<CapturedPacket> queue;

    public PacketWriteQueue(int capacity) {
        // ArrayBlockingQueue is used to provide back-pressure and prevent OOM
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    /**
     * Attempts to insert a packet into the queue.
     * @return true if successful, false if the queue is full (packet dropped)
     */
    public boolean offer(CapturedPacket packet) {
        return queue.offer(packet);
    }

    public BlockingQueue<CapturedPacket> getQueue() {
        return queue;
    }
    
    public int size() {
        return queue.size();
    }

    public int remainingCapacity() {
        return queue.remainingCapacity();
    }
}
