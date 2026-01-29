package com.packetscope.desktop.persistence;

import com.packetscope.desktop.model.CapturedPacket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PacketWriteQueue {

    private final BlockingQueue<CapturedPacket> queue;

    public PacketWriteQueue(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public boolean offer(CapturedPacket packet) {
        return queue.offer(packet);
    }

    public BlockingQueue<CapturedPacket> getQueue() {
        return queue;
    }
}
