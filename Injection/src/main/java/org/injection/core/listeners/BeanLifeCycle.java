package org.injection.core.listeners;

public interface BeanLifeCycle extends BeanLifeCycleEvent{

    default void onEvent(String eventType, Object... args){

    }
}
