package org.injection.core.data;

import org.injection.enums.BeanSourceType;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;

public class BeanConfig {
    Class<?> beanType;
    BeanSourceType sourceType;
    Object source;
    boolean isAlternative;
    Set<Annotation> qualifiers;

    public BeanConfig(Class<?> beanType, BeanSourceType sourceType, Object source) {
        this.beanType = beanType;
        this.sourceType = sourceType;
        this.source = source;
    }

    public BeanConfig(Class<?> beanType, BeanSourceType sourceType, Object source, Set<Annotation> qualifiers) {
        this.beanType = beanType;
        this.sourceType = sourceType;
        this.source = source;
        this.qualifiers = qualifiers;
    }

    public BeanConfig(Class<?> beanType, BeanSourceType sourceType, Object source, boolean isAlternative) {
        this.beanType = beanType;
        this.sourceType = sourceType;
        this.source = source;
        this.isAlternative = isAlternative;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

    public void setBeanType(Class<?> beanType) {
        this.beanType = beanType;
    }

    public BeanSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(BeanSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public boolean isAlternative() {
        return isAlternative;
    }

    public void setAlternative(boolean alternative) {
        isAlternative = alternative;
    }

    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(Set<Annotation> qualifiers) {
        this.qualifiers = qualifiers;
    }

    @Override
    public String toString() {
        return "BeanConfig{" +
                "beanType=" + beanType +
                ", sourceType=" + sourceType +
                ", source=" + source +
                ", isAlternative=" + isAlternative +
                ", qualifiers=" + qualifiers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeanConfig)) return false;
        BeanConfig that = (BeanConfig) o;
        return getBeanType().equals(that.getBeanType())
                && getSourceType() == that.getSourceType()
                && getSource().equals(that.getSource())
                && Objects.equals(getQualifiers(), that.getQualifiers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBeanType(), getSourceType(), getSource(), isAlternative(), getQualifiers());
    }
}
