package org.web;

import org.injection.DefaultProvider;
import org.injection.InjectionConfig;
import org.injection.core.providers.BeanProvider;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

public class WebProviderBuilder {
    private static final Log logger = Log.getInstance(WebProviderBuilder.class);
    private InjectionConfig injectionConfig;
    private BeanProvider beanProvider;

    private WebProviderBuilder(){}

    public static WebProviderBuilder getInstance() {
        return new WebProviderBuilder();
    }

    public WebProviderBuilder setInjectionConfig(InjectionConfig config) {
        if(config==null)
            throw new FrameworkException("Config in web provider can't be null");
        this.injectionConfig = config;
        return this;
    }
    public WebProviderBuilder setBeanProvider(BeanProvider beanProvider) {
        if(beanProvider==null)
            throw new FrameworkException("BeanProvider in web provider can't be null");
        this.beanProvider = beanProvider;
        return this;
    }
    public InjectionConfig getInjectionConfig() {
        return injectionConfig!=null ? injectionConfig : InjectionConfig.getDefaultInstance();
    }
    public BeanProvider getBeanProvider() {
        return beanProvider!=null ? beanProvider : DefaultProvider.init(getInjectionConfig());
    }

    public WebProvider build() {
        return build(null, null);
    }
    public WebProvider build(ServletRequest request) {
        return build(null, request);
    }
    public WebProvider build(HttpSession session) {
        return build(session, null);
    }
    public WebProvider build(HttpSession session, ServletRequest request) {
        return new WebProvider(session, request, getBeanProvider());
    }
}