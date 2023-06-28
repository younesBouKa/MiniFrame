package org.web.core;

import org.web.data.ResponseWrapper;
import org.web.data.RouteHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

public interface HttpRequestProcessor extends AutoConfigurable{

    /**
     * Invoking route method from controller
     * @param request
     * @param response
     * @throws IOException
     */
    void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException;

    RouteHandler resolveRouteHandler(HttpServletRequest request);

    /**
     * Get instance of controller
     * @param routeHandlerClass
     * @return
     */
    Object getControllerInstance(Class<?> routeHandlerClass, HttpServletRequest request);

    Map<String, Object> getHandlerArgs(HttpServletRequest request, HttpServletResponse response, RouteHandler routeHandler);

    /**
     * Prepare parameters,
     * validate values,
     * getting instance of controller class
     * calling route method
     * finally wrapping result
     * @param request
     * @param response
     * @param routeHandler
     * @return
     * @throws IOException
     */
    ResponseWrapper executeRouteHandler(HttpServletRequest request, HttpServletResponse response, RouteHandler routeHandler) throws IOException;

    /**
     * Calling method that represent route handler
     * @param method
     * @param routeHandlerInstance
     * @param parameters
     * @return
     */
    Object callRouteMethod(Method method, Object routeHandlerInstance, Map<String, Object> parameters);
}
