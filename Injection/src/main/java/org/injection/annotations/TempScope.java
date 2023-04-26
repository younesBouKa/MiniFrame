package org.injection.annotations;

import javax.inject.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A custom scope annotation used in bean building process
 * a good example of how to create customized scope annotations
 * see ControlledScope for more details
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface TempScope {
}
