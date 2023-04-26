package org.injection.core.global;

import org.injection.core.data.BeanInstance;
import org.injection.core.listeners.ContainerLifeCycle;

import java.util.EventObject;
import java.util.Set;
import java.util.function.Predicate;

public interface BeanContainer {
    int defaultContainerSizeWarnThreshold = 200;
    int defaultContainerSizeErrorThreshold = 500;

    default int getWarnSizeThreshold(){
        return defaultContainerSizeWarnThreshold;
    }
    default int getErrorSizeThreshold(){
        return defaultContainerSizeErrorThreshold;
    }
    ContainerLifeCycle getContainerLifeCycle();
    void setContainerLifeCycle(ContainerLifeCycle containerLifeCycle);
    void onScopeEvent(EventObject eventObject);
    Set<BeanInstance> getBeansWithFilter(Predicate<BeanInstance> filter);
    Object getBean(Class<?> beanType, Class<?> scopeType, Object scopeId);
    void addBean(Class<?> beanType, Object bean, Class<?> scopeType, Object scopeId, Object source);
    boolean removeBean(Class<?> beanType, Object bean, Object scopeId);
    int removeBeans(Class<?> scopeType, Object scopeId);
}
