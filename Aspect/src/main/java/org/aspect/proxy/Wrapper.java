package org.aspect.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspect.processor.AspectProcessor;
import org.aspect.processor.AspectProcessorImpl;

public class Wrapper {
    private static final Logger logger = LogManager.getLogger(Wrapper.class);
    private static AspectProcessor aspectProcessor = new AspectProcessorImpl();

    public static void setAspectProcessor(AspectProcessor otherAspectProcessor){
        aspectProcessor = otherAspectProcessor;
        AopMethodDynamicProxy.setAspectProcessor(otherAspectProcessor);
        AopMethodCglibProxy.setAspectProcessor(otherAspectProcessor);
    }

    public static AspectProcessor getAspectProcessor(){
        return aspectProcessor;
    }

    public static Object wrap(Object instance){
        // JDK proxy work only for classes implementing interfaces, I should look for CGLIB proxy for other class
        Class<?> targetClass = instance.getClass();
        boolean implementingAnyInterface = targetClass.isInterface() || targetClass.getInterfaces().length>0;
        if(implementingAnyInterface)
            return AopMethodDynamicProxy.newInstance(instance);
        else
            return AopMethodCglibProxy.newInstance(instance);
        /*logger.warn("Instance class "+instance.getClass().getCanonicalName()+" doesn't implement any interface \n" +
                "Then we can't use Java Dynamic Proxy, and CGLIB proxy is not yet implemented \n" +
                "The same instance will be returned");*/
    }
}
