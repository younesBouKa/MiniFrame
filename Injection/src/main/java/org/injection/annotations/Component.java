package org.injection.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The base bean annotation of Dependency injection
 * A class annotated with @Component will be used as implementation of bean
 * A method annotated with this annotation provide bean implementation
 */
@Target(value = {
        ElementType.METHOD,
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
}
