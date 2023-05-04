package org.web;

import org.tools.ClassFinder;
import org.tools.Log;
import org.web.core.WebRequestProcessor;

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
import java.util.Set;
import java.util.stream.Collectors;

import static org.web.Constants.*;

public class WebDispatcher extends HttpServlet {
    private static final Log logger = Log.getInstance(WebDispatcher.class);
    private WebRequestProcessor webRequestProcessor;
    private WebContext webContext;

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
        webContext = WebContext.init();
        ctx.setAttribute(WEB_CONTEXT, webContext);
        ctx.setAttribute(INJECTION_CONFIG, webContext.getInjectionConfig());
        // init Web request processor with web context
        webRequestProcessor = new WebRequestProcessor(webContext);
        super.init();
    }

    public void updateRequestAttribute(HttpServletRequest request){
        ServletContext ctx = getServletContext();
        HttpSession session = request.getSession();
        WebContext webContext = (WebContext) ctx.getAttribute(WEB_CONTEXT);
        if(webContext == null){
            webContext = WebContext.init();
            ctx.setAttribute(WEB_CONTEXT, webContext);
            ctx.setAttribute(INJECTION_CONFIG, webContext.getInjectionConfig());
        }
        WebProvider webProvider = webContext.initWebProvider(session, request);
        request.setAttribute(WEB_CONTEXT, webContext);
        request.setAttribute(REQUEST_WEB_PROVIDER, webProvider);
    }

    /**
     * Calling handler executor
     * @param req
     * @param resp
     * @throws IOException
     */
    public void callHandler(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("New request received, RequestURI: "+req.getRequestURI());
        updateRequestAttribute(req);
        webRequestProcessor.call(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        callHandler(req, resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        callHandler(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        callHandler(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        callHandler(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        callHandler(req, resp);
    }

}
