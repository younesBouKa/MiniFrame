package org.aspect.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.aspect.processor.ProxyEventHandler;
import org.tools.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AopMethodCglibProxy<T> implements MethodInterceptor {
    private static final Log logger = Log.getInstance(AopMethodCglibProxy.class);
    private final Object instance;

    private AopMethodCglibProxy(Object instance) {
        this.instance = instance;
    }

    public static Object newInstance(Object instance) {
        return Enhancer.create(instance.getClass(), new AopMethodCglibProxy<>(instance));
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if(Modifier.isPrivate(method.getModifiers()) || Modifier.isFinal(method.getModifiers()))
            throw new RuntimeException("Method "+method.toGenericString()+" wrapped with a CGLIB proxy can't be private or final");
        Object result;
        ProxyEventHandler.execBeforeCall(instance, method, args);
        try {
            result = methodProxy.invoke(instance, args);
        } catch (Throwable throwable) {
            ProxyEventHandler.execOnException(instance, method, args, throwable);
            throw throwable;
        }
        result = ProxyEventHandler.execBeforeReturn(instance, method, args, result);
        ProxyEventHandler.execAfterCall(instance, method, args, result);
        return result;
    }
}