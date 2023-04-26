package org.web;

import org.injection.DefaultProvider;
import org.injection.InjectionConfig;
import org.injection.core.providers.BeanProvider;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

public class WebContext {
    private static final Log logger = Log.getInstance(WebContext.class);
    private InjectionConfig injectionConfig;
    private BeanProvider beanProvider;

    private WebContext(){}

    public static WebContext init() {
        return init(InjectionConfig.getDefaultInstance());
    }

    public static WebContext init(InjectionConfig config) {
        if(config==null)
            throw new FrameworkException("Config in web provider can't be null");
        WebContext webContext = new WebContext();
        webContext.injectionConfig = config;
        webContext.beanProvider = DefaultProvider.init(config);
        return webContext;
    }

    public InjectionConfig getInjectionConfig() {
        return injectionConfig;
    }

    public WebProvider initWebProvider() {
        return new WebProvider(null, null, beanProvider);
    }

    public WebProvider initWebProvider(HttpSession session, ServletRequest request) {
        return new WebProvider(session, request, beanProvider);
    }

    public WebProvider initWebProvider(ServletRequest request) {
        return new WebProvider(null, request, beanProvider);
    }

    public WebProvider initWebProvider(HttpSession session) {
        return new WebProvider(session, beanProvider);
    }
}