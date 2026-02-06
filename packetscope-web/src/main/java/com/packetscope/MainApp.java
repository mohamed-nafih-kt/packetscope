package com.packetscope;

import com.packetscope.http.*;
import com.packetscope.db.PacketQueryDao;
import com.packetscope.db.Db;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.sql.Connection;

public final class MainApp{

    public static void main(String[] args) throws Exception {
        System.out.println("--- Starting PacketScope Capture Engine ---");

        // ---- DB ----
        Db db = new Db();
        PacketQueryDao dao = new PacketQueryDao(db);

        try (Connection c = db.get()) {
            System.out.println("Database connected.");
        }
//
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("Web listening on " +  server.getAddress().getHostName() + ":" + server.getAddress().getPort());

        server.createContext("/", new StaticHandler("src/main/resources/"));
        server.createContext("/timeline/protocol-direction", new TimelineProtocolDirectionHandler(dao));
        server.createContext("/flows", new FlowsHandler(dao));
        server.createContext("/talkers", new TalkersHandler(dao));
        server.createContext("/packets", new PacketsHandler(dao));
//
//        Static files (index.html, flows.html)
//        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }
}
