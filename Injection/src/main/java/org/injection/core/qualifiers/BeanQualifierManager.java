package org.injection.core.qualifiers;

import org.tools.annotations.AnnotationTools;
import org.tools.exceptions.FrameworkException;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface BeanQualifierManager {
    void setDefaultQualifier(Annotation defaultQualifier);
    Annotation getDefaultQualifier(Class<?> beanType);
    Map<Class<? extends Annotation>, QualifierPredicate> getAvailableQualifiers();
    default QualifierPredicate addMarkerQualifier(Class<? extends Annotation> qualifierAnnotationClass){
        return addQualifier(qualifierAnnotationClass, null);
    }
    QualifierPredicate addQualifier(Class<? extends Annotation> qualifierAnnotationClass, QualifierPredicate qualifierPredicate);
    QualifierPredicate removeQualifier(Class<? extends Annotation> qualifierAnnotationClass);
    default Set<Annotation> getQualifiers(final Object from){
        Set<Annotation> qualifiersToReturn = new HashSet<>();
        Map<Class<? extends Annotation>, QualifierPredicate> availableQualifiers = getAvailableQualifiers();
        if(availableQualifiers==null || availableQualifiers.isEmpty())
            return qualifiersToReturn;
        for (Class<? extends Annotation> qualifierAnnotation : availableQualifiers.keySet()){
            Annotation qualifier = getQualifier(from, qualifierAnnotation);
            if(qualifier!=null)
                qualifiersToReturn.add(qualifier);
        }
        return qualifiersToReturn;
    }
    default Annotation getQualifier(final Object from, Class<? extends Annotation> qualifierClass) {
        Annotation qualifier = null;
        if(from instanceof Parameter){
            qualifier = AnnotationTools.getAnnotation((Parameter) from, qualifierClass);
        } else if(from instanceof Field){
            qualifier = AnnotationTools.getAnnotation((Field) from, qualifierClass);
        }else if(from instanceof Class){
            qualifier = AnnotationTools.getAnnotation((Class) from, qualifierClass);
        }else if(from instanceof Method){
            qualifier = AnnotationTools.getAnnotation((Method) from, qualifierClass);
        }else{
            throw new FrameworkException("Qualifier source type ["+from+"] is not recognized");
        }
        return qualifier;
    }
    Set<Class> filterImplementations(final Set<Class> beanImplementations, final Set<Annotation> qualifiers);
    Set<Method> filterFactories(final Set<Method> beanFactories, final Set<Annotation> qualifiers);
    boolean match(final Object beanSource, final Set<Annotation> qualifiers, boolean withAndEvaluation);
    default boolean isValidQualifierAnnotation(Class<? extends Annotation> annotationClass){
        if(annotationClass==null || !annotationClass.isAnnotation())
            return false;
        Annotation rootQualifierAnnotation = AnnotationTools.getAnnotation(annotationClass, Qualifier.class);
        return annotationClass.isAnnotation() && rootQualifierAnnotation!=null;
    }
}
