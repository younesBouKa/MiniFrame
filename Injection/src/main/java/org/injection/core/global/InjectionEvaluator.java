package org.injection.core.global;

import org.tools.annotations.AnnotationTools;
import org.tools.exceptions.FrameworkException;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public interface InjectionEvaluator {
    default boolean isInjectable(Object obj){
        return AnnotationTools.isAnnotationPresent(obj, Inject.class);
    }

    /**
     * Injectable fields:
     *                 are annotated with @Inject.
     *                 are not final.
     *                 may have any otherwise valid name.
     *           Ex:
     *             @Inject FieldModifiers opt Type VariableDeclarators;
     * @param field
     * @return
     */
    default boolean isValidInjectedField(Field field){
        boolean isFinal = Modifier.isFinal(field.getModifiers());
        boolean isInjectable = isInjectable(field);
        boolean isInterface =  field.getType().isInterface();
        if(isFinal)
            return false;
        if(!isInjectable)
            return false;
        if(!isInterface){
            throw new FrameworkException(
                    "Field ["+field.toGenericString()+"] annotated is Injectable but not an interface"
            );
        }
        return true;
    }

    /**
     * Injectable methods:
     *                 are annotated with @Inject.
     *                 are not abstract.
     *                 do not declare type parameters of their own. ( from java injection spec)
     *                 may return a result
     *                 may have any otherwise valid name.
     *                 accept zero or more dependencies as arguments.
     *             EX:
     *                 @Inject MethodModifiers opt ResultType Identifier(FormalParameterListopt) Throwsopt MethodBody
     * @param method
     * @return
     */
    default boolean isValidInjectedMethod(Method method){
        boolean isInjectable = isInjectable(method);
        boolean isAbstract = Modifier.isAbstract(method.getModifiers());
        if(isInjectable && isAbstract)
            throw new FrameworkException(
                    "Injectable method "+method.toGenericString()+" can't be abstract"
            );
        if(isAbstract)
            return false;
        return isInjectable;
    }

}
