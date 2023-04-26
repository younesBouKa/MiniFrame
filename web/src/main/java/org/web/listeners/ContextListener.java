package org.web.listeners;

import org.tools.Log;
import org.web.WebContext;

import javax.servlet.*;

import static org.web.Constants.INJECTION_CONFIG;
import static org.web.Constants.WEB_CONTEXT;

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
        WebContext webContext;
        if(!(ctx.getAttribute(WEB_CONTEXT) instanceof WebContext)){
            webContext = WebContext.init();
            ctx.setAttribute(WEB_CONTEXT, webContext);
            ctx.setAttribute(INJECTION_CONFIG, webContext.getInjectionConfig());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("ContextListener contextDestroyed " +
                "["+servletContextEvent.getServletContext()+"]");
    }
}
