package org.web.annotations.params.types;

import org.web.annotations.params.global.ParamSrc;
import org.web.annotations.params.global.Source;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Source(src = ParamSrc.BODY)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface BodyParam {
    String name();
}
