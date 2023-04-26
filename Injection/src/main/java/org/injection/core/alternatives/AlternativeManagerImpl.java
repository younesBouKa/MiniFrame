package org.injection.core.alternatives;

import org.injection.annotations.AlternativeConfig;
import org.injection.core.data.AlternativeInstance;
import org.injection.enums.BeanSourceType;
import org.tools.ClassFinder;
import org.tools.Log;
import org.tools.annotations.AnnotationTools;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AlternativeManagerImpl implements AlternativeManager {
    private static final Log logger = Log.getInstance(AlternativeManagerImpl.class);
    private final Map<Class<?>, AlternativeInstance> alternatives = new HashMap<>();
    private long lastUpdateTimeStamp = 0;
    private long updateCount = 0;

    public AlternativeManagerImpl(){
    }

    /*---------------------- Core methods --------------------------*/
    public AlternativeInstance getAlternative(Class<?> beanType, BeanSourceType sourceType){
        update(false);
        if(beanType == null || alternatives.isEmpty())
            return null;
        for(Class<?> alternativeBeanType : alternatives.keySet()){
            if ( beanType.isAssignableFrom(alternativeBeanType)
                    && (sourceType==null
                        || alternatives.get(alternativeBeanType).getSourceType()==null
                        || alternatives.get(alternativeBeanType).getSourceType().equals(sourceType)))
                return alternatives
                        .get(alternativeBeanType);
        }
        return null;
    }

    public void addAlternative(AlternativeInstance alternativeInstance){
        logger.info("Adding alternative: ["+alternativeInstance+"]");
        if(!validateAlternative(alternativeInstance))
            return;
        // control duplication
        Class<?> beanType = alternativeInstance.getBeanType();
        if(alternatives.containsKey(beanType)){
            logger.warn("Alternative already exists for bean ["+beanType.getCanonicalName()+"]\n" +
                    "Concerned alternatives: ["+alternatives.get(beanType)+"] \nand ["+alternativeInstance+"]");
        }
        synchronized (alternatives){
            alternatives.put(alternativeInstance.getBeanType(), alternativeInstance);
        }
    }

    public AlternativeInstance removeAlternative(Class<?> beanType){
        logger.info("Remove alternative for bean type: ["+beanType+"]");
        if(!alternatives.isEmpty() && beanType!=null)
            return alternatives.remove(beanType);
        return null;
    }

    /*---------------------- inner methods --------------------------*/
    public void update(boolean force){
        long lastUpdate = ClassFinder.getLastUpdateTimeStamp();
        if(lastUpdateTimeStamp < lastUpdate || force){
            updateCount++;
            logger.debug("Update Alternative manager cache for the ["+updateCount+"] time(s), last update timestamp ["+lastUpdateTimeStamp+"]");
            lastUpdateTimeStamp = lastUpdate;
            scanForAlternativeConfig();
            if(alternatives.isEmpty())
                logger.error("No alternative configuration was found.");
        }
    }

    private void scanForAlternativeConfig(){
        logger.debug("Scanning for alternative config methods ... ");
        long duration = System.currentTimeMillis();
        try {
            Set<Method> classMethods = getConfigMethods();
            if(!classMethods.isEmpty()){
                Object classInstance = null;
                for(Method method : classMethods){
                    try{
                        if(method.getParameters().length>0){
                            logger.warn("Method annotated with @AlternativeConfig can't have parameters");
                            continue;
                        }
                        if(!method.getReturnType().isAssignableFrom(Set.class)){
                            logger.warn("Method annotated with @AlternativeConfig return type should be of type Set<AlternativeInstance>");
                            continue;
                        }
                        if(!Modifier.isStatic(method.getModifiers()))
                            classInstance = method.getDeclaringClass().newInstance();
                        method.setAccessible(true);
                        Object result = method.invoke(classInstance);
                        if(result instanceof Set){
                            for(Object obj: (Set)result){
                                if(obj instanceof AlternativeInstance){
                                   addAlternative((AlternativeInstance) obj);
                                }
                            }
                        }else {
                            logger.error("Method ["+method.toGenericString()+"] return value should be of type Set<AlternativeInstance>");
                        }
                    }catch (Throwable throwable){
                        logger.error(throwable);
                    }
                }
            }
        }catch (Throwable throwable){
            logger.error(throwable.toString());
        }
        logger.debug("Scanning for alternative config methods ended in ["+(System.currentTimeMillis() - duration)+"] Millis " +
                "with "+ alternatives.size() +" alternative(s)");
        if(alternatives.isEmpty())
            logger.warn(
                    "No alternative config found"
            );
    }

    public Set<Method> getConfigMethods(){
        Predicate<Class> classPredicate = clazz-> {
            try {
                return !clazz.isInterface()
                        && !Modifier.isAbstract(clazz.getModifiers())
                        && Arrays.stream(clazz.getMethods())
                        .anyMatch(method -> AnnotationTools.getAnnotation(method, AlternativeConfig.class) != null
                                && method.getReturnType().isAssignableFrom(Set.class)
                        );
            }catch (Throwable throwable){
                //throwable.printStackTrace();
                logger.warn(throwable.toString());
            }
            return false;
        };
        Set<Class> configClass = ClassFinder.getClassesWithFilter(classPredicate);
        Set<Method> methodsToReturn = new HashSet<>();
        Map<Class,Set<Class>> classesWithErrors = new HashMap<>();
        for (Class clazz : configClass){
            try {
                Method[] classMethods = clazz.getMethods();
                Set<Method> configMethods = Arrays.stream(classMethods)
                        .filter(method->AnnotationTools.getAnnotation(method, AlternativeConfig.class)!=null
                        ).collect(Collectors.toSet());
                methodsToReturn.addAll(configMethods);
            }catch (Throwable throwable){
                if(!classesWithErrors.containsKey(throwable.getClass()))
                    classesWithErrors.put(throwable.getClass(), new HashSet<>());
                classesWithErrors.get(throwable.getClass()).add(clazz);
            }

        }

        if(!classesWithErrors.isEmpty()){
            logger.error("Can't get methods of some classes");
            for (Class clazz : classesWithErrors.keySet())
                logger.error("Error: ["+clazz+"], classes: "+classesWithErrors.get(clazz)+"");
        }
        return methodsToReturn;
    }

}
