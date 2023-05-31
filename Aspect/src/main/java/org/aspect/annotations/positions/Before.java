package org.aspect.annotations.positions;

import org.aspect.annotations.Position;
import org.aspect.annotations.enums.ExecPosition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Position(execPosition = ExecPosition.BEFORE)
@Target({
        ElementType.METHOD,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Before {
}
