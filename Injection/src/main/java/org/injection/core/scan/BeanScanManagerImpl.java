package org.injection.core.scan;

import org.injection.annotations.Alternative;
import org.injection.core.alternatives.AlternativeManager;
import org.injection.core.qualifiers.BeanQualifierManager;
import org.injection.core.global.ClassPool;
import org.injection.core.data.AlternativeInstance;
import org.injection.core.data.BeanConfig;
import org.injection.enums.BeanSourceType;
import org.tools.ClassFinder;
import org.tools.Log;
import org.tools.annotations.AnnotationTools;
import org.tools.exceptions.FrameworkException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BeanScanManagerImpl implements BeanScanManager {
    private static final Log logger = Log.getInstance(BeanScanManagerImpl.class);
    private final Set<BeanConfig> beanConfigsCache = new HashSet<>();
    private final ClassPool contextClassPool;
    private final AlternativeManager alternativeManager;
    private final BeanQualifierManager beanQualifierManager;
    private final Set<String> beanImplementationsSet = new HashSet<>();
    private final Set<Method> beanFactoriesSet = new HashSet<>();
    private long lastUpdateTimeStamp = 0;
    private long updateCount = 0;

    public BeanScanManagerImpl(ClassPool classPool,
                               AlternativeManager alternativeManager,
                               BeanQualifierManager beanQualifierManager){
        this.contextClassPool = classPool;
        this.alternativeManager = alternativeManager;
        this.beanQualifierManager = beanQualifierManager;
    }

    /*-------------------------- Core methods  --------------------------------*/
    public BeanConfig getBeanConfig(Class<?> beanType, Set<Annotation> qualifiers){
        BeanConfig beanConfigFromCache = getBeanConfigFromCache(beanType, qualifiers);
        if(beanConfigFromCache!=null){
            logger.info("Getting bean config from cache, bean type: ["+beanType+"], qualifiers: "+qualifiers+", bean config: "+beanConfigFromCache);
            return beanConfigFromCache;
        }

        BeanConfig beanConfigToReturn = getBeanAlternative(beanType);
        if(beanConfigToReturn!=null)
            return beanConfigToReturn;
        Class<?> implementation = selectBeanImplementation(beanType, qualifiers);
        Method factory = selectBeanFactory(beanType, qualifiers);
        boolean twoSourceFound = implementation!=null && factory!=null;
        int priority = getBeanPriority(beanType);
        if(twoSourceFound && priority == NO_PRIORITY) {
            String msg = "Two bean sources founded for ["+beanType.getCanonicalName()+"] ";
            msg+= "\nImplementation => "+implementation;
            msg+= "\nFactory => "+factory.toGenericString();
            msg+= "\nAnd no priority was set in this BeanScanManager implementation";
            if(qualifiers!=null && !qualifiers.isEmpty()) {
                msg += "\nUsing qualifiers: " + qualifiers;
                msg += "\nYou may change or add qualifiers to avoid such problem";
            }
            else
                msg+="\nYou can add qualifier to specify only one";
            throw new FrameworkException(msg);
        }else if((twoSourceFound && priority == IMPLEMENTATION_OVER_FACTORY)){
            beanConfigToReturn = new BeanConfig(beanType, BeanSourceType.CLASS, implementation, qualifiers);
        }else if(twoSourceFound && priority == FACTORY_OVER_IMPLEMENTATION ){
            beanConfigToReturn = new BeanConfig(beanType, BeanSourceType.METHOD, factory, qualifiers);
        }else if(implementation!=null){
            beanConfigToReturn = new BeanConfig(beanType, BeanSourceType.CLASS, implementation, qualifiers);
        }else if(factory != null){
            beanConfigToReturn = new BeanConfig(beanType, BeanSourceType.METHOD, factory, qualifiers);
        }
        if(beanConfigToReturn!=null)
            addBeanConfigToCache(beanConfigToReturn);
        return beanConfigToReturn;
    }

    public BeanConfig getBeanAlternative(Class<?> beanType){
        BeanConfig beanConfigFromCache = getBeanConfigFromCache(beanType, null);
        if(beanConfigFromCache!=null){
            logger.info("Getting bean alternative from cache, bean type: ["+beanType+"], bean config: "+beanConfigFromCache);
            return beanConfigFromCache;
        }
        BeanConfig foundedBeanConfig = null;
        AlternativeInstance alternative = this.alternativeManager.getAlternative(beanType);
        if(alternative!=null){
            String alternativeSourceName = alternative.getSource();
            if(BeanSourceType.CLASS.equals(alternative.getSourceType())){
                // search for implementation alternative
                Class<?> alternativeClass = getBeanImplementations(beanType)
                        .stream()
                        .filter(clazz -> BeanSourceType.CLASS.isMatching(clazz, alternativeSourceName))
                        .map(component -> (Class<?>)component)
                        .findFirst()
                        .orElse(null);
                if(alternativeClass==null)
                    throw new FrameworkException("No class found with name ["+alternativeSourceName+"]");
                if(AnnotationTools.getAnnotation(alternativeClass, Alternative.class)==null)
                    throw new FrameworkException("Alternative class ["+alternativeSourceName+"] is not annotated with @Alternative");
                foundedBeanConfig = new BeanConfig(beanType, BeanSourceType.CLASS, alternativeClass, true);
            }else if(BeanSourceType.METHOD.equals(alternative.getSourceType())){
                // search for factory alternative
                Method alternativeFactory = getBeanFactories(beanType)
                        .stream()
                        .filter(method -> BeanSourceType.METHOD.isMatching(method , alternativeSourceName))
                        .findFirst()
                        .orElse(null);
                if(alternativeFactory==null)
                    throw new FrameworkException("No factory method found with name ["+alternativeSourceName+"] ");
                if(AnnotationTools.getAnnotation(alternativeFactory, Alternative.class)==null)
                    throw new FrameworkException("Alternative factory method ["+alternativeSourceName+"] is not annotated with @Alternative");
                foundedBeanConfig = new BeanConfig(beanType, BeanSourceType.METHOD, alternativeFactory, true);
            }else{
                throw new FrameworkException("Alternative source type is not recognized ["+alternative.getSourceType()+"]");
            }
        }
        if(foundedBeanConfig!=null)
            addBeanConfigToCache(foundedBeanConfig);
        return foundedBeanConfig;
    }

    public Class selectBeanImplementation(Class<?> beanType, Set<Annotation> qualifiers){
        Class<?> implementation = null;
        final Set<Class> possibleImplementations = getBeanImplementations(beanType);
        List<Class<?>> selectedImplementations = beanQualifierManager
                .filterImplementations(possibleImplementations, qualifiers)
                .stream()
                .map(component -> (Class<?>)component)
                .collect(Collectors.toList());
        if(selectedImplementations.size()==1){
            implementation = selectedImplementations.get(0);
        } else if (selectedImplementations.size() > 1){
            String msg = "Many implementations founded for ["+beanType.getCanonicalName()+"] ";
            msg+= "\nFound implementations: "+selectedImplementations;
            if(qualifiers!=null && !qualifiers.isEmpty())
                msg+="\nYou may change qualifiers "+qualifiers+" to avoid such problem";
            else
                msg+="\nYou can add qualifier to specify only one or define a default qualifier in BeanProvider implementation";
            throw new FrameworkException(msg);
        }else{
            String msg = "No bean implementation found for ["+ beanType.getName() +"] ";
            if(qualifiers!=null && !qualifiers.isEmpty())
                msg+= "\nusing qualifiers: "+qualifiers;
            logger.warn(msg);
        }
        return implementation;
    }

    public Method selectBeanFactory(Class<?> beanType, Set<Annotation> qualifiers){
        Method factory = null;
        final Set<Method> possibleFactories = getBeanFactories(beanType);
        List<Method> selectedFactories = beanQualifierManager
                .filterFactories(possibleFactories, qualifiers)
                .stream()
                .map(component -> (Method)component)
                .collect(Collectors.toList());
        if(selectedFactories.size()==1){
            factory = selectedFactories.get(0);
        }else if(selectedFactories.size() > 1){
            String msg = "Many Factories founded for ["+beanType.getCanonicalName()+"] ";
            msg+= "\nFound factories: "+selectedFactories;
            if(qualifiers!=null && !qualifiers.isEmpty())
                msg+="\nYou may change qualifiers "+qualifiers+" to avoid such problem";
            else
                msg+="\nYou can add qualifier to specify only one or define a default qualifier in BeanProvider implementation";
            throw new FrameworkException(msg);
        }else{
            String msg = "No bean factory found for ["+ beanType.getName() +"] ";
            if(qualifiers!=null && !qualifiers.isEmpty())
                msg+= "\nusing qualifiers: "+qualifiers;
            logger.warn(msg);
        }
        return factory;
    }

    public Set<Class> getBeanImplementations(Class<?> beanType){
        Predicate<Class> classTypeFilter = beanType::isAssignableFrom;
        return getBeanImplementations()
                .stream()
                .map(ClassFinder::loadClass)
                .filter(classTypeFilter)
                .collect(Collectors.toSet());
    }

    public Set<Method> getBeanFactories(Class<?> beanType){
        Predicate<Method> returnTypeFilter = method -> beanType.isAssignableFrom(method.getReturnType());
        return getBeanFactories().stream()
                .filter(returnTypeFilter)
                .collect(Collectors.toSet());
    }

    public boolean addBeanImplementation(Class<?> beanType, Class implementation){
        if(beanType==null){
            logger.error("Bean type can't be null");
            return false;
        }
        if(!isValidComponentClass(implementation)){
            logger.error("Bean implementation ["+implementation+"] is not a valid component class");
            return false;
        }
        if(!beanType.isAssignableFrom(implementation)){
            logger.error("Bean type ["+beanType+"] is not assignable from ["+implementation+"]");
            return false;
        }
        return beanImplementationsSet.add(implementation.getCanonicalName());
    }

    public boolean addBeanFactory(Class<?> beanType, Method factory){
        if(beanType==null){
            logger.error("Bean type can't be null");
            return false;
        }
        if(!isValidFactoryMethod(factory)){
            logger.error("Bean factory ["+factory+"] is not a valid component class");
            return false;
        }
        if(!beanType.isAssignableFrom(factory.getReturnType())){
            logger.error("Bean type ["+beanType+"] is not assignable from factory return type ["+factory.getReturnType()+"]");
            return false;
        }
        return beanFactoriesSet.add(factory);
    }

    /*---------------------- inner methods --------------------------*/
    public boolean addBeanConfigToCache(BeanConfig beanConfig){
        return beanConfigsCache.add(beanConfig);
    }

    public BeanConfig getBeanConfigFromCache(Class<?> beanType, Set<Annotation> qualifiers){
       for (BeanConfig beanConfig : beanConfigsCache){
            if(beanConfig.getBeanType().equals(beanType)){
                if(qualifiers==null || qualifiers.isEmpty())
                    return beanConfig;
                if(beanConfig.getQualifiers()!=null && !beanConfig.getQualifiers().isEmpty()){
                    boolean allQualifiersExists = true;
                    for(Annotation qualifier : qualifiers){
                        if(!beanConfig.getQualifiers().contains(qualifier)) {
                            allQualifiersExists = false;
                            break;
                        }
                    }
                    if(allQualifiersExists)
                        return beanConfig;
                }
            }
        }
        return null;
    }

    public void update(boolean force){
        long lastUpdate = ClassFinder.getLastUpdateTimeStamp();
        if(lastUpdateTimeStamp < lastUpdate || force){
            updateCount++;
            logger.debug("Update Bean Scan manager cache for the ["+updateCount+"] time(s), last update timestamp ["+lastUpdateTimeStamp+"]");
            lastUpdateTimeStamp = lastUpdate;
            beanConfigsCache.clear();
            scanForBeansImplementations();
            if(beanImplementationsSet.isEmpty())
                logger.error("No bean class was found.");
            scanForBeansFactories();
            if(beanFactoriesSet.isEmpty())
                logger.error("No bean factory was found.");
        }
    }
    private Set<Method> getBeanFactories(){
        update(false);
        return beanFactoriesSet;
    }
    private Set<String> getBeanImplementations(){
        update(false);
        return beanImplementationsSet;
    }

    /**
     * Scan packages for beans implementations and factories methods, using following steps:
     *      - Throws FrameworkException if beanScanRootClass is null (see getInstance)
     *      - Get packages to scan (using 'getBeanScanPackagesFromClass')
     *      - Get all classes from given packages (using Tools.getClasses)
     *      - Filter only valid bean classes (see isValidComponentClass)
     *      - For each class look for valid factory methods (see isValidFactoryMethod)
     *      - Init beansCache
     */
    private void scanForBeansImplementations(){
        logger.debug("Scanning for beans implementations started ... ");
        logger.debug("Searching for beans in packages : "+ new ArrayList<>(contextClassPool.getPackagesToScan()));
        Set<String> newImplementationSet = contextClassPool
                .getClassNamesWithClassFilter(this::isValidComponentClass);
        beanImplementationsSet.addAll(newImplementationSet);
        logger.debug("Available beans implementations : ["+beanImplementationsSet.size()+"]");
        logger.debug("Scanning for beans ended in packages "+ new ArrayList<>(contextClassPool.getPackagesToScan()) +"");
        if(beanImplementationsSet.isEmpty())
            logger.warn(
                    "No bean implementation is available, "+
                            "try to verify 'BeanScanPackage' or annotate your beans with @Component"
            );
    }

    /**
     * Scan packages for beans factories methods, using following steps:
     *      - Throws FrameworkException if beanScanRootClass is null (see getInstance)
     *      - Get packages to scan (using 'getBeanScanPackagesFromClass')
     *      - Get all classes from given packages (using Tools.getClasses)
     *      - Filter only valid bean classes (see isValidComponentClass)
     *      - For each class look for valid factory methods (see isValidFactoryMethod)
     *      - Init beansCache
     */
    private void scanForBeansFactories(){
        logger.debug("Scanning for beans factories ... ");
        logger.debug("Searching for beans factories in packages : "+ new ArrayList<>(contextClassPool.getPackagesToScan()));
        Set<Method> foundedFactories = new HashSet<>();
        Predicate<Class> classPredicate = clazz-> !clazz.isInterface()
                && !Modifier.isAbstract(clazz.getModifiers());
        Map<Class,Set<Class>> classesWithErrors = new HashMap<>();
        contextClassPool.getClassesWithClassFilter(classPredicate)
                .forEach(clazz ->{
                    try {
                        foundedFactories.addAll(
                                Arrays
                                        .stream(clazz.getMethods())
                                        .filter(this::isValidFactoryMethod)
                                        .collect(Collectors.toSet())
                        );
                    }catch (Throwable throwable){
                        if(!classesWithErrors.containsKey(throwable.getClass()))
                            classesWithErrors.put(throwable.getClass(), new HashSet<>());
                        classesWithErrors.get(throwable.getClass()).add(clazz);
                    }
                });
        beanFactoriesSet.addAll(foundedFactories);
        if(!classesWithErrors.isEmpty()){
            logger.error("Can't get methods of some classes");
            for (Class clazz : classesWithErrors.keySet())
                logger.error("Error: ["+clazz+"], classes: "+classesWithErrors.get(clazz)+"");
        }
        logger.debug("Available beans factories : ["+beanFactoriesSet.size()+"]");
        logger.debug("Scanning for beans factories ended in packages "+ new ArrayList<>(contextClassPool.getPackagesToScan()) +"");
        if(beanFactoriesSet.isEmpty())
            logger.warn(
                    "No bean factory is available, "+
                            "try to verify bean scan package"
            );
    }
}
