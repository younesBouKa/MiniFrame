package org.demo.Server;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.tools.Log;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class Server {
    private static final Log logger = Log.getInstance(Server.class);
    private static HttpServer server;
    private static int port = 9000;
    private static Map<String, HttpHandler> handlers = new HashMap<>();
    private static List<Filter> filters = new ArrayList<>();

    public static void init(int port, Map<String, HttpHandler> httpHandlerMap) throws IOException {
        Server.port = port;
        logger.debug("Init server ...");
        server = HttpServer.create(new InetSocketAddress(port), 0);
        setHandlers(httpHandlerMap);
    }

    public static void start() {
        if(handlers.isEmpty()){
            logger.error("No handler was added to server");
            return;
        }
        logger.debug("Server is starting ...");
        addHandlers();
        server.setExecutor(null);
        server.start();
        logger.debug("Server started at " + port);
    }

    public static void setHandlers(Map<String, HttpHandler> httpHandlers){
        handlers = httpHandlers;
    }

    public static void setFilters(List<Filter> httpFilters){
        filters = httpFilters;
    }

    private static void addHandlers(){
        for (String path : handlers.keySet()) {
            HttpHandler handler = handlers.get(path);
            addHandler(path, handler);
        }
    }

    private static void addHandler(String path, HttpHandler httpHandler){
        logger.debugF("Adding Http handler for root: %s\n", path);;
        server
                .createContext(path, httpHandler)
                .getFilters()
                .addAll(filters);
    }

    public static int getPort() {
        return port;
    }

    public static Map<String, HttpHandler> getHandlers() {
        return handlers;
    }

    public static List<Filter> getFilters() {
        return filters;
    }
}


