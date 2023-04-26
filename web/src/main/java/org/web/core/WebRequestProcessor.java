package org.web.core;

import org.injection.core.data.ScopeInstance;
import org.tools.ClassFinder;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;
import org.web.WebConfig;
import org.web.WebContext;
import org.web.annotations.params.global.ParamSrc;
import org.web.annotations.scopes.RequestScope;
import org.web.annotations.scopes.SessionScope;
import org.web.data.MethodInfo;
import org.web.data.ParamInfo;
import org.web.data.ResponseWrapper;
import org.web.data.RouteHandler;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class WebRequestProcessor {
    // TODO a lot of work here, i should split this functionality
    private static final Log logger = Log.getInstance(WebRequestProcessor.class);
    private Map<String, RouteHandler> routeHandlerMap = new HashMap<>();
    private long lastUpdateTimeStamp = 0;
    private long updateCount = 0;
    private final WebContext webContext;

    public WebRequestProcessor(WebContext webContext){
        if(webContext==null)
            webContext = WebContext.init();
        this.webContext = webContext;
        update(true);
    }

    public void update(boolean force){
        long lastUpdate = ClassFinder.getLastUpdateTimeStamp();
        if (lastUpdateTimeStamp < lastUpdate && ClassFinder.isInitialized() || force) {
            updateCount++;
            lastUpdateTimeStamp = lastUpdate;
            prepareHandlers();
        }
    }

    public Map<String, RouteHandler> getHandlers(){
        update(false);
        return routeHandlerMap;
    }

    /**
     * Scanning project for controllers
     */
    public void prepareHandlers(){
        logger.debug("Scanning for route handlers for the ["+updateCount+"] time(s) ... ");
        long duration = System.currentTimeMillis();
        routeHandlerMap = WebConfig
                .getControllerProcessor()
                .getRouteHandlers();
        logger.debug("Scanning for route handlers ended in ["+(System.currentTimeMillis() - duration)+"] Millis " +
                "with "+ routeHandlerMap.size() +" handler(s)");
        if(routeHandlerMap.isEmpty())
            logger.error(
                    "No controller found in project"
            );
    }

    /**
     * Invoking route method from controller
     * @param request
     * @param response
     * @throws IOException
     */
    public void call(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseWrapper responseWrapper;
        String httpMethod = request.getMethod();
        String uri = request.getPathInfo();
        String handlerKey = httpMethod+":"+uri;
        RouteHandler routeHandler = getHandlers().getOrDefault(handlerKey, null);
        if(routeHandler!=null){
            responseWrapper = executeHandler(request, response, routeHandler);
            response.setStatus(200);
            response.getOutputStream()
                    .write(responseWrapper.getBytes());
            // TODO just for test
            logger.info("Bean Container content after request: "+
                    webContext
                            .getInjectionConfig()
                            .getBeanContainer()
                            .getBeansWithFilter(beanInstance->true)
            );
        }else{
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer
                    .append("Route not found [").append(httpMethod).append(":").append(uri).append("]\n");
            Set<String> similarRoutes = routeHandlerMap
                    .keySet()
                    .stream()
                    .filter(str-> str.split(":").length>1 && str.split(":")[1].startsWith(uri))
                    .map(str-> "\n"+str)
                    .collect(Collectors.toSet());
            if(!similarRoutes.isEmpty())
                stringBuffer.append("Did you mean one of those routes: \n").append(similarRoutes);
            else{
                similarRoutes = routeHandlerMap.keySet().stream().map(str-> "\n"+str)
                        .collect(Collectors.toSet());
                stringBuffer.append("Available routes: ").append(similarRoutes);
            }
            response.setStatus(404);
            response.getOutputStream()
                    .write(stringBuffer.toString().getBytes());
        }
    }

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
    private ResponseWrapper executeHandler(HttpServletRequest request, HttpServletResponse response, RouteHandler routeHandler) throws IOException {
        logger.debugF("Execute Handler: %s \n", routeHandler);
        long startTime = System.currentTimeMillis();
        String requestMethod = routeHandler.getMethodHttp();
        String requestPath = routeHandler.getRootPath();
        MethodInfo methodInfo = routeHandler.getMethodInfo();
        Set<ParamInfo> paramInfo = methodInfo.getParamInfoSet();
        // method params
        logger.debugF("Method parameters info: %s \n",paramInfo);
        // extract parameters values
        Map<String, Object> parameters = extractParametersRawValues(request, response, paramInfo);
        logger.debugF("Request parameters raw values: %s \n",parameters);
        // validate parameters
        boolean isParamsOk = validateParameters(parameters, paramInfo);
        // invoke method
        Method method = routeHandler.getRouteMethod();
        Object controllerInstance = null;
        if(!Modifier.isStatic(method.getModifiers())){
            Class<?> methodDeclaringClass = method.getDeclaringClass();
            Object bean = getControllerInstance(methodDeclaringClass, request);
            controllerInstance = methodDeclaringClass.cast(bean);
            if(controllerInstance==null)
                throw new FrameworkException("Can't get instance of controller "+methodDeclaringClass.getCanonicalName()+"");
        }
        Object result = callMethod(method, controllerInstance, parameters);
        logger.debug("Execution duration ["+requestMethod+":"+requestPath+"] => "+(System.currentTimeMillis()-startTime)+" Millis");
        // return response
        return new ResponseWrapper(result);
    }

    /**
     * Calling method that represent route handler
     * @param method
     * @param controllerInstance
     * @param parameters
     * @return
     */
    private Object callMethod(Method method, Object controllerInstance, Map<String, Object> parameters){
        try {
            logger.debugF("Invoke route method: %s, from controller: %s, with parameters: %s",
                    method.getName(),
                    method.getDeclaringClass().getCanonicalName(),
                    parameters);
            Object[] values = getFormattedParamsValues(method, parameters);
            logger.debugF("Parameters invoking values: %s", Arrays.stream(values).collect(Collectors.toList()));
            Object result = method.invoke(controllerInstance, values);
            logger.debugF("Method return value: %s", result);
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

    /**
     * Get instance of controller
     * @param controllerClass
     * @return
     */
    private Object getControllerInstance(Class<?> controllerClass, HttpServletRequest request){
        try {
            Set<ScopeInstance> scopeInstances = new HashSet<>();
            scopeInstances.add(new ScopeInstance(SessionScope.class, request.getSession()));
            scopeInstances.add(new ScopeInstance(RequestScope.class, request));
            Class<?> controllerScope = webContext
                    .getInjectionConfig()
                    .getScopeManager()
                    .getClassScope(controllerClass);
            if(controllerScope==null)
                controllerScope = Singleton.class;
            Object bean = webContext
                    .initWebProvider(request.getSession(), request)
                    .getBeanInstance(controllerClass, null, scopeInstances, controllerScope);
            return controllerClass.cast(bean);
        }catch (Throwable throwable){
            logger.error("Can't get instance of controller : "+controllerClass.getCanonicalName());
            throw new FrameworkException(throwable);
        }
    }

    /**
     * This method controle parameters and assure parameters order
     * @param method
     * @param parameters
     * @return
     */
    private static Object[] getFormattedParamsValues(Method method, Map<String, Object> parameters){
        Parameter[] methodParameters = method.getParameters();
        if(methodParameters.length!= parameters.size()){
            logger.error("Extracted parameters are not valid\n" +
                    "Method parameters: "+Arrays.asList(methodParameters)+"\n" +
                    "Extracted parameters: "+parameters);
            throw new FrameworkException("Extracted parameters are not valid");
        }
        Object[] values = new Object[methodParameters.length];
        int i=0;
        for(Parameter parameter : methodParameters){
            String paramName = parameter.getName();
            if(!parameters.containsKey(paramName))
                throw new FrameworkException("Can't find value for parameter ["+ parameter +"]");
            values[i++] = WebConfig
                    .getControllerConfig()
                    .getFormattedValue(parameters.get(paramName), parameter.getType());
        }
        return values;
    }

    /**
     * Extract parameters values from query, body, header and path
     * @param request
     * @param response
     * @param paramsInfo
     * @return
     * @throws IOException
     */
    private static Map<String, Object> extractParametersRawValues(HttpServletRequest request, HttpServletResponse response, Set<ParamInfo> paramsInfo) throws IOException {
        Map<String, Object> bodyParams = getBodyParams(request);
        Map<String, Object> queryParams = getQueryParams(request);
        Map<String, Object> pathParams = getPathParams(request);
        Map<String, Object> headerParams = getHeaderParams(request);
        // prepare params
        Map<String, Object> neededParams = new HashMap<>();
        String paramName, paramUsedName;
        Object paramValue;
        for(ParamInfo methodParamInfo : paramsInfo) {
            paramName = methodParamInfo.getName(); // name or used name in annotation
            paramUsedName = methodParamInfo.getUsedName(); // name or used name in annotation
            paramValue = WebConfig
                    .getControllerConfig()
                    .getRouteInjectedParamValue(request, response, methodParamInfo.getType());
            if(ParamSrc.PATH.equals(methodParamInfo.getParamType())){
                paramValue = pathParams.get(paramUsedName);
            }else if(ParamSrc.QUERY.equals(methodParamInfo.getParamType())){
                paramValue = queryParams.get(paramUsedName);
            }else if(ParamSrc.BODY.equals(methodParamInfo.getParamType())){
                paramValue = bodyParams.get(paramUsedName);
            }else if(ParamSrc.HEADER.equals(methodParamInfo.getParamType())){
                paramValue = headerParams.get(paramUsedName);
            }else if(paramValue==null){
                logger.error("Unknown parameter type: "+methodParamInfo);
            }
            neededParams.put(paramName, paramValue);
        }
        logger.debugF("Extracted Request Parameters: \n" +
                "paramsInfo: %s,\n" +
                "extracted parameters: %s\n",paramsInfo, neededParams );
        return neededParams;
    }

    private static Map<String, Object> getQueryParams(HttpServletRequest request) throws UnsupportedEncodingException {
        Map<String, Object> queryParams = new HashMap<>();
        String query = request.getQueryString();
        // get params from query
        parseQuery(query, queryParams);
        return queryParams;
    }

    private static Map<String, Object> getBodyParams(HttpServletRequest request) throws IOException {
        Map<String, Object> bodyParams = new HashMap<>();
        // get params from body (FIXME to see later)
        InputStreamReader isr = new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String body = br.readLine();
        parseQuery(body, bodyParams);
        return bodyParams;
    }

    private static Map<String, Object> getHeaderParams(HttpServletRequest request){
        Map<String, Object> headerParams = new HashMap<>();
        // get headers param
        Enumeration headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String headerName = (String) headerNames.nextElement();
            headerParams.put(headerName, request.getHeader(headerName));
        }
        return headerParams;
    }

    private static Map<String, Object> getPathParams(HttpServletRequest request){
        Map<String, Object> pathParams = new HashMap<>();
        // get path params (FIXME to see later)
        //parseQuery(requestedUri.getRawQuery(), pathParams);
        return pathParams;
    }

    /**
     * Validate parameters TODO not yet implemented
     * @param parametersValues
     * @param paramsInfo
     * @return
     */
    private static boolean validateParameters(Map<String, Object> parametersValues, Set<ParamInfo> paramsInfo){
        // FIXME to see later using validation annotations (see if we can use spec)
        return true;
    }

    /**
     * Parse query to get passed values
     * @param query
     * @param parameters
     * @throws UnsupportedEncodingException
     */
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
