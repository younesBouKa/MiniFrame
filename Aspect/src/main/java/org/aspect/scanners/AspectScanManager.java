package org.aspect.scanners;

import org.aspect.annotations.*;
import org.aspect.annotations.advices.*;
import org.tools.annotations.AnnotationTools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

public interface AspectScanManager {
    default boolean isValidAspect(Class<?> aspectClass){
        return  !aspectClass.isInterface()
                && !Modifier.isAbstract(aspectClass.getModifiers())
                && AnnotationTools.isAnnotationPresent(aspectClass, Aspect.class)
                ;
    }
    default boolean isValidAdvice(Method adviceMethod){
        return  AnnotationTools.isAnnotationPresent(adviceMethod, AdviceMarker.class)
                ;
    }
    default boolean doesPointCutMatch(Method targetMethod, Annotation adviceAnnotation){
        String methodSignature = targetMethod.toGenericString();// TODO to see later
        String pointCutExpression = null;
        if(adviceAnnotation instanceof AroundCall){
            AroundCall adviceAnn = (AroundCall) adviceAnnotation;
            pointCutExpression = adviceAnn.methodSignature();
        }
        if(adviceAnnotation instanceof BeforeCall){
            BeforeCall adviceAnn = (BeforeCall) adviceAnnotation;
            pointCutExpression = adviceAnn.methodSignature();
        }
        if(adviceAnnotation instanceof AfterCall){
            AfterCall adviceAnn = (AfterCall) adviceAnnotation;
            pointCutExpression = adviceAnn.methodSignature();
        }
        if(adviceAnnotation instanceof BeforeReturn){
            BeforeReturn adviceAnn = (BeforeReturn) adviceAnnotation;
            pointCutExpression = adviceAnn.methodSignature();
        }
        if(adviceAnnotation instanceof OnException){
            OnException adviceAnn = (OnException) adviceAnnotation;
            pointCutExpression = adviceAnn.methodSignature();
        }
        return (pointCutExpression!=null && methodSignature.matches(pointCutExpression))
                ;
    }

    Map<Annotation, Method> getAdvices(Method targetMethod, Class<? extends Annotation> adviceAnnotationType);
    void addAdviceMethod(Method adviceMethod);
    void addAdviceMethod(Method adviceMethod, Annotation adviceAnnotation);
}
