package org.web.core.processors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.injection.annotations.qualifiers.markers.FirstFound;
import org.injection.core.data.ScopeInstance;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;
import org.web.WebConfig;
import org.web.WebContext;
import org.web.annotations.methods.Route;
import org.web.annotations.others.Controller;
import org.web.annotations.params.global.ParamSrc;
import org.web.annotations.scopes.RequestScope;
import org.web.core.ControllerConfig;
import org.web.core.MethodInfoBuilder;
import org.web.core.RouteExtractor;
import org.web.data.MethodInfo;
import org.web.data.ParamInfo;
import org.web.data.ResponseWrapper;
import org.web.data.RouteHandler;

import javax.inject.Singleton;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

public class RouteExtractorImpl implements RouteExtractor {
    private static final Log logger = Log.getInstance(RouteExtractorImpl.class);
    private final ControllerConfig Config;
    private final MethodInfoBuilder methodInfoBuilder;

    public RouteExtractorImpl(ControllerConfig controllerConfig, MethodInfoBuilder methodInfoBuilder){
        this.Config = controllerConfig;
        this.methodInfoBuilder = methodInfoBuilder;
    }

    public HashMap<String, HttpHandler> getHttpHandlers(Class controllerClass){
        logger.debugF("Searching for http handlers in : %s\n", controllerClass.getName());
        HashMap<String, Set<RouteHandler>> extractedRouteHandlersList = getRouteHandlers(controllerClass);
        HashMap<String, HttpHandler> httpHandlers = new HashMap<>();
        // prepare http handlers
        for(String routePath : extractedRouteHandlersList.keySet()){
            HttpHandler httpHandler;
            Set<RouteHandler> routeHandlerSet = extractedRouteHandlersList.get(routePath);
            if(routeHandlerSet.size()>1){
                httpHandler = mergeHttpHandlers(
                        routeHandlerSet,
                        routePath
                );
            }else{
                RouteHandler routeHandler = routeHandlerSet.stream().findFirst().orElse(null);
                httpHandler = prepareHttpHandler(routeHandler.getRouteMethod(),
                        routePath,
                        routeHandler.getMethodHttp(),
                        routeHandler.getMethodInfo().getParamInfoSet()
                );
            }
            httpHandlers.put(routePath, httpHandler);
        }
        return httpHandlers;
    }

    public HashMap<String, RouteHandler> getRouteHandlersV2(Class controllerClass){
        logger.debugF("Searching for route handlers in : %s\n", controllerClass.getName());
        HashMap<String, RouteHandler> routeHandlers = new HashMap<>();
        // extract route methods
        Controller annotation = (Controller) controllerClass.getAnnotation(Controller.class);
        String rootPath = annotation.root();
        // extract route handlers
        Arrays
                .stream(controllerClass.getMethods())
                .filter(aMethod-> isValidRouteHandler(aMethod))
                .forEach(aMethod->{
                    MethodInfo methodInfo = methodInfoBuilder.build(aMethod);
                    RouteHandler routeHandler = new RouteHandler();
                    routeHandler.setController(controllerClass);
                    routeHandler.setRouteMethod(aMethod);
                    routeHandler.setMethodInfo(methodInfo);
                    routeHandler.setRootPath(rootPath);
                    routeHandler.setMethodHttp(methodInfo.getHttpMethod());
                    String routePath = routeHandler.getRootPath()+routeHandler.getMethodInfo().getPath();
                    String routeKey = methodInfo.getHttpMethod()+":"+routePath;

                    logger.debugF(
                            "Route method => Http method: %s, path:%s, Controller: %s, Method name: %s\n",
                            methodInfo.getHttpMethod(), routePath,
                            routeHandler.getController().getName(), routeHandler.getRouteMethod().getName()
                    );

                    if(routeHandlers.containsKey(routeKey)){
                        throw new FrameworkException("Duplicate route path: "+ routeKey);
                    }
                    routeHandlers.put(routeKey, routeHandler);
                });
        if(routeHandlers.isEmpty())
            logger.warn(
                    "Controller class ["+controllerClass.getName()+"] "+
                            "has no method with those annotations: "+
                            Config.getRouteAnnotations()
            );
        return routeHandlers;
    }

