package org.web.core;

import com.sun.net.httpserver.HttpHandler;
import org.web.data.RouteHandler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

public interface RouteExtractor {

    HashMap<String, HttpHandler> getHttpHandlers(Class controllerClass);
    HashMap<String, RouteHandler> getRouteHandlersV2(Class controllerClass);
    HashMap<String, Set<RouteHandler>> getRouteHandlers(Class controllerClass);
    boolean isValidRouteHandler(Method method);
}

