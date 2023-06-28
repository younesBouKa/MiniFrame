package org.web.core.processors;

import org.injection.core.data.ScopeInstance;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;
import org.web.WebConfig;
import org.web.WebContext;
import org.web.annotations.scopes.RequestScope;
import org.web.annotations.scopes.SessionScope;
import org.web.core.HttpRequestProcessor;
import org.web.core.RequestParser;
import org.web.core.RequestResolver;
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
    private WebContext webContext;
    private RequestParser requestParser;
    private RequestResolver requestResolver;
    private Map<Class<?>, Object> controllersCache;

    public HttpRequestProcessorImpl(WebContext webContext, RequestResolver requestResolver, RequestParser requestParser){
        if(webContext==null)
            webContext = WebContext.init();
        this.webContext = webContext;
        controllersCache = new HashMap<>();
        this.requestParser = requestParser;
        this.requestResolver = requestResolver;
    }

    @Override
    public void autoConfigure() {
        this.webContext = WebContext.init();
        this.controllersCache = new HashMap<>();
        this.requestParser = WebConfig.getRequestParser();
        this.requestResolver = WebConfig.getRequestResolver();
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
        RouteHandler routeHandler = resolveRouteHandler(request);
        if(routeHandler!=null){
            responseWrapper = executeRouteHandler(request, response, routeHandler);
            response.setStatus(200);
            response.getOutputStream()
                    .write(responseWrapper.getBytes());
            // TODO for test only
            logger.info("Bean Container content after request: "+
                    webContext
                            .getInjectionConfig()
                            .getBeanContainer()
                            .getBeansWithFilter(beanInstance->true)
                            .size()
            );
        }else{
            StringBuilder stringBuffer = new StringBuilder();
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

    public RouteHandler resolveRouteHandler(HttpServletRequest request){
        return requestResolver.resolveRouteHandler(request);
    }

    /**
     * Get instance of controller
     * @param controllerClass
     * @return
     */
    public Object getControllerInstance(Class<?> controllerClass, HttpServletRequest request){
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
            if(Singleton.class.equals(controllerScope) && controllersCache!=null && controllersCache.containsKey(controllerClass))
                return controllerClass.cast(controllersCache.get(controllerClass));

            Object bean = webContext
                    .initWebProvider(request.getSession(), request)
                    .getBeanInstance(controllerClass, null, scopeInstances, controllerScope);
            if(Singleton.class.equals(controllerScope)){
                if(controllersCache==null)
                    controllersCache = new HashMap<>();
                controllersCache.put(controllerClass, bean);
            }
            return controllerClass.cast(bean);
        }catch (Throwable throwable){
            logger.error("Can't get instance of route handler class : "+controllerClass.getCanonicalName());
            throw new FrameworkException(throwable);
        }
    }

    public Map<String, Object> getHandlerArgs(HttpServletRequest request, HttpServletResponse response, RouteHandler routeHandler){
        MethodInfo methodInfo = routeHandler.getMethodInfo();
        Set<ParamInfo> paramInfo = methodInfo.getParamInfoSet();
        String handlerPath = routeHandler.getRootPath()+methodInfo.getPath();
        logger.debugF("Method parameters info: %s \n",paramInfo);
        try {
            return requestParser.extractParametersRawValues(request, response, paramInfo, handlerPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        Method method = routeHandler.getRouteMethod();
        // method params
        // extract request parameters values
        Map<String, Object> parameters = getHandlerArgs(request, response, routeHandler);
        logger.debugF("Request parameters raw values: %s \n",parameters);
        // get controller instance
        Object controllerInstance = null;
        if(!Modifier.isStatic(method.getModifiers())){
            Class<?> methodDeclaringClass = method.getDeclaringClass();
            Object bean = getControllerInstance(methodDeclaringClass, request);
            controllerInstance = methodDeclaringClass.cast(bean);
            if(controllerInstance==null)
                throw new FrameworkException("Can't get instance of controller "+methodDeclaringClass.getCanonicalName()+"");
        }
        // call handler method
        Object result = callRouteMethod(method, controllerInstance, parameters);
        logger.debug("Execution duration ["+routeHandler.getMethodHttp()+":"+routeHandler.getRootPath()+"] => "+(System.currentTimeMillis()-startTime)+" Millis");
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
    public Object callRouteMethod(Method method, Object controllerInstance, Map<String, Object> parameters){
        try {
            logger.debugF("Invoke route method: %s, from controller: %s, with parameters: %s",
                    method.getName(),
                    method.getDeclaringClass().getCanonicalName(),
                    parameters);
            Object[] methodArgs = requestParser.prepareMethodArgs(method.getParameters(), parameters);
            logger.debugF("Parameters invoking values: %s", Arrays.stream(methodArgs).collect(Collectors.toList()));
            Object result = method.invoke(controllerInstance, methodArgs);
            logger.debugF("Method return value: %s", result);
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

}