    public HashMap<String, Set<RouteHandler>> getRouteHandlers(Class controllerClass){
        logger.debugF("Searching for route handlers in : %s\n", controllerClass.getName());
        HashMap<String, Set<RouteHandler>> routeHandlers = new HashMap<>();
        // extract route methods
        Controller annotation = (Controller) controllerClass.getAnnotation(Controller.class);
        String rootPath = annotation.root();
        // extract route handlers
         Arrays
                .stream(controllerClass.getMethods())
                .filter(aMethod-> isValidRouteHandler(aMethod))
                .forEach(aMethod->{
                    MethodInfo methodInfo = methodInfoBuilder.build(aMethod);
                    RouteHandler handler = new RouteHandler();
                    handler.setController(controllerClass);
                    handler.setRouteMethod(aMethod);
                    handler.setMethodInfo(methodInfo);
                    handler.setRootPath(rootPath);
                    handler.setMethodHttp(methodInfo.getHttpMethod());
                    String routePath = handler.getRootPath()+handler.getMethodInfo().getPath();
                    String routeKey = methodInfo.getHttpMethod()+":"+routePath;

                    logger.debugF(
                            "Route method => Http method: %s, path:%s, Controller: %s, Method name: %s\n",
                            methodInfo.getHttpMethod(), routePath,
                            handler.getController().getName(), handler.getRouteMethod().getName()
                    );

                    if(!routeHandlers.containsKey(routePath)){
                        routeHandlers.put(routePath, new HashSet<>());
                    }
                    routeHandlers
                            .get(routePath)
                            .add(handler);
                });
        if(routeHandlers.isEmpty())
            logger.warn(
                    "Controller class ["+controllerClass.getName()+"] "+
                            "has no method with those annotations: "+
                            Config.getRouteAnnotations()
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

    private HttpHandler prepareHttpHandler(Method method, String path, String httpMethod, Set<ParamInfo> methodParameters){
        logger.debugF("prepareHttpHandler: %s \n", method.toGenericString());
        return httpExchange -> {
            long startTime = System.currentTimeMillis();
            String requestMethod = httpExchange.getRequestMethod();
            String requestPath = httpExchange.getRequestURI().getPath();
            logger.debugF(
                    "New Request received, Method: %s, Path: %s\n",
                    requestMethod, requestPath
            );

            boolean isValid = requestPath.equals(path)
                    && requestMethod.equals(httpMethod);
            if(isValid){
                // method params
                logger.debugF("Method parameters: %s \n",methodParameters);
                // extract parameters
                HashMap<String, Object> parameters = extractRequestParameters(httpExchange, methodParameters);
                logger.debugF("Request parameters: %s \n",parameters);
                // validate parameters
                boolean isParamsOk = validateParameters(parameters, methodParameters);
                // invoke method
                Object result;
                try {
                    Object controllerObj = null;
                    if(!Modifier.isStatic(method.getModifiers())){
                        Class methodDeclaringClass = method.getDeclaringClass();
                        Object bean = getControllerInstance(methodDeclaringClass, httpExchange);
                        controllerObj = methodDeclaringClass.cast(bean);
                        if(controllerObj==null)
                            throw new FrameworkException("Can't get instance of controller "+methodDeclaringClass.getCanonicalName()+"");
                    }
                    result = method.invoke(controllerObj, parameters.values().toArray());
                    logger.debugF("Route method handler result: "+result);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    throw new RuntimeException(throwable);
                }
                // return response
                ResponseWrapper responseWrapper = new ResponseWrapper(result);
                httpExchange.sendResponseHeaders(200,responseWrapper.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(responseWrapper.getBytes());
                os.close();
            }
            else{
                logger.debugF(
                        "Request doesn't match any route definition, Method: %s, Path: %s\n",
                        requestMethod, requestPath
                );
                // return response
                httpExchange.sendResponseHeaders(400,0);
                OutputStream os = httpExchange.getResponseBody();
                os.write("Request invalid".getBytes()); // FIXME to fix later
                os.close();
            }
            logger.debug("Execution duration ["+requestMethod+":"+requestPath+"] => "+(System.currentTimeMillis()-startTime)+" Millis");
        };
    }

    /**
     * Get instance of controller
     * @param controllerClass
     * @param request
     * @return
     */
    public static Object getControllerInstance(Class<?> controllerClass, HttpExchange request){
        try {
            Set<Annotation> qualifiers = Collections.singleton(new FirstFound(){
                @Override
                public Class<? extends Annotation> annotationType() {
                    return FirstFound.class;
                }
            });
            Set<ScopeInstance> scopeInstances = new HashSet<>();
            scopeInstances.add(new ScopeInstance(RequestScope.class, request));
            Class<?> controllerScope = WebConfig
                    .getInjectionConfig()
                    .getScopeManager()
                    .getClassScope(controllerClass);
            if(controllerScope==null)
                controllerScope = Singleton.class;
            Object bean = WebContext
                    .init()
                    .initWebProvider()
                    .getBeanInstance(controllerClass, qualifiers, scopeInstances, controllerScope);
            return controllerClass.cast(bean);
        }catch (Throwable throwable){
            logger.error("Can't get instance of controller : "+controllerClass.getCanonicalName());
            throw new FrameworkException(throwable);
        }
    }

    private HttpHandler mergeHttpHandlers(Set<RouteHandler> routeHandlerList, String path){
        logger.debugF("Merge handlers with same route path: %s, %s\n", path,
                routeHandlerList.stream()
                        .map(routeHandler ->
                                routeHandler.getMethodHttp()+"/"+routeHandler.getController().getName()
                                        +"."+
                                        routeHandler.getRouteMethod().getName()
                        )
                        .collect(Collectors.toList())
        );
        return httpExchange -> {
            long startTime = System.currentTimeMillis();
            String requestMethod = httpExchange.getRequestMethod();
            String requestPath = httpExchange.getRequestURI().getPath();
            logger.debugF(
                    "New Request received, Method: %s, Path: %s\n",
                    requestMethod, requestPath
            );
            List<String> httpMethods = routeHandlerList
                    .stream()
                    .map(routeHandler -> routeHandler.getMethodHttp())
                    .collect(Collectors.toList());
            boolean isValid = path.equals(requestPath)
                    && httpMethods.contains(requestMethod);
            if(isValid){
                RouteHandler routeHandlerToUse = routeHandlerList
                        .stream()
                        .filter(h -> requestPath.equals(path) && requestMethod.equals(h.getMethodHttp()))
                        .findFirst()
                        .orElse(null);
                if(routeHandlerToUse!=null){
                    // method params
                    Method method = routeHandlerToUse.getRouteMethod();
                    Set<ParamInfo> methodParameters = routeHandlerToUse.getMethodInfo().getParamInfoSet();
                    logger.debugF("Method parameters: %s \n",methodParameters);
                    // extract parameters
                    HashMap<String, Object> parameters = extractRequestParameters(httpExchange, methodParameters);
                    logger.debugF("Request parameters: %s \n",parameters);
                    // validate parameters
                    boolean isParamsOk = validateParameters(parameters, methodParameters);
                    // prepare parameters values (saving order)
                    Object[] values = new Object[parameters.size()];
                    int i=0;
                    for(Parameter parameter :  method.getParameters()){
                        values[i] = parameters.get(parameter.getName());
                        i++;
                    }
                    // invoke method
                    Object result;
                    try {
                        Object controllerObj = null;
                        if(!Modifier.isStatic(method.getModifiers())){
                            Class methodDeclaringClass = method.getDeclaringClass();
                            Object bean = getControllerInstance(methodDeclaringClass, httpExchange);
                            controllerObj = methodDeclaringClass.cast(bean);
                            if(controllerObj==null)
                                throw new FrameworkException("Can't get instance of controller "+methodDeclaringClass.getCanonicalName()+"");
                        }
                        result = method.invoke(controllerObj, values);
                        logger.debugF("Route method handler result: "+result);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        throw new RuntimeException(throwable);
                    }
                    // return response
                    ResponseWrapper responseWrapper = new ResponseWrapper(result);
                    httpExchange.sendResponseHeaders(200,responseWrapper.length());
                    OutputStream os = httpExchange.getResponseBody();
                    os.write(responseWrapper.getBytes());
                    os.close();
                }else {
                    logger.errorF(
                            "No handler founded for this Method: %s, and Path: %s\n",
                            requestMethod, requestPath
                    );
                }
            }
            else{
                logger.errorF(
                        "Request doesn't match any route definition, Method: %s, Path: %s\n",
                        requestMethod, requestPath
                );
                // return response
                httpExchange.sendResponseHeaders(400,0);
                OutputStream os = httpExchange.getResponseBody();
                os.write("Request invalid".getBytes()); // FIXME to fix later
                os.close();
            }
            logger.debug("Execution duration ["+requestMethod+":"+requestPath+"] => "+(System.currentTimeMillis()-startTime)+" Millis");
        };
    }

    private HashMap<String, Object> extractRequestParameters(HttpExchange httpExchange, Set<ParamInfo> paramsInfo) throws IOException {
        HashMap<String, Object> allMixedParams = new HashMap<>();
        HashMap<String, Object> bodyParams = new HashMap<>();
        HashMap<String, Object> queryParams = new HashMap<>();
        HashMap<String, Object> pathParams = new HashMap<>();
        HashMap<String, Object> headerParams = new HashMap<>();
        URI requestedUri = httpExchange.getRequestURI();
        String query = requestedUri.getRawQuery();
        // get headers param
        Headers headers = httpExchange.getRequestHeaders();
        for (String headerName : headers.keySet())
            headerParams.put(headerName, headers.get(headerName));
        // get path params (FIXME to see later)
        //parseQuery(requestedUri.getRawQuery(), pathParams);
        // get params from query
        parseQuery(query, queryParams);
        // get params from body
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        query = br.readLine();
        parseQuery(query, bodyParams);
        // prepare params
        String paramName, paramUsedName;
        Object paramValue;
        for(ParamInfo methodParamInfo : paramsInfo) {
            paramName = methodParamInfo.getName(); // name or used name in annotation
            paramUsedName = methodParamInfo.getUsedName(); // name or used name in annotation
            paramValue = null;
            if (Config.isInjectableParam(methodParamInfo.getType())){
                paramValue = getInjectedParam(httpExchange, methodParamInfo);
            }else if(ParamSrc.PATH.equals(methodParamInfo.getParamType())){
                paramValue = pathParams.get(paramUsedName);
            }else if(ParamSrc.QUERY.equals(methodParamInfo.getParamType())){
                paramValue = queryParams.get(paramUsedName);
            }else if(ParamSrc.BODY.equals(methodParamInfo.getParamType())){
                paramValue = bodyParams.get(paramUsedName);
            }else if(ParamSrc.HEADER.equals(methodParamInfo.getParamType())){
                paramValue = headerParams.get(paramUsedName);
            }
            allMixedParams.put(paramName, paramValue);
        }
        logger.debugF("extractRequestParameters paramsInfo: %s , parameters: %s \n",paramsInfo, allMixedParams );
        return allMixedParams;
    }

    private static Object getInjectedParam(HttpExchange httpExchange, ParamInfo methodParamInfo) {
        if(methodParamInfo.getType().isAssignableFrom(HttpExchange.class))
            return methodParamInfo.getType().cast(httpExchange);
        if(methodParamInfo.getType().isAssignableFrom(HttpContext.class))
            return methodParamInfo.getType().cast(httpExchange.getHttpContext());
        if(methodParamInfo.getType().isAssignableFrom(Principal.class))
            return methodParamInfo.getType().cast(httpExchange.getPrincipal());
        return null;
    }

    private static boolean validateParameters(Map<String, Object> parametersValues, Set<ParamInfo> paramsInfo){
        // FIXME to see later using validation annotations (see if we can use spec)
        return true;
    }

    private static void parseQuery(String query, Map<String,Object> parameters) throws UnsupportedEncodingException {
        if (query != null) {
            String pairs[] = query.split("[&]");
            for (String pair : pairs) {
                String param[] = pair.split("[=]");
                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0],
                            System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1],
                            System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);

                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }
}

