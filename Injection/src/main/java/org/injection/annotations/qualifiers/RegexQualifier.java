package org.injection.annotations.qualifiers;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use regex to select implementation name
 * (class name/declaring class name of a factory method) to be injected
 */
@Qualifier
@Target({
        ElementType.FIELD,
        ElementType.PARAMETER,
        ElementType.TYPE_PARAMETER,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface RegexQualifier {
    String regex();
}