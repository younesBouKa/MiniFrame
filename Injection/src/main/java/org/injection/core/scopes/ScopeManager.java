package org.injection.core.scopes;

import org.tools.annotations.AnnotationTools;

import javax.inject.Scope;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

public interface ScopeManager {
    default boolean isValidScopeAnnotation(Class clazz){
        if(clazz==null || !clazz.isAnnotation())
            return false;
        Annotation rootScopeAnnotation = AnnotationTools.getAnnotation(clazz, Scope.class);
        return rootScopeAnnotation!=null;
    }
    void setDefaultScopeType(Class<? extends Annotation> defaultScopeType);
    Class<? extends Annotation> getDefaultScopeType(Class<?> beanType);
    boolean addScopeAnnotation(Class<? extends Annotation> scopeAnnotation);
    boolean removeScopeAnnotation(Class<? extends Annotation> scopeAnnotation);
    Set<Class> getAvailableScopes();
    Class<? extends Annotation> getParameterScope(Parameter parameter);
    Class<? extends Annotation> getFieldScope(Field field);
    Class<? extends Annotation> getClassScope(Class clazz);
    Class<? extends Annotation> getMethodScope(Method method);
}
