package org.injection.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method annotated with @AlternativeConfig will be used as source of alternatives definition
 * That method must return a set of org.injection.core.data.AlternativeInstance
 * and can't have any argument
 * [it preferable that the method is static for performance issues]
 */
@Target({
        ElementType.METHOD,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface AlternativeConfig {
}