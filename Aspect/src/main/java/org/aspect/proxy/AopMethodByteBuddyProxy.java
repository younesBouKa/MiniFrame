package org.aspect.proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatchers;
import org.aspect.processor.AspectProcessor;
import org.tools.Log;

import java.lang.reflect.Method;

public class AopMethodByteBuddyProxy<T> {
    private static final Log logger = Log.getInstance(AopMethodByteBuddyProxy.class);
    private static AspectProcessor aspectProcessor;

    public static Object newInstance(Object instance) {
        if(aspectProcessor==null)
            throw new RuntimeException("No aspect processor is defined");
        Class<?> instanceClass = instance.getClass();
        Class<?> dynamicProxyClass = new ByteBuddy()
                .subclass(Object.class)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(AopMethodByteBuddyProxy.class))
                .make()
                .load(instanceClass.getClassLoader())
                .getLoaded();
        try {
            return dynamicProxyClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @RuntimeType
    public static Object intercept(@This Object instance,
                                   @Origin Method proxyMethod,
                                   @AllArguments Object[] args,
                                   @SuperMethod Method method) throws Throwable {
        Object result;
        aspectProcessor.execBeforeCallAdvice(instance, method, args);
        aspectProcessor.execAroundCallAdvice(instance, method, args);
        try {
            method.setAccessible(true);
            result = method.invoke(instance, args);
        } catch (Throwable throwable) {
            aspectProcessor.execOnExceptionAdvice(instance, method, args, throwable);
            throw new RuntimeException("unexpected invocation exception: " + throwable.getMessage());
        }
        result = aspectProcessor.execBeforeReturnAdvice(instance, method, args, result);
        aspectProcessor.execAroundCallAdvice(instance, method, args);
        aspectProcessor.execAfterCallAdvice(instance, method, args, result);
        return result;
    }

    public static void setAspectProcessor(AspectProcessor otherAspectProcessor){
        aspectProcessor = otherAspectProcessor;
    }

    public static AspectProcessor getAspectProcessor(){
        return aspectProcessor;
    }

}