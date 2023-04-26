package org.web.core.processors;

import com.sun.net.httpserver.HttpHandler;
import org.injection.core.global.ClassPool;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;
import org.web.core.ControllerProcessor;
import org.web.core.RouteExtractor;
import org.web.data.RouteHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ControllerProcessorImpl implements ControllerProcessor {
    private static final Log logger = Log.getInstance(ControllerProcessorImpl.class);
    private final RouteExtractor routeProcessor;
    private final ClassPool classPool;

    public ControllerProcessorImpl(ClassPool classPool, RouteExtractor routeExtractor){
        this.routeProcessor = routeExtractor;
        this.classPool = classPool;
    }

    public Map<String, HttpHandler> getHttpHandlers(){
        logger.debugF("Start Scanning for controllers in packages : %s\n", classPool.getPackagesToScan());
        HashMap<String, HttpHandler> handlers = new HashMap<>();
        Set<Class> foundedControllers = classPool
                .getClassesWithClassFilter(this::isValidControllerClass);
        logger.debugF("Founded controllers : %s\n", new ArrayList<>(foundedControllers));
        foundedControllers.forEach(aClass -> {
                    HashMap<String, HttpHandler> routeHandlers = routeProcessor.getHttpHandlers(aClass);
                    for (String path: routeHandlers.keySet()){
                        if(handlers.containsKey(path))
                            throw new FrameworkException("Route ["+path+"] is duplicated in controller: "+aClass.getName());
                    }
                    handlers.putAll(routeHandlers);
                });
        return handlers;
    }

    public Map<String, RouteHandler> getRouteHandlers(){
        logger.debugF("Start Scanning for controllers in packages : %s\n", classPool.getPackagesToScan());
        HashMap<String, RouteHandler> handlers = new HashMap<>();
        Set<Class> foundedControllers = classPool.getClassesWithClassFilter(this::isValidControllerClass);
        logger.debugF("Founded controllers : %s\n", new ArrayList<>(foundedControllers));
        foundedControllers.forEach(aClass -> {
            Map<String, RouteHandler> routeHandlers = routeProcessor.getRouteHandlersV2(aClass);
            for (String path: routeHandlers.keySet()){
                if(handlers.containsKey(path))
                    throw new FrameworkException("Route ["+path+"] is duplicated in controller: "+aClass.getName());
            }
            handlers.putAll(routeHandlers);
        });
        return handlers;
    }
}

