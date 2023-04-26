package org.injection.core.scopes;

import org.tools.ClassFinder;
import org.tools.Log;
import org.tools.annotations.AnnotationTools;
import org.tools.exceptions.FrameworkException;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Set;

public class ScopeManagerImpl implements ScopeManager {
    private static final Log logger = Log.getInstance(ScopeManagerImpl.class);
    private static final ControlledScope singletonScope = new ControlledScope(Singleton.class);
    private final Set<Class> availableScopes = new HashSet<>();
    private long updateCount = 0;
    private long lastUpdateTimeStamp = 0;
    private Class<? extends Annotation> defaultScopeType;

    static {
        boolean singletonScopeCreated = singletonScope
                .createScopeInstance(Singleton.class.getCanonicalName());
        if(!singletonScopeCreated)
            logger.error("Can't create singleton scope instance");
        else
            logger.debug("Singleton scope instance created");
    }

    public ScopeManagerImpl(){
        //update(true); // TODO for optimisation test
    }

    @Override
    public void setDefaultScopeType(Class<? extends Annotation> defaultScopeType) {
        if(defaultScopeType!=null && !isValidScopeAnnotation(defaultScopeType))
            throw new FrameworkException("Default Scope Type "+defaultScopeType+" is not valid");
        this.defaultScopeType = defaultScopeType;
    }

    @Override
    public Class<? extends Annotation> getDefaultScopeType(Class<?> beanType) {
        return this.defaultScopeType;
    }

    public Set<Class> getAvailableScopes(){
        update(false);
        return availableScopes;
    }

    public boolean addScopeAnnotation(Class<? extends Annotation> scopeAnnotation){
        if(!isValidScopeAnnotation(scopeAnnotation)){
            logger.error("Scope annotation ["+scopeAnnotation+"] is not valid");
            return false;
        }
        synchronized (availableScopes){
            return availableScopes.add(scopeAnnotation);
        }
    }

    public synchronized boolean removeScopeAnnotation(Class<? extends Annotation> scopeAnnotation){
        if(!availableScopes.isEmpty()){
            synchronized (availableScopes){
                return availableScopes.remove(scopeAnnotation);
            }
        }
        return false;
    }

    public Class<? extends Annotation> getParameterScope(Parameter parameter) {
        return getScope(parameter);
    }

    public Class<? extends Annotation> getFieldScope(Field field) {
        return getScope(field);
    }

    public Class<? extends Annotation> getClassScope(Class clazz) {
        return getScope(clazz);
    }

    public Class<? extends Annotation> getMethodScope(Method method) {
        return getScope(method);
    }

    /* ------------------------------ inner methods -----------------------------*/
    public void update(boolean force){
        long lastUpdate = ClassFinder.getLastUpdateTimeStamp();
        if(lastUpdateTimeStamp < lastUpdate || force){
            updateCount++;
            long duration = System.currentTimeMillis();
            logger.debug("Update Scope Manager cache for the ["+updateCount+"] time(s), last update timestamp ["+lastUpdateTimeStamp+"]");
            lastUpdateTimeStamp = lastUpdate;
            scanForAvailableScopes();
            checkScopeLifeCycleImplementations();
            logger.debug("Update Scope Manager cache ended in ["+(System.currentTimeMillis() - duration)+"] Millis with ["+availableScopes.size()+"] scope(s)");
            if(availableScopes.isEmpty())
                logger.error("No scope class was found.");
        }
    }

    private void scanForAvailableScopes(){
        logger.info("Start scanning for available scope annotations ...");
        availableScopes.addAll(ClassFinder.getClassesWithFilter(this::isValidScopeAnnotation));
        availableScopes.add(Singleton.class);
        logger.info("Scanning for available scope annotations ended: "+availableScopes.size()+"");
    }

    private void checkScopeLifeCycleImplementations(){
        logger.info("Start checking for scope life cycle implementation, last update timestamp ["+lastUpdateTimeStamp+"]");
        Set<Class> implementations = ClassFinder
                .getClassesWithFilter(aClass -> {
            boolean implementScopeLifeCycle = ScopeLifeCycle.class.isAssignableFrom(aClass);
            boolean isNotAbstract = !Modifier.isAbstract(aClass.getModifiers());
            boolean isNotInterface = !Modifier.isInterface(aClass.getModifiers());
            return implementScopeLifeCycle && isNotAbstract && isNotInterface;
        });
        logger.info("Checking for scope life cycle implementation ended, founded implementations: "+implementations+"");
    }

    private Class<? extends Annotation> getScope(Object obj){
        for(Class scope : getAvailableScopes()){
            Annotation annotation = AnnotationTools.getAnnotation(obj, scope);
            if(annotation!=null)
                return scope;
        }
        return null;
    }
}

