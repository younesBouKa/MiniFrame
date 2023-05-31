package org.aspect.scanners;

import org.aspect.annotations.Advice;
import org.aspect.annotations.Aspect;
import org.aspect.annotations.CutPoint;
import org.aspect.annotations.Type;
import org.aspect.annotations.enums.CutPointType;
import org.tools.ClassFinder;
import org.tools.annotations.AnnotationTools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface AspectScanManager {
    default boolean isValidAspect(Class<?> aspectClass){
        return  !aspectClass.isInterface()
                && !Modifier.isAbstract(aspectClass.getModifiers())
                && AnnotationTools.isAnnotationPresent(aspectClass, Aspect.class)
                ;
    }
    default boolean isValidAdvice(Method adviceMethod){
        return  AnnotationTools.isAnnotationPresent(adviceMethod, Advice.class)
                || (
                        AnnotationTools.isAnnotationPresent(adviceMethod, CutPoint.class)
                        && AnnotationTools.isAnnotationPresent(adviceMethod, Type.class)
                        //&& AnnotationTools.isAnnotationPresent(adviceMethod, Position.class)
                )
                ;
    }
    default boolean isValidAdviceAnnotation(Annotation annotation){
        return isValidAdviceAnnotation(annotation.annotationType())
                ;
    }
    default boolean isValidAdviceAnnotation(Class<? extends Annotation> annotationType){
        return  AnnotationTools.isAnnotationPresent(annotationType, Advice.class)
                || (
                        AnnotationTools.isAnnotationPresent(annotationType, CutPoint.class)
                                && AnnotationTools.isAnnotationPresent(annotationType, Type.class)
                        //&& AnnotationTools.isAnnotationPresent(adviceMethod, Position.class)
                )
                ;
    }
    default boolean doesPointCutMatch(Method targetMethod, Annotation adviceAnnotation){
        String methodSignature = formatMethodSignature(targetMethod);
        Class<?> targetMethodClass = targetMethod.getDeclaringClass();
        if(adviceAnnotation instanceof Advice){
            Advice advice = (Advice) adviceAnnotation;
            CutPointType cutPointType = advice.cutPointType();
            if(cutPointType.equals(CutPointType.METHOD_REGEX)){
                return methodSignature.matches(advice.cutPointValue());
            }else if(cutPointType.equals(CutPointType.CLASS_REGEX)){
                Class<?> targetClass = ClassFinder.loadClass(advice.cutPointValue());
                return targetClass!=null && targetMethodClass.isAssignableFrom(targetClass);
            }else if(cutPointType.equals(CutPointType.ANNOTATION)){
                Class<?> targetAnnotationType = ClassFinder.loadClass(advice.cutPointValue());
                return AnnotationTools.isAnnotationPresent(targetMethod, targetAnnotationType);
            }
        }
        return false;
    }
    default String formatMethodSignature(Method targetMethod){
        return  targetMethod.toGenericString();
    }
    default List<Method> getSortedAdvices(Method targetMethod, Predicate<Annotation> filter){
        return getSortedAdvices(targetMethod, filter, SortType.ASC);
    }
    default List<Method> getSortedAdvices(Method targetMethod, Predicate<Annotation> filter, SortType sortType){
        Map<Annotation, Method> advices = getAdvices(targetMethod, filter);
        int sortModifier = sortType==null || sortType.equals(SortType.ASC) ? 1 : -1;
        return advices.keySet()
                .stream()
                .sorted((anno1, anno2)-> {
                    int ord1 = ((Advice)anno1).order(),
                            ord2 = ((Advice)anno2).order();
                    return sortModifier * Integer.compare(ord1, ord2);
                })
                .map(advices::get)
                .collect(Collectors.toList());
    }

    Map<Annotation, Method> getAdvices(Method targetMethod, Predicate<Annotation> filter);
    Map<Annotation, Method> getAdvices(Method targetMethod);
    void addAdviceMethod(Method adviceMethod, Annotation adviceAnnotation);
    void addAdviceMethod(Method method);
}
