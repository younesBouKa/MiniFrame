package org.web;

import org.injection.core.data.ScopeInstance;
import org.injection.core.providers.BeanProvider;
import org.tools.Log;
import org.tools.proxy.ProxyHandler;
import org.web.annotations.scopes.RequestScope;
import org.web.annotations.scopes.SessionScope;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

public class WebProvider implements BeanProvider{
    private static final Log logger = Log.getInstance(WebProvider.class);
    private ServletRequest request;
    private HttpSession session;
    private BeanProvider beanProvider;

    public WebProvider(HttpSession session, ServletRequest request, BeanProvider beanProvider){
        this.request = request;
        this.beanProvider = beanProvider;
        this.session = session;
    }

    public WebProvider(HttpSession session, BeanProvider beanProvider){
        this.request = null;
        this.session = session;
        this.beanProvider = beanProvider;
    }

    public ServletRequest getRequest() {
        return request;
    }
    public void setRequest(ServletRequest request) {this.request = request;}
    public HttpSession getSession() {return session;}
    public void setSession(HttpSession session){this.session = session;}
    public BeanProvider getBeanProvider() {return beanProvider;}
    public void setBeanProvider(BeanProvider beanProvider) {this.beanProvider = beanProvider;}

    public <T> T getBeanInstance(Class<T> beanType, Class<?> beanScopeType){
        return getBeanInstance(beanType, null, null, beanScopeType);
    }

    public <T> T getBeanInstance(Class<T> beanType, Class<?> beanScopeType, Class<? extends Annotation> markerQualifier) {
        Set<Annotation> qualifiers = new HashSet<>();
        qualifiers.add(()->markerQualifier);
        return (T) this.getBeanInstance(beanType, qualifiers, (Set)null, beanScopeType);
    }

    @Override
    public <T> T getBeanInstance(Class<T> beanType, Set<Annotation> qualifiers, Set<ScopeInstance> scopes, Class<?> beanScopeType) {
        if(getRequest()==null)
            logger.warn("Request is null in WebProvider");
        if(scopes == null)
            scopes = new HashSet<>();
        if(getRequest()!=null){
            scopes.add(new ScopeInstance(RequestScope.class, getRequest()));
        }

        if(getSession()!=null){
            scopes.add(new ScopeInstance(SessionScope.class, getSession()));
        }
        Object bean = beanProvider.getBeanInstance(beanType, qualifiers, scopes, beanScopeType);
        Object beanProxy = ProxyHandler.newInstance(
                bean,
                (proxy, method, args)-> {
                    logger.info("Before calling bean method: "+method.getName());
                },
                ((proxy, method, args, returnVal) -> {
                    logger.info("After calling bean method: "+method.getName());
                })
        );
        return beanType.cast(bean);
    }
}