package org.aspect.annotations;

import org.aspect.annotations.enums.AdviceType;
import org.aspect.annotations.enums.CutPointType;
import org.aspect.annotations.enums.ExecPosition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Advice {
    AdviceType adviceType() default AdviceType.CALL;
    ExecPosition execPosition() default ExecPosition.BEFORE;
    CutPointType cutPointType() default CutPointType.METHOD_REGEX;
    String cutPointValue();
    int order() default 1;
}
