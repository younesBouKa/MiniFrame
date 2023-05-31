package org.aspect.proxy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatchers;
import org.aspect.processor.ProxyEventHandler;
import org.tools.Log;

import java.lang.reflect.Method;

public class AopMethodByteBuddyProxy<T> {
    private static final Log logger = Log.getInstance(AopMethodByteBuddyProxy.class);

    public static Object newInstance(Object instance) {
        Class<?> instanceClass = instance.getClass();
        Class<?> dynamicProxyClass = new ByteBuddy()
                .subclass(instanceClass)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(AopMethodByteBuddyProxy.class))
                .make()
                .load(AopMethodByteBuddyProxy.class.getClassLoader())
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
                                   @Origin Method targetMethod,
                                   @AllArguments Object[] args,
                                   @SuperMethod Method method) throws Throwable {
        Object result;
        ProxyEventHandler.execBeforeCall(instance, method, args);
        try {
            method.setAccessible(true);
            result = method.invoke(instance, args);
        } catch (Throwable throwable) {
            ProxyEventHandler.execOnException(instance, method, args, throwable.getCause());
            throw throwable;
        }
        result = ProxyEventHandler.execBeforeReturn(instance, method, args, result);
        ProxyEventHandler.execAfterCall(instance, method, args, result);
        return result;
    }
}