package org.web.listeners;

import org.tools.Log;
import org.web.WebProviderBuilder;

import javax.servlet.*;

import static org.web.Constants.*;

public class ContextListener implements ServletContextListener, ServletContextAttributeListener {
    private static final Log logger = Log.getInstance(ContextListener.class);

    @Override
    public void attributeAdded(ServletContextAttributeEvent servletContextAttributeEvent) {
        logger.info("ContextListener attributeAdded " +
                "Attribute: ["+servletContextAttributeEvent.getName()+"] " +
                "Value: ["+servletContextAttributeEvent.getValue()+"]");
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent servletContextAttributeEvent) {
        logger.info("ContextListener attributeRemoved " +
                "Attribute: ["+servletContextAttributeEvent.getName()+"] " +
                "Value: ["+servletContextAttributeEvent.getValue()+"]");
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent servletContextAttributeEvent) {
        logger.info("ContextListener attributeReplaced " +
                "Attribute: ["+servletContextAttributeEvent.getName()+"] " +
                "Value: ["+servletContextAttributeEvent.getValue()+"]");
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("ContextListener contextInitialized " +
                "["+servletContextEvent.getServletContext()+"]");
        ServletContext ctx = servletContextEvent.getServletContext();
        WebProviderBuilder webProviderBuilder;
        if(!(ctx.getAttribute(WEB_PROVIDER_BUILDER) instanceof WebProviderBuilder)){
            webProviderBuilder = WebProviderBuilder.getInstance();
            ctx.setAttribute(WEB_PROVIDER_BUILDER, webProviderBuilder);
            ctx.setAttribute(INJECTION_CONFIG, webProviderBuilder.getInjectionConfig());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("ContextListener contextDestroyed " +
                "["+servletContextEvent.getServletContext()+"]");
    }
}
