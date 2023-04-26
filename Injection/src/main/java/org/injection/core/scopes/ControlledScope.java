package org.injection.core.scopes;

import org.tools.Log;
import org.tools.exceptions.FrameworkException;

import java.lang.annotation.Annotation;

public class ControlledScope implements ScopeLifeCycle {
    private static final Log logger = Log.getInstance(ControlledScope.class);
    private final Class<? extends Annotation> scopeType;

    public ControlledScope(Class<? extends Annotation> scopeType){
        if(scopeType==null)
            throw new FrameworkException("Controlled scope type can't be null");
        this.scopeType = scopeType;
    }

    public boolean createScopeInstance(Object scopeInstance){
        if (scopeInstance==null){
            logger.error("Scope Instance can't be null");
            return false;
        }
        boolean scopeAlreadyExists = ScopeLifeCycle.scopeExists(scopeType, scopeInstance);
        if (scopeAlreadyExists){
            logger.error("Scope instance ["+scopeInstance+"] of type ["+scopeType+"] already exist");
            return false;
        }
        return scopeInitialized(scopeInstance);
    }

    public boolean destroyScopeInstance(Object scopeInstance){
        if (scopeInstance==null){
            logger.error("Scope Instance can't be null");
            return false;
        }
        boolean scopeExists = ScopeLifeCycle.scopeExists(scopeType, scopeInstance);
        if (!scopeExists){
            logger.error("Scope instance ["+scopeInstance+"] of type ["+scopeType+"] doesn't exist");
            return false;
        }
        return scopeDestroyed(scopeInstance);
    }

    @Override
    public Class<? extends Annotation> getScopeType() {
        return scopeType;
    }
}
