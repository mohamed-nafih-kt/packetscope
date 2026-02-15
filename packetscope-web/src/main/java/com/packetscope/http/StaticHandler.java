package com.packetscope.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class StaticHandler implements HttpHandler {

    private static final Logger LOGGER  = Logger.getLogger(StaticHandler.class.getName());
    private final Path root;

    public StaticHandler() {
        Path devPath = Path.of("src/main/resources");
        if (Files.exists(devPath)) {
            this.root = devPath.toAbsolutePath().normalize();
        } else {
            this.root = Path.of(".").toAbsolutePath().normalize();
        }
        LOGGER.info("Static assets initialized at: " + root);
    }

    public StaticHandler(String dir) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
        LOGGER.info("Static assets root: " + root);
    }

    @Override
    public void handle(HttpExchange ex) throws IOException{
        String requestPath = ex.getRequestURI().getPath();

        // Default to index.html for root requests
        if (requestPath.equals("/")) {
            requestPath = "/templates/index.html";
        }

        // Normalize and resolve path safely
        Path file = root.resolve(requestPath.substring(1)).normalize();

        // --- SECURITY: Prevent Directory Traversal ---
        if (!file.startsWith(root)) {
            LOGGER.warning("Blocked potential path traversal attempt: " + requestPath);
            ex.sendResponseHeaders(403, -1);
            return;
        }

        if (!Files.exists(file) || Files.isDirectory(file)) {
            ex.sendResponseHeaders(404, -1);
            ex.close();
            return;
        }

        byte[] data = Files.readAllBytes(file);

        ex.getResponseHeaders().add("Content-Type", guessContentType(file));
        ex.sendResponseHeaders(200, data.length);
        try (ex; OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }

    }

    private static String guessContentType(Path file) {
        String name = file.toString().toLowerCase();
        if (name.endsWith(".html")) return "text/html";
        if (name.endsWith(".js"))   return "application/javascript";
        if (name.endsWith(".css"))  return "text/css";
        if (name.endsWith(".json")) return "application/json";
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".svg"))  return "image/svg+xml";
        return "application/octet-stream";
    }

}