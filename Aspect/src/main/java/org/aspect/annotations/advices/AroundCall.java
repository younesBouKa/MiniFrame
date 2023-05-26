package org.aspect.annotations.advices;

import org.aspect.annotations.AdviceMarker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@AdviceMarker
@Target({
        ElementType.METHOD,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface AroundCall {
    String methodSignature();

    int order() default 1;
}
