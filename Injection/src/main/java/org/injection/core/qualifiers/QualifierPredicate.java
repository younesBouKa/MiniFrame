package org.injection.core.qualifiers;

import java.lang.annotation.Annotation;

/**
 * Functional interface use to create custom qualifiers
 * see @QualifierConfig
 */
public interface QualifierPredicate{
    /**
     * Test if bean source match this custom qualifier
     * You should test on annotation type and bean source type
     * @param annotation : a qualifier annotation
     * @param beanSource : can be Class or method (bean factory), ..
     * @return
     */
    boolean accept(Annotation annotation, Object beanSource);
}
