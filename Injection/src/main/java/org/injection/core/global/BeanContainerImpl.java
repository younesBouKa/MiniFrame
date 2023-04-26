package org.injection.core.global;

import org.injection.core.listeners.ContainerLifeCycle;
import org.injection.core.data.BeanInstance;
import org.injection.core.scopes.DefaultScopeLifeCycleEvent;
import org.injection.core.scopes.LifeCycleEventType;
import org.injection.core.scopes.ScopeLifeCycle;
import org.tools.Log;

import java.util.EventObject;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BeanContainerImpl implements BeanContainer{
    private static final Log logger = Log.getInstance(BeanContainerImpl.class);
    private final Set<BeanInstance> cachedBeans = new ConcurrentSkipListSet<>();
    private ContainerLifeCycle containerLifeCycle;

    public BeanContainerImpl(){
        this(null);
    }

    public BeanContainerImpl(ContainerLifeCycle containerLifeCycle){
        this.containerLifeCycle = containerLifeCycle;
        ScopeLifeCycle.registerListener(this::onScopeEvent);
    }

    @Override
    public void setContainerLifeCycle(ContainerLifeCycle containerLifeCycle) {
        this.containerLifeCycle = containerLifeCycle;
    }

    @Override
    public ContainerLifeCycle getContainerLifeCycle() {
        return containerLifeCycle;
    }

    public Set<BeanInstance> getBeansWithFilter(Predicate<BeanInstance> filter){
        return cachedBeans.stream()
                .filter(filter)
                .collect(Collectors.toSet());
    }

    public void addBean(Class<?> beanType, Object bean,Class<?> scopeType, Object scopeId, Object source){
        if (scopeId==null){
            logger.error("Scope Id can't be null");
            return;
        }
        if (scopeType==null){
            logger.error("Scope type can't be null");
            return;
        }
        if (beanType==null){
            logger.error("Bean type can't be null");
            return;
        }
        if (bean==null){
            logger.error("Bean object can't be null");
            return;
        }
        int cacheSize = cachedBeans.size();
        if(cacheSize >= getWarnSizeThreshold()  && cacheSize< getErrorSizeThreshold())
            logger.warn("Container size ["+cacheSize+"] reached warn threshold ["+ getWarnSizeThreshold() +"]");
        if(cacheSize >= getErrorSizeThreshold()){
            logger.error("Container size ["+cacheSize+"] exceed error threshold ["+ getErrorSizeThreshold() +"]");
            return;
        }
        logger.info("Adding bean to container, beanType:["+beanType+"], scopeType:["+scopeType+"], scopeId: ["+scopeId+"]");
        logger.infoF("Current available scope instances : %s", ScopeLifeCycle.getCache());
        logger.infoF("Bean Container size before adding bean of type ["+beanType+"] : %s beans", cachedBeans.size());
        boolean scopeTypeExists = ScopeLifeCycle.getCache().containsKey(scopeType);
        boolean scopeIdExists = scopeTypeExists
                && ScopeLifeCycle.getCache().get(scopeType).contains(scopeId);
        if(!scopeTypeExists){
            logger.error("Scope of type ["+scopeType+"] doesn't exist, check Scope life cycle implementations");
            return;
        }
        if(!scopeIdExists){
            logger.error("Scope instance ["+scopeId+"] of type ["+scopeType+"] was destroyed or doesn't exist");
            return;
        }
        BeanInstance beanInstance = new BeanInstance();
        beanInstance.setInstance(bean);
        beanInstance.setType(beanType);
        beanInstance.setSource(source);
        beanInstance.setScopeId(scopeId);
        beanInstance.setScopeType(scopeType);
        boolean added;
        sendEvent(ContainerLifeCycle.preAddBean, beanType, bean, scopeType, scopeId, source);
        synchronized (cachedBeans){
            added = cachedBeans.add(beanInstance);
        }
        if(added){
            logger.infoF("Bean instance added to container: %s", beanInstance);
            sendEvent(ContainerLifeCycle.postAddBean, beanType, bean, scopeType, scopeId, source);
            logger.infoF("Bean Container after adding bean of type ["+beanType+"], size: %d", cachedBeans.size());
        }
        else
            logger.infoF("Bean already exist in container: %s", beanInstance);
    }

    public synchronized Object getBean(Class<?> beanType, Class<?> scopeType, Object scopeId){
        if (scopeId==null){
            logger.error("Scope Id can't be null");
            return null;
        }
        if (scopeType==null){
            logger.error("Scope type can't be null");
            return null;
        }
        if (beanType==null){
            logger.error("Bean type can't be null");
            return null;
        }
        logger.info("Getting bean from container with size ["+cachedBeans.size()+"] beans, beanType:["+beanType+"], scopeType:["+scopeType+"], scopeId: ["+scopeId+"]");
        logger.infoF("Current available scope instances : %s", ScopeLifeCycle.getCache());
        boolean scopeTypeExists = ScopeLifeCycle.getCache().containsKey(scopeType);
        boolean scopeIdExists = scopeTypeExists
                && ScopeLifeCycle.getCache().get(scopeType).contains(scopeId);
        if(!scopeTypeExists){
            logger.error("Scope of type ["+scopeType+"] doesn't exist, check Scope life cycle implementations");
            return null;
        }
        if(!scopeIdExists){
            logger.error("Scope instance ["+scopeId+"] of type ["+scopeType+"] was destroyed or doesn't exist");
            return null;
        }
        for (BeanInstance beanInstance : cachedBeans){
            if(beanType.isAssignableFrom(beanInstance.getType())
                    && beanInstance.getScopeId()!=null
                    && beanInstance.getScopeId().equals(scopeId)
                    && beanInstance.getScopeType() !=null
                    && beanInstance.getScopeType().isAssignableFrom(scopeType)
            )
                return beanInstance.getInstance();
        }
        return null;
    }

    public boolean removeBean(Class<?> beanType, Object bean, Object scopeId){
        if (beanType==null){
            logger.error("Bean type can't be null");
            return false;
        }
        if (bean==null){
            logger.error("Can't remove null bean");
            return false;
        }
        if (scopeId==null){
            logger.error("Scope Id can't be null");
            return false;
        }
        for (BeanInstance beanInstance : cachedBeans){
            if(
                    beanType.isAssignableFrom(beanInstance.getType())
                            && beanInstance.getInstance().equals(bean)
                            && beanInstance.getScopeId().equals(scopeId)
            ){
                boolean removed;
                sendEvent(ContainerLifeCycle.preRemoveBean, beanType, bean, scopeId);
                synchronized (cachedBeans){
                    removed = cachedBeans.remove(beanInstance);
                }
                if(removed){
                    logger.infoF("Remove bean instance from container: %s", beanInstance);
                    sendEvent(ContainerLifeCycle.postRemoveBean, beanType, bean, beanInstance, scopeId); // TODO call @PreDestroy
                    return true;
                }else{
                    logger.infoF("Can't remove bean instance from container: %s", beanInstance);
                    return false;
                }
            }
        }
        return false;
    }

    public int removeBeans(Class<?> scopeType, Object scopeId){
        int removedBeansCount = 0;
        if (scopeId != null) {
            synchronized (this) {
                for (BeanInstance beanInstance : cachedBeans) {
                    if (
                            beanInstance.getScopeType()!=null
                                    && beanInstance.getScopeType().isAssignableFrom(scopeType)
                                    && beanInstance.getScopeId() != null
                                    && beanInstance.getScopeId().equals(scopeId)
                    ) {
                        sendEvent(ContainerLifeCycle.preRemoveBeans, beanInstance, scopeType,  scopeId);
                        boolean removed = cachedBeans.remove(beanInstance);
                        if (removed){
                            // TODO call @PreDestroy
                            logger.debug("Remove bean instance ["+beanInstance+"] with scopeId ["+scopeId+"] from container");
                            sendEvent(ContainerLifeCycle.postRemoveBeans, beanInstance, scopeType, scopeId);
                            removedBeansCount++;
                        }
                    }
                }
            }
        }
        return removedBeansCount;
    }

    public void onScopeEvent(EventObject eventObject){
        if(eventObject!=null && eventObject.getSource() instanceof DefaultScopeLifeCycleEvent){
            DefaultScopeLifeCycleEvent defaultScopeLifeCycleEvent = (DefaultScopeLifeCycleEvent)eventObject.getSource();
            if(defaultScopeLifeCycleEvent.getEventType().equals(LifeCycleEventType.DESTROYED)){
                Object scopeId = defaultScopeLifeCycleEvent.getScopeInstance();
                Class<?> scopeType = defaultScopeLifeCycleEvent.getScopeType();
                logger.debug("Scope instance ["+scopeId+"] of type ["+scopeType+"] destroyed, start removing beans with this scope id");
                try {
                    int removedBeans = removeBeans(scopeType, scopeId);
                    logger.debugF("["+removedBeans+"] bean instances removed from container, with scopeId ["+scopeId+"] of type ["+scopeType+"]");
                }catch (Exception e){
                    logger.error("Error while removing bean instance with scope id ["+scopeId+"]");
                    logger.error(e);
                }
                logger.infoF("Bean Container size after ["+scopeId+"] scope destroyed,  %d beans", cachedBeans.size());
            }
        }
    }

    private void sendEvent(String eventType, Object ...args){
        // call on destroy method if exist
        if(ContainerLifeCycle.preRemoveBeans.equals(eventType)
                || ContainerLifeCycle.preRemoveBean.equals(eventType)){
            Object instance = args[0];

        }

        ContainerLifeCycle containerLifeCycleTemp = getContainerLifeCycle();
        if(containerLifeCycleTemp!=null){
            Thread eventThread = new Thread(()->{
                containerLifeCycleTemp
                        .onEvent(eventType, args);
            });
            eventThread.setDaemon(true);
            eventThread.start();
        }
    }

}

