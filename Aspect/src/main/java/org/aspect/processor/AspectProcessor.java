package org.aspect.processor;

import org.aspect.annotations.InitAspect;
import org.aspect.scanners.AspectScanManager;
import org.tools.annotations.AnnotationTools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface AspectProcessor {
    Map<Class<?>, Object> aspectInstancesCache = new Hashtable<>();
    default Object getAspectInstance(Class<?> aspectClass){
        // get from cache if exists
        if(aspectInstancesCache.containsKey(aspectClass))
            return aspectInstancesCache.get(aspectClass);
        // create new instance
        Constructor<?>[] constructors = aspectClass.getConstructors();
        Constructor<?> constructor = Arrays.stream(constructors)
                .filter(constructor1 -> constructor1.getParameterCount()==0)
                .findFirst()
                .orElse(null);
        if(constructor == null && constructors.length > 0)
            throw new RuntimeException("Aspect class "+aspectClass.getCanonicalName()+" doesn't have any constructor with no parameters");
        Object aspectInstance = null;
        try {
            if(constructor!=null){
                aspectInstance = constructor.newInstance();
                initAspectInstance(aspectClass, aspectInstance);
            }
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        // add instance to cache if not null
        if(aspectInstance!=null)
            aspectInstancesCache.put(aspectClass, aspectInstance);
        return aspectInstance;
    }
    default void initAspectInstance(Class<?> aspectClass, Object aspectInstance){
        List<Method> candidateInitMethods = Arrays.stream(aspectClass.getDeclaredMethods())
                .filter(method -> AnnotationTools.isAnnotationPresent(method, InitAspect.class))
                .collect(Collectors.toList());
        List<Method> initMethodWithNoParams = candidateInitMethods
                .stream()
                .filter(method -> method.getParameterCount()==0)
                .collect(Collectors.toList());
        Method initMethod = initMethodWithNoParams
                .stream()
                .findAny()
                .orElse(null);
        if(initMethod == null && candidateInitMethods.size() > 0)
            throw new RuntimeException("Aspect init method annotated with @InitAspect can't have parameters");
        try {
            if (initMethod != null) {
                initMethod.setAccessible(true);
                initMethod.invoke(aspectInstance);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    void setAspectScanManager(AspectScanManager aspectScanManager);
    AspectScanManager getAspectScanManager();
    void execBeforeCallAdvice(Object targetInstance, Method method, Object[] args);
    Object execBeforeReturnAdvice(Object targetInstance, Method method, Object[] args, Object returnVal);
    void execAfterCallAdvice(Object targetInstance, Method method, Object[] args, Object returnVal);
    void execAroundCallAdvice(Object targetInstance, Method method, Object[] args);
    void execOnExceptionAdvice(Object targetInstance, Method method, Object[] args, Throwable throwable);
}
