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
        JoinPoint joinPoint = new JoinPoint();
        joinPoint.setTargetMethod(method.toGenericString());
        joinPoint.setTargetClass(method.getDeclaringClass().getCanonicalName());
        joinPoint.setArgs(args);

        Object result;
        ProxyEventHandler.execBeforeCall(joinPoint);
        try {
            method.setAccessible(true);
            result = method.invoke(instance, args);
        } catch (Throwable throwable) {
            joinPoint.setThrowable(throwable);
            ProxyEventHandler.execOnException(joinPoint);
            throw throwable;
        }
        joinPoint.setReturnVal(result);
        result = ProxyEventHandler.execBeforeReturn(joinPoint);
        joinPoint.setReturnVal(result);
        ProxyEventHandler.execAfterCall(joinPoint);
        return result;
    }
}