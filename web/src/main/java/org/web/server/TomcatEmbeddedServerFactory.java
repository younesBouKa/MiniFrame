package org.web.server;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;
import org.web.server.config.FilterConfig;
import org.web.server.config.ServerConfig;
import org.web.server.config.ServletConfig;
import org.web.servlets.DemoServlet;
import org.web.servlets.StaticServlet;
import org.web.servlets.WebDispatcher;

import javax.servlet.ServletException;
import java.io.File;

public class TomcatEmbeddedServerFactory implements EmbeddedServerFactory {
    private static final Log logger = Log.getInstance(TomcatEmbeddedServerFactory.class);
    private Tomcat server;
    private Context context;
    private ServerConfig config;
    private boolean initialized;

    @Override
    public void init() {
        server = new Tomcat();
    }

    @Override
    public void init(ServerConfig config){
        init();
        setup(config);
    }

    @Override
    public void setup(ServerConfig config) {
        this.config = config;
        setPort(config.getPort());
        try {
            initContext(config.getContextPath(), config.getDocBase());
        } catch (ServletException e) {
            throw new FrameworkException(e);
        }
        // add filters
        for (FilterConfig filterConfig : config.getFilterConfigList())
            addFilter(filterConfig);

        // add listeners
        for (String listenerClass : config.getListenerClasses())
            addListener(listenerClass);

        // add context parameters
        for(String paramName : config.getContextParams().keySet())
            addContextParam(paramName, config.getContextParams().get(paramName));

        // add servlets
        for (ServletConfig servletConfig : config.getServletConfigList())
            addServlet(servletConfig);

        // farther config (to see later)
        context.addErrorPage(new ErrorPage());
        context.addWelcomeFile(config.getWelcomeFile());
        context.setCookies(config.isCookies());
        context.setSessionTimeout(config.getSessionTimeOut());
        initialized = true;
    }

    @Override
    public void setPort(int port){
        server.setPort(port);
    }

    @Override
    public void initContext(String contextPath, String docBase) throws ServletException {
        context = server.addWebapp(contextPath, docBase);
        //context = server.addContext(contextPath, docBase);
    }

    @Override
    public void addFilter(FilterConfig filterConfig){
        FilterDef filterDef = getFilterDef(filterConfig);
        context.addFilterDef(filterDef);
        FilterMap filterMap = getFilterMap(filterConfig);
        context.addFilterMap(filterMap);
    }

    @Override
    public void removeFilter(FilterConfig filterConfig){
        FilterDef filterDef = getFilterDef(filterConfig);
        context.removeFilterDef(filterDef);
        FilterMap filterMap = getFilterMap(filterConfig);
        context.removeFilterMap(filterMap);
    }

    @Override
    public void addListener(String listenerClass){
        context.addApplicationListener(listenerClass);
    }

    @Override
    public void removeListener(String listenerClass){
        context.removeApplicationListener(listenerClass);
    }

    @Override
    public void addContextParam(String name, String value){
        context.addParameter(name, value);
    }

    @Override
    public void removeContextParam(String name){
        context.removeParameter(name);
    }

    @Override
    public void addServlet(ServletConfig servletConfig){
        Wrapper servletWrapper;
        if(servletConfig.getServletInstance()!=null)
            servletWrapper = server.addServlet(config.getContextPath(), servletConfig.getServletName(), servletConfig.getServletInstance());
        else
            servletWrapper = server.addServlet(config.getContextPath(), servletConfig.getServletName(), servletConfig.getServletClass());
        if(servletWrapper!=null){
            servletWrapper.setLoadOnStartup(servletConfig.getLoadOnStartup());
            for(String paramName : servletConfig.getInitParams().keySet())
                servletWrapper.addInitParameter(paramName, servletConfig.getInitParams().get(paramName));
            servletWrapper.setEnabled(servletConfig.isEnabled());
            servletWrapper.setMultipartConfigElement(servletConfig.getMultipartConfigElement());
            context.addServletMappingDecoded(servletConfig.getUrlPattern(), servletConfig.getServletName());
        }
    }

    @Override
    public void stop(){
        if(server==null)
            throw new FrameworkException("Server not yet initialized");

        try {
            server.stop();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addUser(String user, String password){
        if(server==null)
            throw new FrameworkException("Server not yet initialized");
        server.addUser(user, password);
    }

    @Override
    public void addRole(String user, String role){
        if(server==null)
            throw new FrameworkException("Server not yet initialized");
        server.addRole(user, role);
    }

    @Override
    public void destroy(){
        if(server==null)
            throw new FrameworkException("Server not yet initialized");
        try {
            server.destroy();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() {
        if(!initialized)
            throw new FrameworkException("Server not yet configured");
        try {
            server.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
        logger.info("Server started with success on port : "+config.getPort());
        server.getServer().await();
    }

    /*--------------------------------- helper methods-------------------------------------*/
    public static FilterDef getFilterDef(FilterConfig filterConfig){
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(filterConfig.getFilterName());
        filterDef.setFilterClass(filterConfig.getFilterClass());
        filterDef.setFilter(filterConfig.getFilter());
        filterDef.setDescription(filterConfig.getDescription());
        filterDef.setDisplayName(filterConfig.getDisplayName());
        filterDef.setAsyncSupported(filterConfig.getAsyncSupported());
        filterDef.setLargeIcon(filterConfig.getLargeIcon());
        filterDef.setSmallIcon(filterConfig.getSmallIcon());
        for(String name : filterConfig.getParameters().keySet())
            filterDef.addInitParameter(name, filterConfig.getParameters().get(name));
        return filterDef;
    }

    public static FilterMap getFilterMap(FilterConfig filterConfig){
        FilterMap filterMap = new FilterMap();
        filterMap.addURLPattern(filterConfig.getUrlPattern());
        filterMap.setFilterName(filterConfig.getFilterName());
        return filterMap;
    }

    public static void main(String[] args) {
        EmbeddedServerFactory embeddedServerFactory = new TomcatEmbeddedServerFactory();
        // create server config instance
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(8090);
        serverConfig.setContextPath("/webapp");
        serverConfig.setDocBase(new File("Demo/src/main/webapp/").getAbsolutePath());
        serverConfig.setListenerClasses(WebDispatcher.getListenersClasses());
        // add static resources servlet
        ServletConfig staticServletConfig = new ServletConfig();
        staticServletConfig.setServletName("StaticServlet");
        staticServletConfig.setServletClass(StaticServlet.class.getCanonicalName());
        staticServletConfig.setUrlPattern("/static/*");
        staticServletConfig.addInitParam(StaticServlet.PATH_PREFIX_PARAM_NAME, "/static");
        staticServletConfig.addInitParam(StaticServlet.STATIC_RESOURCE_FOLDER_PARAM_NAME, "/WEB-INF/static");
        serverConfig.addServletConfig(staticServletConfig);
        // add web dispatcher servlet
        ServletConfig dispatcherServletConfig = WebDispatcher.getWebDispatcherServletConfig();
        dispatcherServletConfig.setUrlPattern("/api/*");
        serverConfig.addServletConfig(dispatcherServletConfig);
        // add demo servlet
        ServletConfig demoServletConfig = DemoServlet.getDemoServletConfig();
        demoServletConfig.setUrlPattern("/demo/*");
        serverConfig.addServletConfig(demoServletConfig);
        // start server
        embeddedServerFactory.init(serverConfig);
        embeddedServerFactory.start();
    }
}
