package org.injection;

import org.injection.core.providers.BeanProvider;
import org.injection.core.data.ScopeInstance;
import org.injection.core.providers.BeanProviderImpl;
import org.tools.proxy.ProxyHandler;
import org.tools.Log;

import java.lang.annotation.Annotation;
import java.util.Set;

public class DefaultProvider implements BeanProvider{
    private static final Log logger = Log.getInstance(DefaultProvider.class);
    private InjectionConfig injectionConfig;
    private BeanProvider beanProvider;

    protected DefaultProvider(){}

    public static DefaultProvider init() {
        return init(InjectionConfig.getDefaultInstance());
    }

    public static DefaultProvider init(InjectionConfig injectionConfig) {
        DefaultProvider defaultProvider = new DefaultProvider();
        defaultProvider.injectionConfig = injectionConfig;
        defaultProvider.beanProvider = new BeanProviderImpl(
                injectionConfig.getBeanContainer(),
                injectionConfig.getBeanScanManager(),
                injectionConfig.getBeanQualifierManager(),
                injectionConfig.getScopeManager(),
                injectionConfig.getBeanLifeCycle()
        );
        return defaultProvider;
    }

    @Override
    public <T> T getBeanInstance(Class<T> beanType, Set<Annotation> qualifiers, Set<ScopeInstance> scopes, Class<?> beanScopeType) {
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
