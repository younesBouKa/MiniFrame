package org.web.servlets;

import org.tools.ClassFinder;
import org.tools.Log;
import org.web.WebConfig;
import org.web.WebProviderBuilder;
import org.web.WebProvider;
import org.web.core.request.HttpRequestProcessor;
import org.web.listeners.ContextListener;
import org.web.listeners.RequestListener;
import org.web.listeners.SessionListener;
import org.web.server.config.ServletConfig;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.web.Constants.*;

public class WebDispatcher extends HttpServlet {
    private static final Log logger = Log.getInstance(WebDispatcher.class);
    private HttpRequestProcessor httpRequestProcessor;
    private ExecutorService executorService;

    public void init() throws ServletException {
        logger.info("WebDispatcher init");
        // get init params
        logger.info("Web Dispatcher init params:");
        Enumeration paramNames = getServletConfig()
                .getInitParameterNames();
        while (paramNames.hasMoreElements()){
            String paramName = (String) paramNames.nextElement();
            logger.info("Param name:["+paramName+"] , value:["+getInitParameter(paramName)+"]");
        }
        // add web app context to classpath
        Set<String> webAppPaths = Arrays
                .stream(((URLClassLoader) getClass().getClassLoader()).getURLs())
                .map(URL::getPath)
                .collect(Collectors.toSet());
        ClassFinder.addToClassPath(webAppPaths);
        // add web context to servlet context attributes
        ServletContext ctx = getServletContext();
        WebProviderBuilder webProviderBuilder = WebProviderBuilder.getInstance();
        ctx.setAttribute(WEB_PROVIDER_BUILDER, webProviderBuilder);
        ctx.setAttribute(INJECTION_CONFIG, webProviderBuilder.getInjectionConfig());
        // init http request processor with web context
        httpRequestProcessor = WebConfig.getHttpRequestProcessor();
        // init executor
        executorService = Executors.newCachedThreadPool();
        super.init();
    }

    public void updateRequestAttribute(HttpServletRequest request){
        ServletContext ctx = getServletContext();
        HttpSession session = request.getSession();
        WebProviderBuilder webProviderBuilder = (WebProviderBuilder) ctx.getAttribute(WEB_PROVIDER_BUILDER);
        if(webProviderBuilder == null){
            webProviderBuilder = WebProviderBuilder.getInstance();
            ctx.setAttribute(WEB_PROVIDER_BUILDER, webProviderBuilder);
            ctx.setAttribute(INJECTION_CONFIG, webProviderBuilder.getInjectionConfig());
        }
        WebProvider webProvider = webProviderBuilder.build(session, request);
        request.setAttribute(WEB_PROVIDER_BUILDER, webProviderBuilder);
        request.setAttribute(WEB_PROVIDER, webProvider);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        logger.info("New request received, RequestURI: "+req.getRequestURI());
        updateRequestAttribute(req);
        executorService.submit(()->{
            try {
                httpRequestProcessor.processRequest(req, resp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static ServletConfig getWebDispatcherServletConfig(){
        ServletConfig dispatcherServletConfig = new ServletConfig();
        dispatcherServletConfig.setServletName("WebDispatcher");
        dispatcherServletConfig.setServletClass(WebDispatcher.class.getCanonicalName());
        dispatcherServletConfig.setLoadOnStartup(1);
        dispatcherServletConfig.setUrlPattern("/api/*");
        return dispatcherServletConfig;
    }

    public static Set<String> getListenersClasses(){
        Set<String> listenerClasses = new HashSet<>();
        listenerClasses.add(ContextListener.class.getCanonicalName());
        listenerClasses.add(SessionListener.class.getCanonicalName());
        listenerClasses.add(RequestListener.class.getCanonicalName());
        return listenerClasses;
    }
}
