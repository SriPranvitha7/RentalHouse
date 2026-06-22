package com.housefinder;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        // Create server on port 4567
        HttpServer server = HttpServer.create(new InetSocketAddress(4567), 0);

        // Register our handler for /api/properties
        server.createContext("/api/properties", new PropertiesHandler());

        // Start server
        server.start();
        System.out.println("========================================");
        System.out.println("HouseFinder backend is RUNNING!");
        System.out.println("Test: http://localhost:4567/api/properties");
        System.out.println("========================================");
    }

    static class PropertiesHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // Add CORS headers so frontend can call this
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            String method = exchange.getRequestMethod();

            // Handle OPTIONS preflight
            if (method.equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            // Handle GET — fetch/search properties
            if (method.equalsIgnoreCase("GET")) {
                handleGet(exchange);
            }

            // Handle POST — add property
            else if (method.equalsIgnoreCase("POST")) {
                handlePost(exchange);
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            try {
                PropertyDAO dao = new PropertyDAO();

                // Parse query parameters e.g. ?location=hyd&type=2BHK&rent=5000-10000
                String query = exchange.getRequestURI().getQuery();
                Map<String, String> params = parseQuery(query);

                String location = params.getOrDefault("location", "");
                String type     = params.getOrDefault("type", "");
                String rentStr  = params.getOrDefault("rent", "");

                List<Property> list;

                if (location.isEmpty() && type.isEmpty() && rentStr.isEmpty()) {
                    list = dao.getAllProperties();
                } else {
                    int minRent = 0;
                    int maxRent = 999999;
                    if (!rentStr.isEmpty()) {
                        String[] parts = rentStr.split("-");
                        minRent = Integer.parseInt(parts[0]);
                        maxRent = Integer.parseInt(parts[1]);
                    }
                    list = dao.searchProperties(location, type, minRent, maxRent);
                }

                // Build JSON
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    json.append(list.get(i).toJSON());
                    if (i < list.size() - 1) json.append(",");
                }
                json.append("]");

                sendResponse(exchange, 200, json.toString());

            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            try {
                // Read POST body
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes());
                Map<String, String> params = parseQuery(body);

                String owner    = params.get("owner");
                String phone    = params.get("phone");
                String location = params.get("location");
                String address  = params.get("address");
                int    rent     = Integer.parseInt(params.get("rent"));
                String type     = params.get("type");
                double lat      = Double.parseDouble(params.get("lat"));
                double lng      = Double.parseDouble(params.get("lng"));

                Property p = new Property(owner, phone, location, address, rent, type, lat, lng);
                PropertyDAO dao = new PropertyDAO();
                boolean success = dao.addProperty(p);

                if (success) {
                    sendResponse(exchange, 201, "{\"status\":\"success\",\"message\":\"Property added\"}");
                } else {
                    sendResponse(exchange, 500, "{\"status\":\"error\",\"message\":\"Failed to add\"}");
                }

            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }
        }

        // Helper — send response
        private void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
            byte[] bytes = body.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }

        // Helper — parse query string into map
        private Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
            Map<String, String> map = new HashMap<>();
            if (query == null || query.isEmpty()) return map;
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    map.put(
                        URLDecoder.decode(kv[0], "UTF-8"),
                        URLDecoder.decode(kv[1], "UTF-8")
                    );
                }
            }
            return map;
        }
    }
}