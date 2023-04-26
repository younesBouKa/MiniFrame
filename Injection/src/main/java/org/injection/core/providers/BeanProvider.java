package org.injection.core.providers;

import org.injection.core.global.InjectionEvaluator;
import org.injection.core.data.ScopeInstance;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * BeanProvider interface help in bean injection abstraction
 */
public interface BeanProvider extends InjectionEvaluator {

    default <T> T getBeanInstance(Class<T> beanType){
        return getBeanInstance(beanType, null, null, null);
    }
    default <T> T getBeanInstanceWithQualifiers(Class<T> beanType, Set<Annotation> qualifiers){
        return getBeanInstance(beanType, qualifiers, null, null);
    }
    default <T> T getBeanInstanceWithScopes(Class<T> beanType, Set<ScopeInstance> scopes){
        return getBeanInstance(beanType, null, scopes, null);
    }
    default <T> T getBeanInstanceWithScopes(Class<T> beanType, Set<ScopeInstance> scopes, Class<?> beanScopeType){
        return getBeanInstance(beanType, null, scopes, beanScopeType);
    }
    <T> T getBeanInstance(Class<T> beanType, Set<Annotation> qualifiers, Set<ScopeInstance> scopes, Class<?> beanScopeType);

}
