package org.aspect.scanners;

import org.aspect.annotations.*;
import org.aspect.annotations.advices.*;
import org.tools.annotations.AnnotationTools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    default boolean isValidAdviceAnnotation(Annotation annotation){
        return isValidAdviceAnnotation(annotation.annotationType())
                ;
    }
    default boolean isValidAdviceAnnotation(Class<? extends Annotation> annotationType){
        return AnnotationTools.isAnnotationPresent(annotationType, AdviceMarker.class)
                ;
    }
    default boolean doesPointCutMatch(Method targetMethod, Annotation adviceAnnotation){
        String methodSignature = formatMethodSignature(targetMethod);
        Class<?> targetMethodClass = targetMethod.getDeclaringClass();
        if(adviceAnnotation instanceof AroundCall){
            AroundCall adviceAnn = (AroundCall) adviceAnnotation;
            return methodSignature.matches(adviceAnn.methodSignature());
        }
        if(adviceAnnotation instanceof BeforeCall){
            BeforeCall adviceAnn = (BeforeCall) adviceAnnotation;
            return methodSignature.matches(adviceAnn.methodSignature());
        }
        if(adviceAnnotation instanceof AfterCall){
            AfterCall adviceAnn = (AfterCall) adviceAnnotation;
            return methodSignature.matches(adviceAnn.methodSignature());
        }
        if(adviceAnnotation instanceof BeforeReturn){
            BeforeReturn adviceAnn = (BeforeReturn) adviceAnnotation;
            return methodSignature.matches(adviceAnn.methodSignature());
        }
        if(adviceAnnotation instanceof OnException){
            OnException adviceAnn = (OnException) adviceAnnotation;
            return methodSignature.matches(adviceAnn.methodSignature());
        }
        if(adviceAnnotation instanceof AnnotatedWith){
            AnnotatedWith adviceAnn = (AnnotatedWith) adviceAnnotation;
            Class<? extends Annotation> targetAnnotationType = adviceAnn.annotationClass();
            return AnnotationTools.isAnnotationPresent(targetMethod, targetAnnotationType);
        }
        if(adviceAnnotation instanceof TargetClass){
            TargetClass adviceAnn = (TargetClass) adviceAnnotation;
            Class<?> pointCutTargetClass= adviceAnn.target();
            return targetMethodClass.isAssignableFrom(pointCutTargetClass);
        }
        return false;
    }
    default String formatMethodSignature(Method targetMethod){
        return  targetMethod.toGenericString();
    }
    default void addAdviceMethod(Method method){
        for(Annotation annotation : method.getAnnotations()){
            if(isValidAdviceAnnotation(annotation))
                addAdviceMethod(method, annotation);
        }
    }
    default int getAdviceAnnotationOrder(Annotation adviceAnnotation){
        if(adviceAnnotation instanceof AroundCall){
            AroundCall adviceAnn = (AroundCall) adviceAnnotation;
            return adviceAnn.order();
        }
        if(adviceAnnotation instanceof BeforeCall){
            BeforeCall adviceAnn = (BeforeCall) adviceAnnotation;
            return adviceAnn.order();
        }
        if(adviceAnnotation instanceof AfterCall){
            AfterCall adviceAnn = (AfterCall) adviceAnnotation;
            return adviceAnn.order();
        }
        if(adviceAnnotation instanceof BeforeReturn){
            BeforeReturn adviceAnn = (BeforeReturn) adviceAnnotation;
            return adviceAnn.order();
        }
        if(adviceAnnotation instanceof OnException){
            OnException adviceAnn = (OnException) adviceAnnotation;
            return adviceAnn.order();
        }
        if(adviceAnnotation instanceof AnnotatedWith){
            AnnotatedWith adviceAnn = (AnnotatedWith) adviceAnnotation;
            return adviceAnn.order();
        }
        if(adviceAnnotation instanceof TargetClass){
            TargetClass adviceAnn = (TargetClass) adviceAnnotation;
            return adviceAnn.order();
        }
        return 1;
    }
    default List<Method> getSortedAdvices(Method targetMethod, Class<? extends Annotation> adviceAnnotationType){
        return getSortedAdvices(targetMethod, adviceAnnotationType, SortType.ASC);
    }
    default List<Method> getSortedAdvices(Method targetMethod, Class<? extends Annotation> adviceAnnotationType, SortType sortType){
        Map<Annotation, Method> advices = getAdvices(targetMethod, adviceAnnotationType);
        int sortModifier = sortType==null || sortType.equals(SortType.ASC) ? 1 : -1;
        return advices.keySet()
                .stream()
                .sorted((anno1, anno2)-> {
                    int ord1 = getAdviceAnnotationOrder(anno1),
                            ord2 = getAdviceAnnotationOrder(anno2);
                    return sortModifier * Integer.compare(ord1, ord2);
                })
                .map(advices::get)
                .collect(Collectors.toList());
    }
    Map<Annotation, Method> getAdvices(Method targetMethod, Class<? extends Annotation> adviceAnnotationType);
    void addAdviceMethod(Method adviceMethod, Annotation adviceAnnotation);
}
