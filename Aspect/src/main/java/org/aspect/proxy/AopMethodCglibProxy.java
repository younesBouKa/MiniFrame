package org.aspect.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.aspect.processor.AspectProcessor;
import org.tools.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AopMethodCglibProxy<T> implements MethodInterceptor {
    private static final Log logger = Log.getInstance(AopMethodCglibProxy.class);
    private static AspectProcessor aspectProcessor;
    private final Object instance;

    private AopMethodCglibProxy(Object instance) {
        this.instance = instance;
    }

    public static Object newInstance(Object instance) {
        if(aspectProcessor==null)
            throw new RuntimeException("No aspect processor is defined");
        return Enhancer.create(instance.getClass(), new AopMethodCglibProxy<>(instance));
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        if(Modifier.isPrivate(method.getModifiers()) || Modifier.isFinal(method.getModifiers()))
            throw new RuntimeException("Method "+method.toGenericString()+" wrapped with a CGLIB proxy can't be private or final");
        Object result;
        aspectProcessor.execBeforeCallAdvice(o, method, args);
        aspectProcessor.execAroundCallAdvice(o, method, args);
        try {
            result = methodProxy.invoke(instance, args);
        } catch (Throwable throwable) {
            aspectProcessor.execOnExceptionAdvice(o, method, args, throwable);
            throw new RuntimeException("unexpected invocation exception: " + throwable.getMessage());
        }
        result = aspectProcessor.execBeforeReturnAdvice(o, method, args, result);
        aspectProcessor.execAroundCallAdvice(o, method, args);
        aspectProcessor.execAfterCallAdvice(o, method, args, result);
        return result;
    }


    public static void setAspectProcessor(AspectProcessor otherAspectProcessor){
        aspectProcessor = otherAspectProcessor;
    }

    public static AspectProcessor getAspectProcessor(){
        return aspectProcessor;
    }
}