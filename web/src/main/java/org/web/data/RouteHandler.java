package org.web.data;


import java.lang.reflect.Method;

public class RouteHandler {
    private String methodHttp;
    private String rootPath;
    private Class<?> controller;
    private Method routeMethod;
    private MethodInfo methodInfo;

    public String getMethodHttp() {
        return methodHttp;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public Class<?> getController() {
        return controller;
    }

    public void setController(Class<?> controller) {
        this.controller = controller;
    }

    public Method getRouteMethod() {
        return routeMethod;
    }

    public void setRouteMethod(Method routeMethod) {
        this.routeMethod = routeMethod;
    }

    public MethodInfo getMethodInfo() {
        return methodInfo;
    }

    public void setMethodInfo(MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
    }

    public void setMethodHttp(String methodHttp) {
        this.methodHttp = methodHttp;
    }

    @Override
    public String toString() {
        return "RouteHandler{" +
                "methodHttp='" + methodHttp + '\'' +
                ", rootPath='" + rootPath + '\'' +
                ", controller=" + controller +
                ", routeMethod=" + routeMethod +
                ", methodInfo=" + methodInfo +
                '}';
    }
}
