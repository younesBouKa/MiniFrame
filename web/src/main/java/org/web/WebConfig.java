package org.web;

import org.injection.InjectionConfig;
import org.tools.exceptions.FrameworkException;
import org.web.core.*;
import org.web.core.helpers.*;
import org.web.core.processors.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class WebConfig {
    private static ControllerConfig controllerConfig;
    private static ControllerProcessor controllerProcessor;
    private static MethodInfoBuilder methodInfoBuilder;
    private static ParamInfoBuilder paramInfoBuilder;
    private static RouteExtractor routeExtractor;
    private static InjectionConfig injectionConfig;
    private static RequestParser requestParser;
    private static RequestResolver requestResolver;
    private static HttpRequestProcessor httpRequestProcessor;

    public static RequestParser getRequestParser() {
        if(requestParser==null)
            requestParser = new RequestParserImpl();
        return requestParser;
    }

    public static RequestResolver getRequestResolver(){
        if(requestResolver==null)
            requestResolver = new RequestResolverImpl();
        return requestResolver;
    }

    public static HttpRequestProcessor getHttpRequestProcessor(){
        if(httpRequestProcessor==null)
            httpRequestProcessor = new HttpRequestProcessorImpl(
                    null,
                    getRequestResolver(),
                    getRequestParser()
            );
        return httpRequestProcessor;
    }

    public static InjectionConfig getInjectionConfig() {
        if(injectionConfig==null)
            injectionConfig = InjectionConfig
                    .getDefaultInstance();
        return injectionConfig;
    }

    public static ControllerConfig getControllerConfig() {
        if(controllerConfig==null)
            controllerConfig = new ControllerConfigImpl();
        return controllerConfig;
    }
    public static ParamInfoBuilder getParamInfoBuilder(ControllerConfig controllerConfig) {
        if(paramInfoBuilder==null){
            if(controllerConfig==null)
                controllerConfig = getControllerConfig();
            paramInfoBuilder = new ParamInfoBuilderImpl(controllerConfig);
        }
        return paramInfoBuilder;
    }
    public static MethodInfoBuilder getMethodInfoBuilder(ControllerConfig controllerConfig) {
        if(methodInfoBuilder==null){
            if(controllerConfig==null)
                controllerConfig = getControllerConfig();
            methodInfoBuilder = new MethodInfoBuilderImpl(
                    controllerConfig,
                    getParamInfoBuilder(controllerConfig)
            );
        }
        return methodInfoBuilder;
    }
    public static RouteExtractor getRouteExtractor(ControllerConfig controllerConfig) {
        if(routeExtractor==null){
            if(controllerConfig==null)
                controllerConfig = getControllerConfig();
            routeExtractor = new RouteExtractorImpl(
                    controllerConfig,
                    getMethodInfoBuilder(controllerConfig)
            );
        }
        return routeExtractor;
    }
    public static ControllerProcessor getControllerProcessor(ControllerConfig controllerConfig) {
        if(controllerProcessor==null){
            if(controllerConfig==null)
                controllerConfig = getControllerConfig();
            controllerProcessor = new ControllerProcessorImpl(
                    getInjectionConfig()
                            .getClassPool(),
                    getRouteExtractor(controllerConfig)
            );
        }
        return controllerProcessor;
    }

    /*---------------------- Setters -----------------------------------------*/
    public static void setHttpRequestProcessor(HttpRequestProcessor httpRequestProcessor){
        if(httpRequestProcessor==null)
            throw new FrameworkException("HttpRequestProcessor can't be null");
        WebConfig.httpRequestProcessor = httpRequestProcessor;
    }

    public static void setRequestParser(RequestParser requestParser) {
        if(requestParser==null)
            throw new FrameworkException("RequestParser can't be null");
        WebConfig.requestParser = requestParser;
    }

    public static void setRequestResolver(RequestResolver requestResolver){
        if(requestResolver==null)
            throw new FrameworkException("RequestResolver can't be null");
        WebConfig.requestResolver = requestResolver;
    }

    public static void setInjectionConfig(InjectionConfig injectionConfig) {
        if(injectionConfig==null)
            throw new FrameworkException("Injection config can't be null");
        WebConfig.injectionConfig = injectionConfig;
    }

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

    public static void loadConfigFromFile(String filePath){
        if(filePath==null || filePath.trim().isEmpty())
            filePath = "web-config.properties";
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream stream = loader.getResourceAsStream(filePath)){
            properties.load(stream);
            String implementationName = properties.getProperty(ControllerConfig.class.getCanonicalName());
            /*
             private static ControllerConfig controllerConfig;
             private static ControllerProcessor controllerProcessor;
             private static MethodInfoBuilder methodInfoBuilder;
             private static ParamInfoBuilder paramInfoBuilder;
             private static RouteExtractor routeExtractor;
             private static InjectionConfig injectionConfig;
             */

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
