package org.aspect.processor;

import java.lang.reflect.Method;

public interface AspectProcessor {
    Object getAspectInstance(Class aspectClass);
    void execBeforeCallAdvice(Object targetInstance, Method method, Object[] args);
    Object execBeforeReturnAdvice(Object targetInstance, Method method, Object[] args, Object returnVal);
    void execAfterCallAdvice(Object targetInstance, Method method, Object[] args, Object returnVal);
    void execAroundCallAdvice(Object targetInstance, Method method, Object[] args);
    void execOnExceptionAdvice(Object targetInstance, Method method, Object[] args, Throwable throwable);
}
