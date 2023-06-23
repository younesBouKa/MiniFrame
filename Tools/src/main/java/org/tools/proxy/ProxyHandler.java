package org.tools.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProxyHandler<T> implements InvocationHandler {

    private BeforeCallFunction beforeCall;
    private AfterCallFunction afterCall;
    private final Object instance;

    public ProxyHandler(Object instance,
                        BeforeCallFunction beforeCall,
                        AfterCallFunction afterCall) {
        this.instance = instance;
        this.beforeCall = beforeCall;
        this.afterCall = afterCall;
    }

    public ProxyHandler(Object instance) {
        this.instance = instance;
    }

    public static Object newInstance(Object instance) {
        return java.lang.reflect.Proxy.newProxyInstance(
                instance.getClass().getClassLoader(),
                instance.getClass().getInterfaces(),
                new ProxyHandler<>(instance));
    }

    public static Object newInstance(Object instance,
                                     BeforeCallFunction beforeCall,
                                     AfterCallFunction afterCall) {
        return java.lang.reflect.Proxy.newProxyInstance(
                instance.getClass().getClassLoader(),
                instance.getClass().getInterfaces(),
                new ProxyHandler<>(instance, beforeCall, afterCall));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        if(beforeCall!=null)
            beforeCall.apply(proxy, method, args);
        try {
            result = method.invoke(instance, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
            throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
        } finally {
        }
        if(afterCall!=null)
            afterCall.apply(proxy, method, args, result);
        return result;
    }
}