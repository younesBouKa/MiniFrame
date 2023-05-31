package org.aspect.annotations.pointcuts;

import org.aspect.annotations.CutPoint;
import org.aspect.annotations.enums.CutPointType;

import java.lang.annotation.*;


@CutPoint(cutPointType = CutPointType.ANNOTATION)
@Target({
        ElementType.METHOD,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotatedWith {
    Class<? extends Annotation> annotationClass();
}
