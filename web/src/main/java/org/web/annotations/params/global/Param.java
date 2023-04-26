package org.web.annotations.params.global;

import org.web.annotations.params.global.ParamSrc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    String name() default "";
    ParamSrc type();
}
