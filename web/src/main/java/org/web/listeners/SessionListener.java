package org.web.listeners;

import org.injection.core.scopes.ScopeLifeCycle;
import org.tools.Log;
import org.web.annotations.scopes.SessionScope;

import javax.servlet.http.*;
import java.lang.annotation.Annotation;

public class SessionListener implements
        ScopeLifeCycle,
        HttpSessionListener,
        HttpSessionAttributeListener,
        HttpSessionActivationListener {
    private static final Log logger = Log.getInstance(SessionListener.class);
    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        logger.info("SessionListener [sessionCreated] " +
                " SessionId: ["+httpSessionEvent.getSession().getId()+"] ");
        HttpSession session = httpSessionEvent.getSession();
        this.scopeInitialized(session);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        logger.info("SessionListener [sessionDestroyed] " +
                " SessionId: ["+httpSessionEvent.getSession().getId()+"] ");
        HttpSession session = httpSessionEvent.getSession();
        this.scopeDestroyed(session);
    }

    @Override
    public void sessionWillPassivate(HttpSessionEvent httpSessionEvent) {
        logger.info("SessionListener [sessionWillPassivate] " +
                " SessionId: ["+httpSessionEvent.getSession().getId()+"] ");
    }

    @Override
    public void sessionDidActivate(HttpSessionEvent httpSessionEvent) {
        logger.info("SessionListener [sessionDidActivate] " +
                " SessionId: ["+httpSessionEvent.getSession().getId()+"] ");
    }

    @Override
    public void attributeAdded(HttpSessionBindingEvent httpSessionBindingEvent) {
        logger.info("SessionListener [attributeAdded] " +
                " SessionId: ["+httpSessionBindingEvent.getSession().getId()+"] " +
                " Attribute: ["+httpSessionBindingEvent.getName()+"]" +
                " Value: ["+httpSessionBindingEvent.getValue()+"]");
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent httpSessionBindingEvent) {
        logger.info("SessionListener [attributeRemoved] " +
                " SessionId: ["+httpSessionBindingEvent.getSession().getId()+"] " +
                " Attribute: ["+httpSessionBindingEvent.getName()+"]" +
                " Value: ["+httpSessionBindingEvent.getValue()+"]");
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent httpSessionBindingEvent) {
        logger.info("SessionListener [attributeReplaced] " +
                " SessionId: ["+httpSessionBindingEvent.getSession().getId()+"] " +
                " Attribute: ["+httpSessionBindingEvent.getName()+"]" +
                " Value: ["+httpSessionBindingEvent.getValue()+"]");
    }

    public Class<? extends Annotation> getScopeType(){
        return SessionScope.class;
    }
}
