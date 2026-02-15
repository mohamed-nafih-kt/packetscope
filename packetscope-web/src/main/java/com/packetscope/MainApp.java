package com.packetscope;

import com.packetscope.http.*;
import com.packetscope.db.PacketQueryDao;
import com.packetscope.db.Db;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MainApp{

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 8;

    public static void main(String[] args){
        try {
            // Initialize Persistence Layer
            Db db = new Db();
            PacketQueryDao dao = new PacketQueryDao(db);

            // Health Check
            try (Connection c = db.get()) {
                LOGGER.info("Database connectivity established.");
            }
            // Server Setup
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", new StaticHandler());
            server.createContext("/timeline/protocol-direction", new TimelineProtocolDirectionHandler(dao));
            server.createContext("/flows", new FlowsHandler(dao));
            server.createContext("/talkers", new TalkersHandler(dao));
            server.createContext("/packets", new PacketsHandler(dao));
            server.createContext("/api/transactions", new SocketHandler(dao));

    //        Static files (index.html, flows.html)
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            server.setExecutor(executor);

            // Shutdown logic
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LOGGER.info("Shutting down PacketScope Web...");
                server.stop(0);
                executor.shutdownNow();
            }));

            server.start();
            LOGGER.info("Web Dashboard active at http://localhost:" + PORT + "/");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start the Web Application", e);
            System.exit(1);
        }
    }
}
