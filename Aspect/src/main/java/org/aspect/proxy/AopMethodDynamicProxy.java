package org.aspect.proxy;

import org.aspect.processor.AspectProcessor;
import org.aspect.processor.AspectProcessorImpl;
import org.tools.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AopMethodDynamicProxy<T> implements InvocationHandler {
    private static final Log logger = Log.getInstance(AopMethodDynamicProxy.class);
    private static AspectProcessor aspectProcessor = new AspectProcessorImpl();
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
        aspectProcessor.execBeforeCallAdvice(instance, method, args);
        aspectProcessor.execAroundCallAdvice(instance, method, args);
        try {
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