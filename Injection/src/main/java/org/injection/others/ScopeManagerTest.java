package org.injection.others;

import org.tools.agent.AgentConfig;
import org.tools.agent.AgentLoader;
import org.tools.exceptions.FrameworkException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ScopeManagerTest {
    //private static final Map<Class, Set<Class>> scopeClassBeanTypeAssociation; // {scopeClass: [beanType1, beanType2]}
    private static final Map<Class, Set<String>> scopesInstances; // {scopeClass: [scopeInstanceID, scopeInstanceID2, ...]}
    private static final Map<String, Set<Object>> InstanceIdBeansAssociation; // {scopeInstanceID: [bean1Ref, bean2Ref]}

    static {
        scopesInstances = new ConcurrentHashMap<>();
        InstanceIdBeansAssociation = new ConcurrentHashMap<>();
        //scopeClassBeanTypeAssociation = new ConcurrentHashMap<>();
    }

    private  void attachLifeCycleAgent(String applicationName){
        Map<String, Object> options = AgentLoader.getDefaultOptions();
        options.put("type", "lifecycle");
        options.put("cnfr","org\\.demo.*");
        String agentMainClass = "org.agent.Agent";
        String agentPackage = "org.agent";
        AgentConfig agentConfig = null;
        try {
            agentConfig = AgentLoader.searchAndAttachAgent(applicationName, agentMainClass, options);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                agentConfig = AgentLoader.buildAndAttachAgent(applicationName,agentPackage, agentMainClass, options);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        if(agentConfig == null){
            throw new FrameworkException("Can't load agent");
        }
    }

    /*public static void getBeanInstance(Object injectedIn){
        ScopedToClass scopedToClassAnnotation = null;
        Class beanType = null;
        if(injectedIn instanceof Parameter){
            Parameter parameter = (Parameter) injectedIn;
            scopedToClassAnnotation = (ScopedToClass) AnnotationTools.getAnnotation(parameter, ScopedToClass.class);
            beanType = parameter.getType();

        }else if(injectedIn instanceof Field){
            Field field = (Field) injectedIn;
            scopedToClassAnnotation = (ScopedToClass) AnnotationTools.getAnnotation(field, ScopedToClass.class);
            beanType = field.getType();
        }

        if(scopedToClassAnnotation!=null){
            Class scopedTo = scopedToClassAnnotation.scoppedTo();
            if(scopedTo==null)
                throw new FrameworkException("ScopedTo can't be null in annotation ScopedToClass");
            Object beanObj = getOrCreateBean(scopedTo, beanType);
        }
    }*/

    private static Object getOrCreateBean(Class scopedTo, Class beanType) {
        Set<String> scopeInstances = scopesInstances.getOrDefault(scopedTo,null);
        InstanceIdBeansAssociation.getOrDefault("", null);
        return null;
    }
}
