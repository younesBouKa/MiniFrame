package org.injection;

import org.injection.core.alternatives.AlternativeManager;
import org.injection.core.alternatives.AlternativeManagerImpl;
import org.injection.core.global.BeanContainer;
import org.injection.core.global.BeanContainerImpl;
import org.injection.core.global.BeansClassPool;
import org.injection.core.global.ClassPool;
import org.injection.core.listeners.BeanLifeCycle;
import org.injection.core.listeners.ContainerLifeCycle;
import org.injection.core.qualifiers.BeanQualifierManager;
import org.injection.core.qualifiers.BeanQualifierManagerImpl;
import org.injection.core.scan.BeanScanManager;
import org.injection.core.scan.BeanScanManagerImpl;
import org.injection.core.scopes.ScopeManager;
import org.injection.core.scopes.ScopeManagerImpl;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;

public class InjectionConfig {
    private static final Log logger = Log.getInstance(InjectionConfig.class);
    private  ClassPool classPool;
    private  BeanScanManager beanScanManager;
    private  BeanQualifierManager beanQualifierManager;
    private  AlternativeManager alternativeManager;
    private  ScopeManager scopeManager;
    private  BeanContainer beanContainer;
    private  BeanLifeCycle beanLifeCycle;
    private  ContainerLifeCycle containerLifeCycle;

    private static InjectionConfig defaultInstance;

    public static InjectionConfig getDefaultInstance(){
        if(defaultInstance==null){
            defaultInstance = new InjectionConfig();
        }
        return defaultInstance;
    }

    /* ------------------------- getters -----------------------------*/
    public ClassPool getClassPool() {
        if(classPool==null)
            classPool = new BeansClassPool();
        return classPool;
    }
    public ScopeManager getScopeManager() {
        if(scopeManager==null)
            scopeManager = new ScopeManagerImpl();
        return scopeManager;
    }
    public BeanLifeCycle getBeanLifeCycle() {
        if(beanLifeCycle==null)
            beanLifeCycle = new BeanLifeCycle() {
                private final Log logger = Log.getInstance(BeanLifeCycle.class);
                @Override
                public void onEvent(String eventType, Object... args) {
                    //logger.infoF("Bean Life Cycle event: %s, args: %s", eventType, args);
                }
            };
        return beanLifeCycle;
    }
    public AlternativeManager getAlternativeManager() {
        if(alternativeManager==null)
            alternativeManager = new AlternativeManagerImpl();
        return alternativeManager;
    }
    public BeanQualifierManager getBeanQualifierManager() {
        if(beanQualifierManager==null)
            beanQualifierManager = new BeanQualifierManagerImpl();
        return beanQualifierManager;
    }
    public BeanScanManager getBeanScanManager() {
        if(beanScanManager==null)
            beanScanManager = new BeanScanManagerImpl(
                    getClassPool(),
                    getAlternativeManager(),
                    getBeanQualifierManager()
            );
        return beanScanManager;
    }
    public ContainerLifeCycle getContainerLifeCycle() {
        if(containerLifeCycle==null)
            containerLifeCycle = new ContainerLifeCycle(){
                private final Log logger = Log.getInstance(BeanLifeCycle.class);
                @Override
                public void onEvent(String eventType, Object... args) {
                    //logger.infoF("Container Life Cycle event: %s, args: %s", eventType, args);
                }
            };
        return containerLifeCycle;
    }
    public BeanContainer getBeanContainer() {
        if(beanContainer==null)
            beanContainer = new BeanContainerImpl(getContainerLifeCycle());
        return beanContainer;
    }
    /*----------------------- Setters ------------------------------------*/
    public void setClassPool(ClassPool classPool) {
        if(classPool==null)
            throw new FrameworkException("Class Pool can't be null");
        this.classPool = classPool;
    }
    public void setAlternativeManager(AlternativeManager alternativeManager) {
        if(alternativeManager==null)
            throw new FrameworkException("Alternative manager can't be null");
        this.alternativeManager = alternativeManager;
    }
    public void setContainerLifeCycle(ContainerLifeCycle containerLifeCycle) {
        if(containerLifeCycle==null)
            throw new FrameworkException("Container event dispatcher can't be null");
        this.containerLifeCycle = containerLifeCycle;
    }
    public void setBeanLifeCycle(BeanLifeCycle beanLifecycle) {
        if(beanLifecycle==null)
            throw new FrameworkException("Bean Life cycle manager can't be null");
        this.beanLifeCycle = beanLifecycle;
    }
    public void setBeanContainer(BeanContainer beanContainer) {
        if(beanContainer==null)
            throw new FrameworkException("Bean Container can't be null");
        this.beanContainer = beanContainer;
    }
    public void setScopeManager(ScopeManager scopeManager) {
        if(scopeManager==null)
            throw new FrameworkException("Scope Manager can't be null");
        this.scopeManager = scopeManager;
    }
    public void setBeanQualifierManager(BeanQualifierManager beanQualifierManager) {
        if(beanQualifierManager==null)
            throw new FrameworkException("Bean Qualifier Manager can't be null");
        this.beanQualifierManager = beanQualifierManager;
    }
    public void setBeanScanManager(BeanScanManager beanScanManager) {
        if(beanScanManager==null)
            throw new FrameworkException("Bean Scan Manager can't be null");
        this.beanScanManager = beanScanManager;
    }
}
