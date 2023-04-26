package org.demo.others;

import org.injection.annotations.QualifierConfig;
import org.injection.core.qualifiers.QualifierPredicate;

import java.lang.annotation.Annotation;

@QualifierConfig(qualifierAnnotation = ClassNameQualifier.class)
public class ClassNameQualifierPredicate implements QualifierPredicate {
    @Override
    public boolean accept(Annotation annotation, Object beanSource) {
        if(annotation instanceof ClassNameQualifier){
            ClassNameQualifier nameQualifier = (ClassNameQualifier) annotation;
            if(beanSource instanceof Class) {
                Class implementation = (Class) beanSource;
                return implementation.getCanonicalName().equals(nameQualifier.name());
            }
        }
        return false;
    }
}
