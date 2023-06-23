package org.aspect.annotations.types;

import org.aspect.annotations.Type;
import org.aspect.annotations.enums.AdviceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Type(adviceType = AdviceType.CALL)
@Target({
        ElementType.METHOD,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Call {
}
