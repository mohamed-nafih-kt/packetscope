package com.packetscope;

import com.packetscope.http.TalkersHandler;
import com.packetscope.db.PacketQueryDao;
import com.packetscope.db.Db;
import com.packetscope.http.TimelineProtocolDirectionHandler;

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
//

//        server.createContext("/timeline", new TimelineHandler(db));

        server.createContext(
                "/timeline/protocol-direction",
                new TimelineProtocolDirectionHandler(dao)
        );

//        server.createcontext("/flows", new FlowHandler(db));
//        server.createContext("/talkers", new TalkersHandler(db));
//
//        Static files (index.html, flows.html, etc)
//        server.createContext("/", new StaticHandler("static"));
//
//        server.setExecutor(Executors.newFixedThreadPool(8));
//        server.start();

        System.out.println("Web listening on " +  server.getAddress().getHostName() + ":" + server.getAddress().getPort());
    }
}
