package org.injection.annotations;

import java.lang.annotation.*;

/**
 * We can customize a qualifier by implementing org.injection.core.qualifiers.QualifierPredicate interface
 * and annotate a config class with @org.injection.annotations.QualifierConfig
 * For example if you have to qualify implementations based on type
 * 1- create an annotation "TypeQualifier" (must be annotated with @javax.inject.Qualifier)
 * 2- create a class that implement QualifierPredicate
 * 3- annotated this class with @org.injection.annotations.QualifierConfig passing 'TypeQualifier.class' as qualifierAnnotation
 * 4- then put you logic in the accept method of this class
 * 5- finally you can use your @TypeQualifier as customized qualifier on any injection point
 */
@Target({
        ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface QualifierConfig {
    Class<? extends Annotation> qualifierAnnotation();
}
