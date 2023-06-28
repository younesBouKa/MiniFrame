package org.web.core;

import org.web.data.RouteHandler;

import java.lang.reflect.Method;
import java.util.Map;

public interface RouteExtractor extends AutoConfigurable{
    Map<String, RouteHandler> getRouteHandlers(Class<?> controllerClass);
    boolean isValidRouteHandler(Method method);
    default String generateRouteHandlerKey(RouteHandler routeHandler){
        String routePath = routeHandler.getRootPath()+routeHandler.getMethodInfo().getPath();
        return routeHandler.getMethodInfo().getHttpMethod()+":"+routePath;
    }
}

