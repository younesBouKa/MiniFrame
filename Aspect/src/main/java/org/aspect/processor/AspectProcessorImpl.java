package org.aspect.processor;

import org.aspect.annotations.advices.*;
import org.aspect.scanners.AspectScanManager;
import org.tools.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class AspectProcessorImpl implements AspectProcessor{
    private static final Log logger = Log.getInstance(AspectProcessorImpl.class);
    private AspectScanManager aspectScanManager;

    public AspectProcessorImpl(AspectScanManager aspectScanManager){
        this.aspectScanManager = aspectScanManager;
    }

    @Override
    public AspectScanManager getAspectScanManager() {
        return aspectScanManager;
    }

    @Override
    public void setAspectScanManager(AspectScanManager aspectScanManager) {
        this.aspectScanManager = aspectScanManager;
    }

    public void execTargetClassAdvice(Object targetInstance, Method method, Object[] args){
        // get matching advices
        List<Method> sortedAdvices = aspectScanManager.getSortedAdvices(method, BeforeCall.class);
        // call advice method
        for(Method adviceMethod : sortedAdvices){
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
                adviceMethod.setAccessible(true);
                adviceMethod.invoke(aspectInstance, method, args, targetInstance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e){
                logger.error("Advice method "+adviceMethod.toGenericString()+" must accept 3 arguments \n (" +
                        "java.lang.reflect.Method targetMethod, " +
                        "java.lang.reflect.Object[] args, " +
                        "java.lang.reflect.Object currentInstance" +
                        ")");
            }
        }
    }

    public void execAnnotatedWithAdvice(Object targetInstance, Method method, Object[] args){
        // get matching advices
        List<Method> sortedAdvices = aspectScanManager.getSortedAdvices(method, BeforeCall.class);
        // call advice method
        for(Method adviceMethod : sortedAdvices){
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
                adviceMethod.setAccessible(true);
                adviceMethod.invoke(aspectInstance, method, args, targetInstance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e){
                logger.error("Advice method "+adviceMethod.toGenericString()+" must accept 3 arguments \n (" +
                        "java.lang.reflect.Method targetMethod, " +
                        "java.lang.reflect.Object[] args, " +
                        "java.lang.reflect.Object currentInstance" +
                        ")");
            }
        }
    }

    public void execBeforeCallAdvice(Object targetInstance, Method method, Object[] args){
        // get matching advices
        List<Method> sortedAdvices = aspectScanManager.getSortedAdvices(method, BeforeCall.class);
        // call advice method
        for(Method adviceMethod : sortedAdvices){
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
                adviceMethod.setAccessible(true);
                adviceMethod.invoke(aspectInstance, method, args, targetInstance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e){
                logger.error("Advice method "+adviceMethod.toGenericString()+" must accept 3 arguments \n (" +
                        "java.lang.reflect.Method targetMethod, " +
                        "java.lang.reflect.Object[] args, " +
                        "java.lang.reflect.Object currentInstance" +
                        ")");
            }
        }
    }

    public Object execBeforeReturnAdvice(Object targetInstance, Method method, Object[] args, Object returnVal){
        // get matching advices
        List<Method> sortedAdvices = aspectScanManager.getSortedAdvices(method, BeforeReturn.class);
        // call advice method
        for(Method adviceMethod : sortedAdvices){
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
                adviceMethod.setAccessible(true);
                returnVal = adviceMethod.invoke(aspectInstance, method, args, targetInstance, returnVal);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e){
                logger.error("Advice method "+adviceMethod.toGenericString()+" must accept 4 arguments \n (" +
                        "java.lang.reflect.Method targetMethod, " +
                        "java.lang.Object[] args, " +
                        "java.lang.Object currentInstance, " +
                        "java.lang.Object returnVal" +
                        ")");
            }
        }
        return returnVal;
    }

    public void execAfterCallAdvice(Object targetInstance, Method method, Object[] args, Object returnVal){
        // get matching advices
        List<Method> sortedAdvices = aspectScanManager.getSortedAdvices(method, AfterCall.class);
        // call advice method
        for(Method adviceMethod : sortedAdvices){
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
                adviceMethod.setAccessible(true);
                adviceMethod.invoke(aspectInstance, method, args, targetInstance, returnVal);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e){
                logger.error("Advice method "+adviceMethod.toGenericString()+" must accept 4 arguments \n " +
                        "(java.lang.reflect.Method targetMethod, " +
                        "java.lang.Object[] args, " +
                        "java.lang.Object currentInstance, " +
                        "java.lang.Object returnValue)" +
                        "");
            }
        }
    }

    public void execAroundCallAdvice(Object targetInstance, Method method, Object[] args){
        // get matching advices
        List<Method> sortedAdvices = aspectScanManager.getSortedAdvices(method, AroundCall.class);
        // call advice method
        for(Method adviceMethod : sortedAdvices){
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
                adviceMethod.setAccessible(true);
                adviceMethod.invoke(aspectInstance, method, args, targetInstance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e){
                logger.error("Advice method "+adviceMethod.toGenericString()+" must accept 3 arguments \n(" +
                        "java.lang.reflect.Method targetMethod, " +
                        "java.lang.Object[] args, " +
                        "java.lang.Object currentInstance" +
                        ")");
            }
        }
    }

    public void execOnExceptionAdvice(Object targetInstance, Method method, Object[] args, Throwable throwable){
        // get matching advices
        List<Method> sortedAdvices = aspectScanManager.getSortedAdvices(method, OnException.class);
        // call advice method
        for(Method adviceMethod : sortedAdvices){
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
                adviceMethod.setAccessible(true);
                adviceMethod.invoke(aspectInstance, method, args, targetInstance, throwable);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e){
                logger.error("Advice method "+adviceMethod.toGenericString()+" must accept 4 arguments \n(" +
                        "java.lang.reflect.Method targetMethod, " +
                        "java.lang.Object[] args, " +
                        "java.lang.Object currentInstance, " +
                        "java.lang.Throwable throwable" +
                        ")");
            }
        }
    }

}
