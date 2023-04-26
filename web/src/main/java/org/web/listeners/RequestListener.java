package org.web.listeners;

import org.injection.core.scopes.ScopeLifeCycle;
import org.tools.Log;
import org.web.WebContext;
import org.web.WebProvider;
import org.web.annotations.scopes.RequestScope;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;

import static org.web.Constants.*;

public class RequestListener implements
        ScopeLifeCycle,
        ServletRequestListener,
        ServletRequestAttributeListener {
    private static final Log logger = Log.getInstance(RequestListener.class);

    @Override
    public void attributeAdded(ServletRequestAttributeEvent servletRequestAttributeEvent) {
        logger.info("RequestListener [attributeAdded] " +
                " Attribute: ["+servletRequestAttributeEvent.getName()+"] " +
                " Value: ["+servletRequestAttributeEvent.getValue()+"] ");
    }

    @Override
    public void attributeRemoved(ServletRequestAttributeEvent servletRequestAttributeEvent) {
        logger.info("RequestListener [attributeRemoved] " +
                " Request: ["+servletRequestAttributeEvent.getServletRequest().hashCode()+"] "+
                " Attribute: ["+servletRequestAttributeEvent.getName()+"] " +
                " Value: ["+servletRequestAttributeEvent.getValue()+"] ");
    }

    @Override
    public void attributeReplaced(ServletRequestAttributeEvent servletRequestAttributeEvent) {
        logger.info("RequestListener [attributeReplaced] " +
                " Request: ["+servletRequestAttributeEvent.getServletRequest().hashCode()+"] "+
                " Attribute: ["+servletRequestAttributeEvent.getName()+"] " +
                " Value: ["+servletRequestAttributeEvent.getValue()+"] ");
    }

    @Override
    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
        logger.info("RequestListener [requestDestroyed] " +
                " Request: ["+servletRequestEvent.getServletRequest().hashCode()+"] ");
        scopeDestroyed(servletRequestEvent.getServletRequest());
    }

    @Override
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        logger.info("RequestListener [requestInitialized] " +
                " Request: ["+servletRequestEvent.getServletRequest().hashCode()+"] ");
        ServletRequest request = servletRequestEvent.getServletRequest();
        scopeInitialized(request);
        ServletContext ctx = servletRequestEvent.getServletContext();
        WebContext webContext = (WebContext)ctx.getAttribute(WEB_CONTEXT);
        if(webContext == null){
            webContext = WebContext.init();
            ctx.setAttribute(WEB_CONTEXT, webContext);
            ctx.setAttribute(INJECTION_CONFIG, webContext.getInjectionConfig());
        }

        HttpSession session = null;
        if(request instanceof HttpServletRequest){
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            session = httpServletRequest.getSession();
        }
        WebProvider webProvider = webContext.initWebProvider(session, request);
        request.setAttribute(WEB_CONTEXT, webContext);
        request.setAttribute(REQUEST_WEB_PROVIDER, webProvider);
    }

    public Class<? extends Annotation> getScopeType(){
        return RequestScope.class;
    }
}
