package org.web.core.processors;

import org.injection.core.data.ScopeInstance;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;
import org.web.WebContext;
import org.web.annotations.scopes.RequestScope;
import org.web.annotations.scopes.SessionScope;
import org.web.core.*;
import org.web.core.helpers.RequestParserImpl;
import org.web.core.helpers.RequestResolverImpl;
import org.web.data.MethodInfo;
import org.web.data.ParamInfo;
import org.web.data.ResponseWrapper;
import org.web.data.RouteHandler;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class HttpRequestProcessorImpl implements HttpRequestProcessor {
    private static final Log logger = Log.getInstance(HttpRequestProcessorImpl.class);
    private final WebContext webContext;
    private final RequestParser requestParser;
    private final RequestResolver requestResolver;

    public HttpRequestProcessorImpl(WebContext webContext){
        this(webContext, new RequestResolverImpl(), new RequestParserImpl());
    }

    public HttpRequestProcessorImpl(WebContext webContext, RequestResolver requestResolver, RequestParser requestParser){
        if(webContext==null)
            webContext = WebContext.init();
        this.webContext = webContext;
        this.requestParser = requestParser;
        this.requestResolver = requestResolver;
    }

    /**
     * Invoking route method from controller
     * @param request
     * @param response
     * @throws IOException
     */
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseWrapper responseWrapper;
        String httpMethod = request.getMethod();
        String uri = request.getPathInfo();
        RouteHandler routeHandler = requestResolver.resolveRouteHandler(request);
        if(routeHandler!=null){
            responseWrapper = executeRouteHandler(request, response, routeHandler);
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
            Set<String> similarRoutes = requestResolver.getRouteHandlers()
                    .keySet()
                    .stream()
                    .filter(str-> str.split(":").length>1 && str.split(":")[1].startsWith(uri))
                    .map(str-> "\n"+str)
                    .collect(Collectors.toSet());
            if(!similarRoutes.isEmpty())
                stringBuffer.append("Did you mean one of those routes: \n").append(similarRoutes);
            else{
                similarRoutes = requestResolver.getRouteHandlers().keySet().stream().map(str-> "\n"+str)
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
    public ResponseWrapper executeRouteHandler(HttpServletRequest request, HttpServletResponse response, RouteHandler routeHandler) throws IOException {
        logger.debugF("Execute Handler: %s \n", routeHandler);
        long startTime = System.currentTimeMillis();
        String requestMethod = routeHandler.getMethodHttp();
        String requestPath = routeHandler.getRootPath();
        MethodInfo methodInfo = routeHandler.getMethodInfo();
        Set<ParamInfo> paramInfo = methodInfo.getParamInfoSet();
        String handlerPath = routeHandler.getRootPath()+methodInfo.getPath();
        // method params
        logger.debugF("Method parameters info: %s \n",paramInfo);
        // extract parameters values
        Map<String, Object> parameters = requestParser.extractParametersRawValues(request, response, paramInfo, handlerPath);
        logger.debugF("Request parameters raw values: %s \n",parameters);
        // validate parameters
        boolean isParamsOk = requestParser.validateParameters(parameters, paramInfo);
        // invoke method
        Method method = routeHandler.getRouteMethod();
        Object controllerInstance = null;
        if(!Modifier.isStatic(method.getModifiers())){
            Class<?> methodDeclaringClass = method.getDeclaringClass();
            Object bean = getHandlerInstance(methodDeclaringClass, request);
            controllerInstance = methodDeclaringClass.cast(bean);
            if(controllerInstance==null)
                throw new FrameworkException("Can't get instance of controller "+methodDeclaringClass.getCanonicalName()+"");
        }
        Object result = callRouteMethod(method, controllerInstance, parameters);
        logger.debug("Execution duration ["+requestMethod+":"+requestPath+"] => "+(System.currentTimeMillis()-startTime)+" Millis");
        // return response
        return new ResponseWrapper(result);
    }

    /**
     * Calling method that represent route handler
     * @param method
     * @param routeHandlerInstance
     * @param parameters
     * @return
     */
    public Object callRouteMethod(Method method, Object routeHandlerInstance, Map<String, Object> parameters){
        try {
            logger.debugF("Invoke route method: %s, from controller: %s, with parameters: %s",
                    method.getName(),
                    method.getDeclaringClass().getCanonicalName(),
                    parameters);
            Object[] values = requestParser.formatParamsValues(method, parameters);
            logger.debugF("Parameters invoking values: %s", Arrays.stream(values).collect(Collectors.toList()));
            Object result = method.invoke(routeHandlerInstance, values);
            logger.debugF("Method return value: %s", result);
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

    /**
     * Get instance of controller
     * @param routeHandlerClass
     * @return
     */
    public Object getHandlerInstance(Class<?> routeHandlerClass, HttpServletRequest request){
        try {
            Set<ScopeInstance> scopeInstances = new HashSet<>();
            scopeInstances.add(new ScopeInstance(SessionScope.class, request.getSession()));
            scopeInstances.add(new ScopeInstance(RequestScope.class, request));
            Class<?> controllerScope = webContext
                    .getInjectionConfig()
                    .getScopeManager()
                    .getClassScope(routeHandlerClass);
            if(controllerScope==null)
                controllerScope = Singleton.class;
            Object bean = webContext
                    .initWebProvider(request.getSession(), request)
                    .getBeanInstance(routeHandlerClass, null, scopeInstances, controllerScope);
            return routeHandlerClass.cast(bean);
        }catch (Throwable throwable){
            logger.error("Can't get instance of route handler class : "+routeHandlerClass.getCanonicalName());
            throw new FrameworkException(throwable);
        }
    }
}
