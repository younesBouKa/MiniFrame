package org.aspect.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspect.processor.AspectProcessor;
import org.aspect.processor.AspectProcessorImpl;

public class Wrapper {
    private static final Logger logger = LogManager.getLogger(Wrapper.class);
    private static AspectProcessor aspectProcessor = new AspectProcessorImpl();
    private static ProxyType defaultProxyType = ProxyType.JDK;

    public static void setAspectProcessor(AspectProcessor otherAspectProcessor){
        aspectProcessor = otherAspectProcessor;
        AopMethodDynamicProxy.setAspectProcessor(otherAspectProcessor);
        AopMethodCglibProxy.setAspectProcessor(otherAspectProcessor);
    }

    public static AspectProcessor getAspectProcessor(){
        return aspectProcessor;
    }

    public static ProxyType getDefaultProxyType() {
        return defaultProxyType;
    }

    public static void setDefaultProxyType(ProxyType defaultProxyType) {
        Wrapper.defaultProxyType = defaultProxyType;
    }

    public static Object wrap(Object instance){
        return wrap(instance, defaultProxyType);
    }

    public static Object wrap(Object instance, ProxyType proxyType){
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
