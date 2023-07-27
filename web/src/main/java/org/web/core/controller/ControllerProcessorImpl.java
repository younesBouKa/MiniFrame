package org.web.core.controller;

import org.injection.core.global.ClassPool;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;
import org.web.WebConfig;
import org.web.core.config.ControllerConfig;
import org.web.core.method.RouteExtractor;
import org.web.data.RouteHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ControllerProcessorImpl implements ControllerProcessor {
    private static final Log logger = Log.getInstance(ControllerProcessorImpl.class);
    private RouteExtractor routeExtractor;
    private ClassPool classPool;

    public ControllerProcessorImpl(ClassPool classPool, RouteExtractor routeExtractor){
        this.routeExtractor = routeExtractor;
        this.classPool = classPool;
    }

    @Override
    public void autoConfigure() {
        ControllerConfig controllerConfig = WebConfig.getControllerConfig();
        this.routeExtractor = WebConfig.getRouteExtractor(controllerConfig);
        this.classPool = WebConfig.getInjectionConfig().getClassPool();
    }

    public Map<String, RouteHandler> getRouteHandlers(){
        logger.debugF("Start Scanning for controllers in packages : %s\n", classPool.getPackagesToScan());
        HashMap<String, RouteHandler> handlers = new HashMap<>();
        Set<Class> foundedControllers = classPool.getClassesWithClassFilter(this::isValidControllerClass);
        logger.debugF("Founded controllers : %s\n", new ArrayList<>(foundedControllers));
        foundedControllers.forEach(aClass -> {
            Map<String, RouteHandler> routeHandlers = routeExtractor.getRouteHandlers(aClass);
            for (String path: routeHandlers.keySet()){
                if(handlers.containsKey(path))
                    throw new FrameworkException("Route ["+path+"] is duplicated in controller: "+aClass.getName());
            }
            handlers.putAll(routeHandlers);
        });
        return handlers;
    }
}

