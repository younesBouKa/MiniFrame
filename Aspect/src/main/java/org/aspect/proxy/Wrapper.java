package org.aspect.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Modifier;

public class Wrapper {
    private static final Logger logger = LogManager.getLogger(Wrapper.class);
    private ProxyType defaultProxyType = ProxyType.JDK;

    private Wrapper(){}

    public static Wrapper init(){
        return new Wrapper();
    }

    public ProxyType getDefaultProxyType() {
        return defaultProxyType;
    }

    public Wrapper setDefaultProxyType(ProxyType defaultProxyType) {
        this.defaultProxyType = defaultProxyType;
        return this;
    }

    public Object wrap(Object instance){
        return wrap(instance, defaultProxyType);
    }

    public Object wrap(Object instance, ProxyType proxyType){
        // JDK proxy work only for classes implementing interfaces, I should look for CGLIB proxy for other class
        Class<?> targetClass = instance.getClass();
        boolean implementingAnyInterface = targetClass.isInterface()
                || targetClass.getInterfaces().length>0;
        boolean isAccessibleClass = Modifier.isPublic(targetClass.getModifiers());
        if(proxyType == ProxyType.CGLIB || (proxyType == ProxyType.JDK && !implementingAnyInterface)){
            return AopMethodCglibProxy.newInstance(instance);
        } else if (proxyType == ProxyType.ASM && isAccessibleClass) {
            return AopMethodByteBuddyProxy.newInstance(instance);
        }else{
            return AopMethodDynamicProxy.newInstance(instance);
        }
    }
}
