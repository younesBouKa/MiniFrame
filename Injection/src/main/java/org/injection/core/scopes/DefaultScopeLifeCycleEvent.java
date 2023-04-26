package org.injection.core.scopes;

import java.lang.annotation.Annotation;

public class DefaultScopeLifeCycleEvent {
    private Class<? extends Annotation> scopeType;
    private Object scopeInstance;
    private LifeCycleEventType eventType;

    public DefaultScopeLifeCycleEvent(Class<? extends Annotation> scopeType, Object scopeInstance, LifeCycleEventType eventType) {
        this.scopeType = scopeType;
        this.scopeInstance = scopeInstance;
        this.eventType = eventType;
    }

    public Class<? extends Annotation> getScopeType() {
        return scopeType;
    }

    public void setScopeType(Class<? extends Annotation> scopeType) {
        this.scopeType = scopeType;
    }

    public Object getScopeInstance() {
        return scopeInstance;
    }

    public void setScopeInstance(Object scopeInstance) {
        this.scopeInstance = scopeInstance;
    }

    public LifeCycleEventType getEventType() {
        return eventType;
    }

    public void setEventType(LifeCycleEventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "DefaultScopeLifeCycleEvent{" +
                "scopeType=" + scopeType +
                ", scopeInstance=" + scopeInstance +
                ", eventType=" + eventType +
                '}';
    }
}
