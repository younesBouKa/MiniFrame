package org.injection.annotations.qualifiers.markers;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Evaluate qualifiers with AND
 */
@Qualifier
@Target({
        ElementType.FIELD,
        ElementType.PARAMETER,
        ElementType.TYPE_PARAMETER,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface EvalWithAND {
}