package org.aspect.processor;

import org.aspect.annotations.advices.*;
import org.aspect.scanners.AspectScanManager;
import org.aspect.scanners.AspectScanManagerImpl;
import org.tools.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AspectProcessorImpl implements AspectProcessor{
    private static final Log logger = Log.getInstance(AspectProcessorImpl.class);
    private static final AspectScanManager aspectScanManager = new AspectScanManagerImpl();

    public Object getAspectInstance(Class aspectClass){
        Constructor[] constructors = aspectClass.getConstructors();
        Constructor constructor = constructors[0];
        try {
            return constructor.newInstance(null);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void execBeforeCallAdvice(Object targetInstance, Method method, Object[] args){
        // get matching advices
        Map<Annotation, Method> advices = aspectScanManager.getAdvices(method, BeforeCall.class);
        List<Annotation> sortedAdviceAnnotations = advices.keySet()
                .stream()
                .sorted((anno1, anno2)-> {
                    int ord1 = ((BeforeCall) anno1).order(),
                            ord2 = ((BeforeCall) anno2).order();
                    return Integer.compare(ord1, ord2);
                })
                .collect(Collectors.toList());
        // call advice method
        for(Annotation adviceAnnotation : sortedAdviceAnnotations){
            Method adviceMethod = advices.get(adviceAnnotation);
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
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
        Map<Annotation, Method> advices = aspectScanManager.getAdvices(method, BeforeReturn.class);
        List<Annotation> sortedAdviceAnnotations = advices.keySet()
                .stream()
                .sorted((anno1, anno2)-> {
                    int ord1 = ((BeforeReturn) anno1).order(),
                            ord2 = ((BeforeReturn) anno2).order();
                    return Integer.compare(ord1, ord2);
                })
                .collect(Collectors.toList());
        // call advice method
        for(Annotation adviceAnnotation : sortedAdviceAnnotations){
            Method adviceMethod = advices.get(adviceAnnotation);
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
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
        Map<Annotation, Method> advices = aspectScanManager.getAdvices(method, AfterCall.class);
        List<Annotation> sortedAdviceAnnotations = advices.keySet()
                .stream()
                .sorted((anno1, anno2)-> {
                    int ord1 = ((AfterCall) anno1).order(),
                            ord2 = ((AfterCall) anno2).order();
                    return Integer.compare(ord1, ord2);
                })
                .collect(Collectors.toList());
        // call advice method
        for(Annotation adviceAnnotation : sortedAdviceAnnotations){
            Method adviceMethod = advices.get(adviceAnnotation);
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
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
        Map<Annotation, Method> advices = aspectScanManager.getAdvices(method, AroundCall.class);
        List<Annotation> sortedAdviceAnnotations = advices.keySet()
                .stream()
                .sorted((anno1, anno2)-> {
                    int ord1 = ((AroundCall) anno1).order(),
                            ord2 = ((AroundCall) anno2).order();
                    return Integer.compare(ord1, ord2);
                })
                .collect(Collectors.toList());
        // call advice method
        for(Annotation adviceAnnotation : sortedAdviceAnnotations){
            Method adviceMethod = advices.get(adviceAnnotation);
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
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
        Map<Annotation, Method> advices = aspectScanManager.getAdvices(method, OnException.class);
        List<Annotation> sortedAdviceAnnotations = advices.keySet()
                .stream()
                .sorted((anno1, anno2)-> {
                    int ord1 = ((OnException) anno1).order(),
                            ord2 = ((OnException) anno2).order();
                    return Integer.compare(ord1, ord2);
                })
                .collect(Collectors.toList());
        // call advice method
        for(Annotation adviceAnnotation : sortedAdviceAnnotations){
            Method adviceMethod = advices.get(adviceAnnotation);
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
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
