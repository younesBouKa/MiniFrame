package org.web;

import org.injection.InjectionConfig;
import org.tools.exceptions.FrameworkException;
import org.web.core.*;
import org.web.core.helpers.ControllerConfigImpl;
import org.web.core.helpers.MethodInfoBuilderImpl;
import org.web.core.helpers.ParamInfoBuilderImpl;
import org.web.core.processors.*;

public class WebConfig {
    private static ControllerConfig controllerConfig;
    private static ControllerProcessor controllerProcessor;
    private static MethodInfoBuilder methodInfoBuilder;
    private static ParamInfoBuilder paramInfoBuilder;
    private static RouteExtractor routeExtractor;
    private static InjectionConfig injectionConfig;

    public static InjectionConfig getInjectionConfig() {
        if(injectionConfig==null)
            injectionConfig = InjectionConfig
                    .getDefaultInstance();
        return injectionConfig;
    }

    public static void setInjectionConfig(InjectionConfig injectionConfig) {
        if(injectionConfig==null)
            throw new FrameworkException("Injection config can't be null");
        WebConfig.injectionConfig = injectionConfig;
    }

    public static ControllerConfig getControllerConfig() {
        if(controllerConfig==null)
            controllerConfig = new ControllerConfigImpl();
        return controllerConfig;
    }
    public static ParamInfoBuilder getParamInfoBuilder() {
        if(paramInfoBuilder==null)
            paramInfoBuilder = new ParamInfoBuilderImpl(
                    getControllerConfig()
            );
        return paramInfoBuilder;
    }
    public static MethodInfoBuilder getMethodInfoBuilder() {
        if(methodInfoBuilder==null)
            methodInfoBuilder = new MethodInfoBuilderImpl(
                    getControllerConfig(),
                    getParamInfoBuilder()
            );
        return methodInfoBuilder;
    }
    public static RouteExtractor getRouteExtractor() {
        if(routeExtractor==null)
            routeExtractor = new RouteExtractorImpl(
                    getControllerConfig(),
                    getMethodInfoBuilder()
            );
        return routeExtractor;
    }
    public static ControllerProcessor getControllerProcessor() {
        if(controllerProcessor==null){
            controllerProcessor = new ControllerProcessorImpl(
                    getInjectionConfig()
                            .getClassPool(),
                    getRouteExtractor()
            );
        }
        return controllerProcessor;
    }

    /*---------------------- Setters -----------------------------------------*/
    public static void setControllerConfig(ControllerConfig controllerConfig) {
        if(controllerConfig==null)
            throw new FrameworkException("Controller config can't be null");
        WebConfig.controllerConfig = controllerConfig;
    }
    public static void setControllerProcessor(ControllerProcessor controllerProcessor) {
        if(controllerConfig==null)
            throw new FrameworkException("Controller Processor can't be null");
        WebConfig.controllerProcessor = controllerProcessor;
    }
    public static void setMethodInfoBuilder(MethodInfoBuilder methodInfoBuilder) {
        if(controllerConfig==null)
            throw new FrameworkException("Method Info Builder can't be null");
        WebConfig.methodInfoBuilder = methodInfoBuilder;
    }
    public static void setParamInfoBuilder(ParamInfoBuilder paramInfoBuilder) {
        if(controllerConfig==null)
            throw new FrameworkException("Param Info Builder can't be null");
        WebConfig.paramInfoBuilder = paramInfoBuilder;
    }
    public static void setRouteExtractor(RouteExtractor routeExtractor) {
        if(routeExtractor==null)
            throw new FrameworkException("Route Extractor can't be null");
        WebConfig.routeExtractor = routeExtractor;
    }
}
