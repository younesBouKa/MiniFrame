package org.web.core.method;

import org.tools.Log;
import org.tools.annotations.AnnotationTools;
import org.tools.exceptions.FrameworkException;
import org.web.WebConfig;
import org.web.annotations.methods.Route;
import org.web.annotations.others.Controller;
import org.web.core.config.ControllerConfig;
import org.web.data.MethodInfo;
import org.web.data.RouteHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RouteExtractorImpl implements RouteExtractor {
    private static final Log logger = Log.getInstance(RouteExtractorImpl.class);
    private ControllerConfig controllerConfig;
    private MethodInfoBuilder methodInfoBuilder;

    public RouteExtractorImpl(ControllerConfig controllerConfig, MethodInfoBuilder methodInfoBuilder){
        this.controllerConfig = controllerConfig;
        this.methodInfoBuilder = methodInfoBuilder;
    }

    @Override
    public void autoConfigure() {
        this.controllerConfig = WebConfig.getControllerConfig();
        this.methodInfoBuilder = WebConfig.getMethodInfoBuilder(this.controllerConfig);
    }

    public Map<String, RouteHandler> getRouteHandlers(Class<?> controllerClass){
        logger.debugF("Searching for route handlers in : %s\n", controllerClass.getName());
        Map<String, RouteHandler> routeHandlers = new HashMap<>();
        // extract route methods
        Controller annotation = (Controller) AnnotationTools.getAnnotation(controllerClass, Controller.class);
        String rootPath = annotation.root();
        // extract route handlers
        Arrays
                .stream(controllerClass.getMethods())
                .filter(this::isValidRouteHandler)
                .forEach(aMethod->{
                    MethodInfo methodInfo = methodInfoBuilder.build(aMethod);
                    RouteHandler routeHandler = new RouteHandler();
                    routeHandler.setController(controllerClass);
                    routeHandler.setRouteMethod(aMethod);
                    routeHandler.setMethodInfo(methodInfo);
                    routeHandler.setRootPath(rootPath);
                    routeHandler.setMethodHttp(methodInfo.getHttpMethod());

                    logger.debugF("New Route handler => %s\n",routeHandler);
                    String routeKey = generateRouteHandlerKey(routeHandler);

                    if(routeHandlers.containsKey(routeKey)){
                        throw new FrameworkException("Duplicate route path: "+ routeKey);
                    }
                    routeHandlers.put(routeKey, routeHandler);
                });
        if(routeHandlers.isEmpty())
            logger.warn(
                    "Controller class ["+controllerClass.getName()+"] "+
                            "has no method with those annotations: "+
                            controllerConfig.getRouteAnnotations()
            );
        return routeHandlers;
    }

    public boolean isValidRouteHandler(Method method){
        Annotation[] annotations = method.getAnnotations();
        boolean isValid = false;
        for(Annotation annotation: annotations){// FIXME try to make it generic
            if(annotation instanceof Route){
                Route routeAnnotation = (Route) annotation;
                isValid = routeAnnotation.route()!=null
                        && !routeAnnotation.route().isEmpty()
                        && routeAnnotation.method()!=null
                        && !routeAnnotation.method().name().isEmpty();
                break;
            }else {
                Class annotationType = annotation.annotationType();
                if(annotationType.isAnnotationPresent(Route.class)){
                    try {
                        Method routeMethod = annotationType.getMethod("route", new Class[]{});
                        String route = (String) routeMethod.invoke(annotation, new Object[]{});
                        String httpMethod = ((Route) annotationType.getAnnotation(Route.class))
                                .method().name();
                        isValid = route!=null
                                && !route.isEmpty()
                                && httpMethod!=null
                                && !httpMethod.isEmpty();
                        break;
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            /*
            if(annotation instanceof POST){
                isValid = ((POST) annotation).route()!=null && !((POST) annotation).route().isEmpty();
                break;
            }

            if(annotation instanceof GET){
                isValid = ((GET) annotation).route()!=null && !((GET) annotation).route().isEmpty();
                break;
            }

             */
        }
        return isValid;
    }

}

