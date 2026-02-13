package com.packetscope.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticHandler implements HttpHandler {

    private final Path root;

    public StaticHandler(String dir) {
        this.root = Path.of(dir);
        System.out.println("Static root: " + root.toAbsolutePath());
    }

    @Override
    public void handle(HttpExchange ex) throws IOException{

        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/templates/index.html";

        Path file = root.resolve(path.substring(1));

        if (!Files.exists(file)) {
            ex.sendResponseHeaders(404, -1);
            return;
        }

        byte[] data = Files.readAllBytes(file);

        ex.getResponseHeaders().add("Content-Type", guessContentType(file));
        ex.sendResponseHeaders(200, data.length);
        ex.getResponseBody().write(data);
        ex.close();

    }

    private static String guessContentType(Path file) {
        String name = file.toString();
        if (name.endsWith(".html")) return "text/html";
        if (name.endsWith(".js")) return "application/javascript";
        if (name.endsWith(".css")) return "text/css";
        return "application/octet-stream";
    }

}