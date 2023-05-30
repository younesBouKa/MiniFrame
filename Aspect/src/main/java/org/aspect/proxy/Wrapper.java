package org.aspect.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspect.processor.AspectProcessor;
import org.aspect.processor.AspectProcessorImpl;
import org.aspect.scanners.AspectScanManager;
import org.aspect.scanners.AspectScanManagerImpl;

public class Wrapper {
    private static final Logger logger = LogManager.getLogger(Wrapper.class);
    private AspectProcessor aspectProcessor;
    private ProxyType defaultProxyType = ProxyType.JDK;

    public static Wrapper init(){
        Wrapper wrapper = new Wrapper();
        wrapper.setAspectProcessor(new AspectProcessorImpl(new AspectScanManagerImpl()));
        return wrapper;
    }

    public static Wrapper init(AspectScanManager othetAspectScanManager){
        Wrapper wrapper = new Wrapper();
        wrapper.setAspectProcessor(new AspectProcessorImpl(othetAspectScanManager));
        return wrapper;
    }

    public static Wrapper init(AspectProcessor otherAspectProcessor){
        Wrapper wrapper = new Wrapper();
        wrapper.setAspectProcessor(otherAspectProcessor);
        return wrapper;
    }

    public void setAspectScanManager(AspectScanManager otherAspectScanManager){
        AspectProcessor aspectProcessor = getAspectProcessor();
        aspectProcessor.setAspectScanManager(otherAspectScanManager);
        AopMethodDynamicProxy.setAspectProcessor(aspectProcessor);
        AopMethodCglibProxy.setAspectProcessor(aspectProcessor);
        AopMethodByteBuddyProxy.setAspectProcessor(aspectProcessor);
    }

    public void setAspectProcessor(AspectProcessor otherAspectProcessor){
        aspectProcessor = otherAspectProcessor;
        AopMethodDynamicProxy.setAspectProcessor(otherAspectProcessor);
        AopMethodCglibProxy.setAspectProcessor(otherAspectProcessor);
        AopMethodByteBuddyProxy.setAspectProcessor(otherAspectProcessor);
    }

    public AspectProcessor getAspectProcessor(){
        return aspectProcessor;
    }

    public ProxyType getDefaultProxyType() {
        return defaultProxyType;
    }

    public void setDefaultProxyType(ProxyType defaultProxyType) {
        this.defaultProxyType = defaultProxyType;
    }

    public Object wrap(Object instance){
        return wrap(instance, defaultProxyType);
    }

    public Object wrap(Object instance, ProxyType proxyType){
        // JDK proxy work only for classes implementing interfaces, I should look for CGLIB proxy for other class
        Class<?> targetClass = instance.getClass();
        boolean implementingAnyInterface = targetClass.isInterface()
                || targetClass.getInterfaces().length>0;
        if(proxyType == ProxyType.CGLIB ||
                (proxyType == ProxyType.JDK && !implementingAnyInterface)){
            return AopMethodCglibProxy.newInstance(instance);
        } else if (proxyType == ProxyType.ASM) {
            logger.warn("ASM proxy not yet implemented");
            return AopMethodByteBuddyProxy.newInstance(instance);
        }else{
            return AopMethodDynamicProxy.newInstance(instance);
        }
    }
}
