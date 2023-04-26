package org.injection.core.alternatives;

import org.injection.annotations.Alternative;
import org.injection.core.data.AlternativeInstance;
import org.injection.enums.BeanSourceType;
import org.tools.ClassFinder;
import org.tools.annotations.AnnotationTools;
import org.tools.exceptions.FrameworkException;

import java.lang.reflect.Method;
import java.util.stream.Stream;

public interface AlternativeManager {
    AlternativeInstance getAlternative(Class<?> beanType, BeanSourceType sourceType);
    void addAlternative(AlternativeInstance alternativeInstance);
    AlternativeInstance removeAlternative(Class<?> beanType);
    default AlternativeInstance getAlternative(Class<?> beanType){
        return getAlternative(beanType, null);
    }
    default boolean validateAlternative(AlternativeInstance alternativeInstance){
        if(alternativeInstance==null)
            return false;
        Class<?> beanType = alternativeInstance.getBeanType();
        if(beanType==null){
            throw new FrameworkException("Bean Type from alternative ["+alternativeInstance+"] can't be null");
        }
        BeanSourceType sourceType = alternativeInstance.getSourceType();
        if(sourceType==null){
            throw new FrameworkException("Bean source type from alternative ["+alternativeInstance+"] can't be null");
        }
        String source = alternativeInstance.getSource();
        if(source==null){
            throw new FrameworkException("Bean source from alternative ["+alternativeInstance+"] can't be null");
        }
        if(sourceType.equals(BeanSourceType.CLASS)) {
            Class sourceClass = null;
            try {
                sourceClass = ClassFinder.loadClass(source);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            if (sourceClass == null)
                throw new FrameworkException("Bean source [" + source + "] from alternative [" + alternativeInstance + "] not found");
            if(AnnotationTools.getAnnotation(sourceClass, Alternative.class)==null)
                throw new FrameworkException("Bean source [" + source + "] from alternative [" + alternativeInstance + "] is not annotated with @Alternative");
        }else if (sourceType.equals(BeanSourceType.METHOD)) {
            String sourceClassName = null;
            String methodName = null;
            Class sourceClass = null;
            try {
                sourceClassName = source.substring(0, source.lastIndexOf("."));
                sourceClass = ClassFinder.loadClass(sourceClassName);
                methodName = source.substring(source.lastIndexOf(".")+1);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            if (sourceClass == null)
                throw new FrameworkException("Bean source [" + sourceClassName + "] from alternative [" + alternativeInstance + "] not found");
            if(methodName==null || methodName.isEmpty())
                throw new FrameworkException("Bean source method name [" + methodName + "] from alternative [" + alternativeInstance + "] not valid");
            final String finalMethodName = methodName;
            Method method = Stream
                    .of(sourceClass.getMethods())
                    .filter(method1 ->
                            method1.getName().equals(finalMethodName) && method1.getReturnType().isAssignableFrom(beanType)
                    )
                    .findFirst()
                    .orElse(null);
            if(method == null)
                throw new FrameworkException("Bean source factory doesn't exists or return type is not assignable to bean type ["+beanType.getCanonicalName()+"]");
            if(AnnotationTools.getAnnotation(method, Alternative.class)==null)
                throw new FrameworkException("Bean source factory [" + finalMethodName + "] from alternative [" + alternativeInstance + "] is not annotated with @Alternative");
        }else {
            throw new FrameworkException("Bean source type ["+sourceType+"] is unknown,\n" +
                    "Bean source type must be in ["+BeanSourceType.CLASS+", "+BeanSourceType.METHOD+"]");
        }
        return true;
    }
}
