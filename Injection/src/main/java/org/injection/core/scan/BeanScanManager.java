package org.injection.core.scan;

import org.injection.annotations.Component;
import org.injection.core.data.BeanConfig;
import org.tools.annotations.AnnotationTools;
import org.tools.exceptions.FrameworkException;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

public interface BeanScanManager {
    int NO_PRIORITY = 0;
    int IMPLEMENTATION_OVER_FACTORY = 1;
    int FACTORY_OVER_IMPLEMENTATION = 2;
    /**
     * Prioritize bean implementation classes over bean factory
     * @param beanType
     * @return
     *    0: no priority (implementations and factories have the same priority)
     *    1: implementations oven factories
     *    2: factories over implementations
     */
    default int getBeanPriority(Class<?> beanType){
        return FACTORY_OVER_IMPLEMENTATION;
    }

    /**
     * Check if given class valid:
     *      - Not null
     *      - Is not interface
     *      - Is not abstract
     *      - Annotated with @Component or one of its children
     * @param clazz
     * @return
     */
    default boolean isValidComponentClass(Class<?> clazz){
        return clazz!=null
                && !clazz.isInterface()
                && !Modifier.isAbstract(clazz.getModifiers())
                && (
                AnnotationTools.isAnnotationPresent(clazz, Singleton.class)
                        || AnnotationTools.isAnnotationPresent(clazz, Component.class)
        );
    }

    /**
     * Check if given method is valid bean factory method:
     *      - Not null
     *      - Method return type is of type interface
     *      - Annotated with @Component or one of its children
     * @param method
     * @return
     */
    default boolean isValidFactoryMethod(Method method){
        boolean isAnnotationPresent = method!=null
                && (
                AnnotationTools.isAnnotationPresent(method, Singleton.class)
                        || AnnotationTools.isAnnotationPresent(method, Component.class)
        );
        if(!isAnnotationPresent)
            return false;
        boolean isDeclaringClassAbstract = Modifier.isAbstract(method.getDeclaringClass().getModifiers());
        if(isDeclaringClassAbstract){
            throw new FrameworkException("Factory method ["+method.toGenericString()+"] " +
                    "can't be abstract");
        }
        return true;
        // TODO detect construction loop
    }
    boolean addBeanImplementation(Class<?> beanType, Class implementation);
    boolean addBeanFactory(Class<?> beanType, Method factory);
    BeanConfig getBeanConfig(Class<?> beanType, Set<Annotation> qualifiers);

    BeanConfig getBeanAlternative(Class<?> beanType);

    Class selectBeanImplementation(Class<?> beanType, Set<Annotation> qualifiers);

    Method selectBeanFactory(Class<?> beanType, Set<Annotation> qualifiers);

    Set<Class> getBeanImplementations(Class<?> beanType);

    Set<Method> getBeanFactories(Class<?> beanType);
}
