package org.injection.core.data;

import java.util.Objects;

public class BeanInstance implements Comparable<BeanInstance>{
    private Class<?> scopeType;
    private Object scopeId;
    private Class<?> type;
    private Object instance;
    private Object source;

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public Object getScopeId() {
        return scopeId;
    }

    public void setScopeId(Object scopeId) {
        this.scopeId = scopeId;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public Class<?> getScopeType() {
        return scopeType;
    }

    public void setScopeType(Class<?> scopeType) {
        this.scopeType = scopeType;
    }

    @Override
    public String toString() {
        return "BeanInstance{" +
                "type=" + type +
                ", instance=" + instance +
                ", scopeType=" + scopeType +
                ", scopeId=" + scopeId +
                ", source=" + source +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeanInstance)) return false;
        BeanInstance that = (BeanInstance) o;
        return getScopeId().equals(that.getScopeId())
                && getType().equals(that.getType())
                && getInstance().equals(that.getInstance());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScopeId(), getType(), getInstance());
    }

    @Override
    public int compareTo(BeanInstance o) {
        if (this.equals(o))
            return 0;
        if(o!=null)
            return String.valueOf(hashCode())
                    .compareTo(String.valueOf(o.hashCode()));
        return 1;
    }
}
