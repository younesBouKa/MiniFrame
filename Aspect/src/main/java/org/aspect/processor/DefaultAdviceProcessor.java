package org.aspect.processor;

import org.aspect.annotations.Advice;
import org.aspect.annotations.enums.AdviceType;
import org.aspect.annotations.enums.ExecPosition;
import org.aspect.annotations.types.Exception;
import org.aspect.proxy.JoinPoint;
import org.aspect.scanners.AspectScanManager;
import org.tools.ClassFinder;
import org.tools.Log;
import org.tools.annotations.AnnotationTools;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DefaultAdviceProcessor implements AdviceProcessor{
    private static final Log logger = Log.getInstance(DefaultAdviceProcessor.class);
    private AspectScanManager aspectScanManager;

    public DefaultAdviceProcessor(AspectScanManager aspectScanManager){
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

    public Method getTargetMethodFromJointPoint(JoinPoint joinPoint){
        if (joinPoint==null || joinPoint.getTargetClass()==null || joinPoint.getTargetMethod()==null)
            return null;
        Class<?> targetClass = ClassFinder.loadClass(joinPoint.getTargetClass());
        Method method = null;
        for(Method targetMethod : targetClass.getDeclaredMethods()){
            if(targetMethod.toGenericString().equals(joinPoint.getTargetMethod())){
                method = targetMethod;
                break;
            }
        }
        return method;
    }

    public void execBeforeCall(JoinPoint joinPoint){
        Method method = getTargetMethodFromJointPoint(joinPoint);
        if (method==null )
            return;
        // get matching advices
        Predicate<Annotation> filter = (annotation) -> annotation instanceof Advice
                && (
                        ((Advice)annotation).execPosition().equals(ExecPosition.BEFORE) ||
                                ((Advice)annotation).execPosition().equals(ExecPosition.AROUND)
                )
                && ((Advice)annotation).adviceType().equals(AdviceType.CALL);
        List<Method> sortedAdvices = aspectScanManager.getSortedAdvices(method, filter);
        // call advice method
        for(Method adviceMethod : sortedAdvices){
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
                adviceMethod.setAccessible(true);
                adviceMethod.invoke(aspectInstance, joinPoint);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e){
                logger.error("Advice method "+adviceMethod.toGenericString()+" must accept 4 arguments \n (" +
                        "java.lang.reflect.Method targetMethod, " +
                        "java.lang.reflect.Object[] args, " +
                        "java.lang.reflect.Object currentInstance" +
                        "java.lang.Object returnVal" +
                        ")");
            }
        }
    }

    public Object execBeforeReturn(JoinPoint joinPoint){
        Method method = getTargetMethodFromJointPoint(joinPoint);
        Object returnVal = joinPoint.getReturnVal();
        if (method==null )
            return returnVal;
        // get matching advices
        Predicate<Annotation> filter = (annotation) -> annotation instanceof Advice
                && ((Advice)annotation).execPosition().equals(ExecPosition.BEFORE_RETURN)
                && ((Advice)annotation).adviceType().equals(AdviceType.CALL);
        List<Method> sortedAdvices = aspectScanManager.getSortedAdvices(method, filter);
        // call advice method
        for(Method adviceMethod : sortedAdvices){
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
                adviceMethod.setAccessible(true);
                returnVal = adviceMethod.invoke(aspectInstance, joinPoint);
                joinPoint.setReturnVal(returnVal);
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

    public void execAfterCall(JoinPoint joinPoint){
        Method method = getTargetMethodFromJointPoint(joinPoint);
        if (method==null )
            return;
        // get matching advices
        Predicate<Annotation> filter = (annotation) -> annotation instanceof Advice
                && (
                        ((Advice)annotation).execPosition().equals(ExecPosition.AFTER) ||
                        ((Advice)annotation).execPosition().equals(ExecPosition.AROUND)
                )
                && ((Advice)annotation).adviceType().equals(AdviceType.CALL);
        List<Method> sortedAdvices = aspectScanManager.getSortedAdvices(method, filter);
        // call advice method
        for(Method adviceMethod : sortedAdvices){
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
                adviceMethod.setAccessible(true);
                adviceMethod.invoke(aspectInstance, joinPoint);
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

    public void execOnException(JoinPoint joinPoint){
        Method method = getTargetMethodFromJointPoint(joinPoint);
        Throwable throwable = joinPoint.getThrowable();
        if (method==null )
            return;
        // get matching advices
        Predicate<Annotation> filter = (annotation) -> annotation instanceof Advice
                && ((Advice)annotation).adviceType().equals(AdviceType.EXCEPTION)
                ;
        List<Method> sortedAdvices = aspectScanManager.getSortedAdvices(method, filter);
        List<Method> sortedAdvicesWithExceptionType = new ArrayList<>();
        for (Method adviceMethod : sortedAdvices){
            Exception exception = (Exception) AnnotationTools.getAnnotation(adviceMethod, Exception.class);
            if(exception!=null){
                Class<?>[] types = exception.types();
                for(Class<?> type: types){
                    if(type.isAssignableFrom(throwable.getClass()))
                        sortedAdvicesWithExceptionType.add(adviceMethod);
                }
            }
        }
        // call advice method
        for(Method adviceMethod : sortedAdvicesWithExceptionType){
            try {
                // get aspect class instance
                Object aspectInstance = null;
                if(!Modifier.isStatic(adviceMethod.getModifiers()))
                    aspectInstance = getAspectInstance(adviceMethod.getDeclaringClass());
                adviceMethod.setAccessible(true);
                adviceMethod.invoke(aspectInstance, joinPoint);
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
