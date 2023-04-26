package org.injection.enums;

import java.lang.reflect.Method;

public enum BeanSourceType {
    CLASS,
    METHOD;
    public boolean isMatching(Object beanSource, String sourceName){
        if(sourceName==null)
            return false;
        return sourceName
                .equals(formatSourceName(beanSource));
    }
    private String formatSourceName(Object beanSource){
        switch (this){
            case CLASS:{
                if(beanSource instanceof Class){
                    Class aClass = (Class) beanSource;
                    String classAlternativeKey = aClass.getCanonicalName();
                    return classAlternativeKey;
                }
                return null;
            }
            case METHOD:{
                if(beanSource instanceof Method){
                    Method method = (Method) beanSource;
                    String methodAlternativeKey = method.getDeclaringClass().getCanonicalName()+"."+method.getName();
                    return methodAlternativeKey;
                }
                return null;
            }
            default:
                return null;
        }
    }
}
