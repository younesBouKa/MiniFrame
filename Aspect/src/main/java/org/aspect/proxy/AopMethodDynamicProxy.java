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
        Object result;
        ProxyEventHandler.execBeforeCall(instance, method, args);
        try {
            method.setAccessible(true);
            result = method.invoke(instance, args);
        } catch (Throwable throwable) {
            ProxyEventHandler.execOnException(instance, method, args, throwable);
            throw throwable;
        }
        result = ProxyEventHandler.execBeforeReturn(instance, method, args, result);
        ProxyEventHandler.execAfterCall(instance, method, args, result);
        return result;
    }
}