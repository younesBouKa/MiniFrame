package org.injection.core.listeners;

public interface BeanLifeCycle extends BeanLifeCycleEventType{

    default void onEvent(String eventType, Object... args){

    }
}
