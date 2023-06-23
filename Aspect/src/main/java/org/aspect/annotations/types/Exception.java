package org.aspect.annotations.types;

import org.aspect.annotations.Type;
import org.aspect.annotations.enums.AdviceType;

import java.lang.annotation.*;


@Type(adviceType = AdviceType.EXCEPTION)
@Target({
        ElementType.METHOD,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Exception {
    Class<?>[] types() default Throwable.class;
}
