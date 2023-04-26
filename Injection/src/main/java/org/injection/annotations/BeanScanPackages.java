package org.injection.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to filter packages and classes to be scanned for beans
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BeanScanPackages {
    String[] packages() default "";
    String[] excludes() default ""; // classes to exclude (regular expressions)
}
