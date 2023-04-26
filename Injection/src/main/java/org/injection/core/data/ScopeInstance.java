package org.injection.core.data;


import org.tools.exceptions.FrameworkException;

public class ScopeInstance {
    private final Class<?> scopeType;
    private final Object scopeId;

    public ScopeInstance(Class<?> scopeType, Object scopeId) {
        if(scopeType==null || scopeId==null)
            throw new FrameworkException("Scope type and scope Id can't be null in scope instance");
        this.scopeType = scopeType;
        this.scopeId = scopeId;
    }

    public Class<?> getScopeType() {
        return scopeType;
    }

    public Object getScopeId() {
        return scopeId;
    }

}
