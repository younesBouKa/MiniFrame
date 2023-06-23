package org.aspect.proxy;

import org.aspect.processor.ProxyEventHandler;
import org.tools.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AopMethodDynamicProxy<T> implements InvocationHandler {
    private static final Log logger = Log.getInstance(AopMethodDynamicProxy.class);
    private final Object instance;

    private AopMethodDynamicProxy(Object instance) {
        this.instance = instance;
    }

    public static Object newInstance(Object instance) {
        return java.lang.reflect.Proxy.newProxyInstance(
                instance.getClass().getClassLoader(),
                instance.getClass().getInterfaces(),
                new AopMethodDynamicProxy<>(instance));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        JoinPoint joinPoint = new JoinPoint();
        joinPoint.setTargetMethod(method);
        joinPoint.setTargetClass(method.getDeclaringClass());
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