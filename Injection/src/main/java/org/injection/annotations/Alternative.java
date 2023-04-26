package org.injection.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Select bean implementation based on some config file
 * It help in bean selection on deployment level
 */
@Component
@Target({
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Alternative {
}