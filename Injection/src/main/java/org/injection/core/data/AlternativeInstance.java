package org.injection.core.data;

import org.injection.enums.BeanSourceType;

public class AlternativeInstance {
    Class<?> beanType;
    String source;
    BeanSourceType sourceType;

    public AlternativeInstance(Class<?> beanType, String source, BeanSourceType sourceType) {
        this.beanType = beanType;
        this.source = source;
        this.sourceType = sourceType;
    }

    public AlternativeInstance(Class<?> beanType, Class<?> source) {
        this.beanType = beanType;
        this.source = source.getCanonicalName();
        this.sourceType = BeanSourceType.CLASS;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

    public void setBeanType(Class<?> beanType) {
        this.beanType = beanType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public BeanSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(BeanSourceType sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public String toString() {
        return "AlternativeInstance{" +
                "beanType=" + beanType +
                ", source=" + source +
                ", sourceType='" + sourceType + '\'' +
                '}';
    }
}
