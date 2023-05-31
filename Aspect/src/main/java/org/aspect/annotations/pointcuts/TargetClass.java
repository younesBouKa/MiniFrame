package org.aspect.annotations.pointcuts;

import org.aspect.annotations.CutPoint;
import org.aspect.annotations.enums.CutPointType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@CutPoint(cutPointType = CutPointType.CLASS_REGEX)
@Target({
        ElementType.METHOD,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface TargetClass{
    Class<?> target();
}
