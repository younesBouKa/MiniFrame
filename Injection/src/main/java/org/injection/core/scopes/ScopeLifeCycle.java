package org.injection.core.scopes;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;

public interface ScopeLifeCycle {

    Map<Class<? extends Annotation>, Set<Object>> cache = new HashMap<>();
    Set<Consumer<EventObject>> listeners = new HashSet<>();

    default EventObject buildEventObject(Class<? extends Annotation> scopeType, Object scopeInstance, LifeCycleEventType eventType){
        return new EventObject(new DefaultScopeLifeCycleEvent(scopeType, scopeInstance, eventType));
    }

    static boolean registerListener(Consumer<EventObject> listener){
        return listeners.add(listener);
    }
    static boolean removeListener(Consumer<EventObject> listener){
        return listeners.remove(listener);
    }

    default Set<Consumer<EventObject>> callListeners(Class<? extends Annotation> scopeType, Object scopeInstance, LifeCycleEventType eventType){
        Set<Consumer<EventObject>> calledListeners = new HashSet<>();
        if(scopeType!=null){
            EventObject eventObject = buildEventObject(scopeType, scopeInstance, eventType);
            for (Consumer<EventObject> existingListener : listeners){
                try {
                    existingListener.accept(eventObject);
                    calledListeners.add(existingListener);
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        }
        return calledListeners;
    }

    Class<? extends Annotation> getScopeType();

    static Map<Class<? extends Annotation>, Set<Object>> getCache(){
        return cache;
    }

    static boolean scopeExists(Class<? extends Annotation> scopeType, Object instance){
        return scopeTypeExists(scopeType) && cache
                .get(scopeType)
                .contains(instance);
    }

    static boolean scopeTypeExists(Class<? extends Annotation> scopeType){
        return cache.containsKey(scopeType);
    }

    default boolean scopeInitialized(Object instance) {
        Class<? extends Annotation> scopeType = getScopeType();
        if(scopeType!=null && instance!=null && !cache.containsKey(scopeType))
            cache.put(scopeType, new HashSet<>());
        boolean addedToCache = cache.get(scopeType).add(instance);
        if(addedToCache)
            callListeners(scopeType, instance, LifeCycleEventType.CREATED);
        return addedToCache;
    }

    default boolean scopeDestroyed(Object instance) {
        Class<? extends Annotation> scopeType = getScopeType();
        boolean deletedFromCache = false;
        if(scopeType!=null && instance!=null && cache.containsKey(scopeType)){
            deletedFromCache = cache.get(scopeType).remove(instance);
        }
        if(deletedFromCache)
            callListeners(scopeType, instance, LifeCycleEventType.DESTROYED);
        return deletedFromCache;
    }
}
