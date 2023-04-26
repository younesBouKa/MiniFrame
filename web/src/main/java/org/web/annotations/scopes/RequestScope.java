package org.web.annotations.scopes;

import javax.inject.Scope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Scope
@Target({
        ElementType.TYPE,
        ElementType.FIELD,
        ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestScope {
}
