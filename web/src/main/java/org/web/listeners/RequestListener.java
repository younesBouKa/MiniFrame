package org.web.listeners;

import org.injection.core.scopes.ScopeLifeCycle;
import org.tools.Log;
import org.web.WebProvider;
import org.web.WebProviderBuilder;
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
        WebProviderBuilder webProviderBuilder = (WebProviderBuilder)ctx.getAttribute(WEB_PROVIDER_BUILDER);
        if(webProviderBuilder == null){
            webProviderBuilder = WebProviderBuilder.getInstance();
            ctx.setAttribute(WEB_PROVIDER_BUILDER, webProviderBuilder);
            ctx.setAttribute(INJECTION_CONFIG, webProviderBuilder.getInjectionConfig());
        }

        HttpSession session = null;
        if(request instanceof HttpServletRequest){
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            session = httpServletRequest.getSession();
        }
        WebProvider webProvider = webProviderBuilder.build(session, request);
        request.setAttribute(WEB_PROVIDER_BUILDER, webProviderBuilder);
        request.setAttribute(WEB_PROVIDER, webProvider);
    }

    public Class<? extends Annotation> getScopeType(){
        return RequestScope.class;
    }
}
