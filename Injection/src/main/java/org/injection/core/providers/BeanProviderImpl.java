package org.injection.core.providers;

import org.injection.annotations.TempScope;
import org.injection.annotations.lifecycle.PostInit;
import org.injection.annotations.lifecycle.PostPropertiesSet;
import org.injection.core.data.BeanConfig;
import org.injection.core.data.ScopeInstance;
import org.injection.core.global.BeanContainer;
import org.injection.core.listeners.BeanLifeCycle;
import org.injection.core.qualifiers.BeanQualifierManager;
import org.injection.core.scan.BeanScanManager;
import org.injection.core.scopes.ControlledScope;
import org.injection.core.scopes.ScopeManager;
import org.injection.dependency.LoopDetection;
import org.injection.enums.BeanSourceType;
import org.injection.enums.DependencyType;
import org.tools.Log;
import org.tools.annotations.AnnotationTools;
import org.tools.exceptions.FrameworkException;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BeanProviderImpl implements BeanProvider {
    private static final Log logger = Log.getInstance(BeanProviderImpl.class);
    private final BeanScanManager beanScanManager;
    private final BeanQualifierManager beanQualifierManager;
    private final ScopeManager scopeManager;
    private final BeanContainer beanContainer;
    private final BeanLifeCycle beanLifeCycle;
    private final LoopDetection loopDetection = new LoopDetection();
    private final ControlledScope tempScope = new ControlledScope(TempScope.class);

    public BeanProviderImpl(BeanContainer beanContainer,
                            BeanScanManager beanScanManager,
                            BeanQualifierManager beanQualifierManager,
                            ScopeManager scopeManager){
        this(beanContainer, beanScanManager, beanQualifierManager, scopeManager, null);
    }
    public BeanProviderImpl(BeanContainer beanContainer,
                            BeanScanManager beanScanManager,
                            BeanQualifierManager beanQualifierManager,
                            ScopeManager scopeManager,
                            BeanLifeCycle beanLifeCycle){
        this.beanContainer = beanContainer;
        this.beanScanManager = beanScanManager;
        this.beanQualifierManager = beanQualifierManager;
        this.scopeManager = scopeManager;
        this.beanLifeCycle = beanLifeCycle;
    }
    /*----------------------------------- core methods ---------------------------------------*/
    @Override
    public <T> T getBeanInstance(Class<T> beanType, Set<Annotation> qualifiers, Set<ScopeInstance> scopes, Class<?> beanScopeType) {
        logger.info("[getBeanInstance] Start getting bean instance of type:["+beanType+"] " +
                "\nWith initial qualifiers:["+qualifiers+"], initial scopes:["+scopes+"] and bean scope initial type:["+beanScopeType+"]");
        if(beanType==null)
            throw new FrameworkException("Bean type can't be null");
        String tempScopeKey = beanType.getCanonicalName()+
                "."+
                System.currentTimeMillis()+
                "."+
                (Math.random() * 1000);
        T beanInstance = null;
        try {
            // create a temporary scope (for bean building only)
            boolean tempScopeCreated = tempScope.createScopeInstance(tempScopeKey);
            if(!tempScopeCreated)
                throw new FrameworkException("Can't create temporary scope for ["+beanType+"] bean building");
            if(scopes==null)
                scopes = new HashSet<>();
            scopes.add(new ScopeInstance(Singleton.class, Singleton.class.getCanonicalName()));
            scopes.add(new ScopeInstance(tempScope.getScopeType(), tempScopeKey));
            // start creating bean
            beanInstance = innerGetBeanInstance(beanType, qualifiers, beanScopeType, scopes);
        }catch (Exception exception){
            logger.error(exception);
        }finally {
            tempScope.destroyScopeInstance(tempScopeKey);
        }
        logger.info("End getting bean instance ["+beanInstance+"] of type :["+beanType+"]");
        return beanInstance;
    }

    /*------------------------------ inner methods ---------------------------*/

    /**
     * Return instance from the given interface type using 'getBean':
     *   - init LoopDetection class
     *   - Call 'resolveBean' with interface type and DependencyType.FIRST
     *   - Return instance
     *   - Finally, print search dependency logs then clear loopDetection instance
     * @param beanType
     * @return
     *      @param <T>
     */
    private synchronized <T> T innerGetBeanInstance(Class<T> beanType, Set<Annotation> qualifiers, Class<?> beanScopeType, Set<ScopeInstance> scopeInstances){
        loopDetection.init();
        try{
            return resolveBean(beanType, DependencyType.FIRST, qualifiers, beanScopeType, scopeInstances);
        }catch(Throwable throwable){
            throw new FrameworkException("Can't resolve bean: "+beanType.getName());
        }finally {
            loopDetection.print();
            loopDetection.clear();
        }
    }

    /**
     * Resolve bean dependencies and manage loopDetection operations
     * @param beanType
     * @param dependencyType
     * @param qualifiers
     * @param beanScopeType
     * @param scopeInstances
     * @return
     * @param <T>
     */
    private synchronized <T> T resolveBean(Class<T> beanType, DependencyType dependencyType, Set<Annotation> qualifiers, Class<?> beanScopeType, Set<ScopeInstance> scopeInstances){
        if(dependencyType==null)
            throw new FrameworkException("Dependency Type can't be null");
        if(beanType==null)
            throw new FrameworkException("Type of bean can't be null");
        // add dependency to loop detection
        loopDetection.addDependency(dependencyType, beanType);
        try {
            // verify qualifiers
            if (qualifiers==null || qualifiers.isEmpty()){
                Annotation defaultQualifier = beanQualifierManager.getDefaultQualifier(beanType);
                logger.info("Try getting bean instance of "+beanType+" with no qualifiers");
                if(defaultQualifier!=null){
                    if(!beanQualifierManager.isValidQualifierAnnotation(defaultQualifier.annotationType()))
                        throw new FrameworkException("Default qualifier "+defaultQualifier+" is not valid");
                    logger.info("Default qualifier "+defaultQualifier+" will be used");
                    qualifiers = Collections.singleton(defaultQualifier);
                }else{
                    logger.warn("No default qualifier was defined");
                }
            }
            // get bean
            T bean = getBean(beanType, qualifiers, beanScopeType, scopeInstances);
            String gotFrom = "Got from "+bean.getClass().getCanonicalName()+" with qualifiers: "+qualifiers+" and scope type: "+beanScopeType+"";
            loopDetection.resolveDependency(beanType, gotFrom);
            return bean;
        }catch (Exception e){
            String cause = "With qualifiers: "+qualifiers+" and scope type: "+beanScopeType+"";
            loopDetection.rejectDependency(beanType, cause);
            throw new FrameworkException("Can't resolve bean: "+beanType.getName());
        }
    }

    /**
     * Return instance, following steps:
     *   - Return from instance cache
     *      + if implementation source (class or factory method)
     *      + and cache contains an instance of this type
     *   - Else get instance from implementation or factory method
     *   - Add instance to bean container
     *   - Throws FrameworkException if can't build instance (no implementation class or factory method)
     * @return
     *      @param <T>
     */
    private  synchronized <T> T getBean(Class<T> beanType, Set<Annotation> qualifiers, Class<?> beanScopeType, Set<ScopeInstance> scopeInstances) {
        // search for possible implementation/factory using given qualifier
        final BeanConfig beanConfig = getBeanConfig(beanType, qualifiers);
        // try to get scope from implementation or factory method
        if(beanScopeType == null)
            beanScopeType = getBeanScopeType(beanConfig);
        ScopeInstance scopeInstance = getScopeInstance(beanScopeType, scopeInstances);
        Object scopeId = scopeInstance!=null ? scopeInstance.getScopeId() : null;
        Class<?> scopeType = scopeInstance!=null ? scopeInstance.getScopeType() : beanScopeType;
        logger.info("[getBean] Get bean with type:["+beanType.getCanonicalName()+"] with qualifiers:"+qualifiers+", scope type: "+scopeType+" and scope instance: "+scopeId+"");
        // get from container
        sendEvent(BeanLifeCycle.preBeanBuilding, beanType, qualifiers, beanScopeType, scopeId, scopeInstance);
        Object beanToReturn =  getBeanFromContainer(beanType, scopeType, scopeId, qualifiers, beanConfig.isAlternative());
        if(beanToReturn==null){
            logger.debug("Start creating new instance of bean ["+beanType.getCanonicalName()+"] with scope type ["+scopeType+"] and scope id ["+scopeId+"]");
            if(beanConfig.getSourceType().equals(BeanSourceType.CLASS)){
                beanToReturn = buildFromClass(beanType, (Class<?>) beanConfig.getSource(), scopeInstances);
            }else if(beanConfig.getSourceType().equals(BeanSourceType.METHOD)){
                beanToReturn = buildFromMethod(beanType, (Method) beanConfig.getSource(), scopeInstances);
            }else{
                throw new FrameworkException("Bean source type unknown ["+beanConfig+"]");
            }
        }

        // add to container or throw exception
        if(beanToReturn!=null){
            if(scopeType!=null && scopeId!=null)
                beanContainer.addBean(beanType, beanToReturn , scopeType, scopeId, beanConfig.getSource());
            sendEvent(BeanLifeCycle.postBeanBuilding, beanToReturn, beanType, qualifiers, beanScopeType, scopeId, scopeInstance);
            String fromWhereDescription = getFromWhereDescription(beanConfig.getSource(), beanToReturn, qualifiers);
            logger.info(fromWhereDescription);
            return (T)beanToReturn;
        }
        throw new FrameworkException("Can't build bean for: "+beanType.getName());
    }

    private BeanConfig getBeanConfig(Class<?> beanType, Set<Annotation> qualifiers){
        final BeanConfig beanConfig = beanScanManager.getBeanConfig(beanType, qualifiers);
        if (beanConfig==null || beanConfig.getBeanType()==null){
            String msg = "No bean factory or implementation found for ["+ beanType.getCanonicalName() +"] ";
            if(qualifiers!=null && !qualifiers.isEmpty())
                msg+= "\nusing qualifiers: "+qualifiers;
            throw new FrameworkException(msg);
        }
        if(beanConfig.getSourceType()==null){
            throw new FrameworkException("Bean source of ["+beanType.getCanonicalName()+"] is null");
        }
        return beanConfig;
    }

    private Class<?> getBeanScopeType(BeanConfig beanConfig){
        Class<?> beanScopeType = null;
        // try to get scope from implementation or factory method
        if(beanConfig.getSourceType().equals(BeanSourceType.CLASS)){
            beanScopeType = scopeManager.getClassScope((Class) beanConfig.getSource());
        }else if(beanConfig.getSourceType().equals(BeanSourceType.METHOD)) {
            beanScopeType = scopeManager.getMethodScope((Method) beanConfig.getSource());
        }else{
            throw new FrameworkException("Bean source type unknown ["+beanConfig+"]");
        }
        // try to get scope from bean type
        if(beanScopeType==null)
            beanScopeType = scopeManager.getClassScope(beanConfig.getBeanType());
        // else set default bean scope if exists
        if(beanScopeType == null){
            Class<? extends Annotation> defaultScopeType = scopeManager.getDefaultScopeType(beanConfig.getBeanType());
            if(defaultScopeType != null) {
                if(!scopeManager.isValidScopeAnnotation(defaultScopeType))
                    throw new FrameworkException("Default Scope Type "+defaultScopeType+" is not valid");
                beanScopeType = defaultScopeType;
            }else
                beanScopeType = tempScope.getScopeType();
        }
        return beanScopeType;
    }

    private ScopeInstance getScopeInstance(Class<?> scopeType, Set<ScopeInstance> scopeInstances){
        if(scopeType!=null && scopeInstances!=null && !scopeInstances.isEmpty()){
            for (ScopeInstance scopeInstance : scopeInstances){
                if(scopeInstance.getScopeType().equals(scopeType))
                    return scopeInstance;
                Annotation scopeAnnotation = AnnotationTools.getAnnotation(scopeType, scopeInstance.getScopeType());
                if(scopeAnnotation!=null){
                    return scopeInstance;
                }
            }
        }
        return null;
    }

    private Object getBeanFromContainer(Class<?> beanType, Class<?> scopeType, Object scopeId, Set<Annotation> qualifiers, boolean isAlternative){
        if(scopeType==null || scopeId==null)
            return null;
        Object beanToReturn = beanContainer.getBean(beanType, scopeType, scopeId);
        // check if we have the same implementation of bean (bean container doesn't use qualifiers)
        boolean isValid = beanToReturn!=null && (
                isAlternative ||
                isMatchingBean(beanToReturn.getClass(), qualifiers)
        );
        if(!isValid)
            return null;
        logger.debug("Getting bean instance from container ["+beanToReturn.getClass()+"] of type ["+beanType.getCanonicalName()+"] with scope type ["+scopeType+"] and scope id ["+scopeId+"]");
        return beanToReturn;
    }

    private boolean isMatchingBean(Class beanInstanceClass, Set<Annotation> qualifiers){
        Set<Class> filteredImpl =  beanQualifierManager.filterImplementations(Collections.singleton(beanInstanceClass), qualifiers);
        return filteredImpl !=null
                && filteredImpl.size()==1;
    }

    private Object buildFromClass(Class<?> clazz, Class<?> implementation, Set<ScopeInstance> scopeInstances){
        Object bean = createBeanFromImplementation(implementation, scopeInstances);
        logger.debug(
                "Getting instance of ["+clazz.getCanonicalName()+"] from implementation class: "+
                        bean.getClass()
        );
        return bean;
    }

    private Object buildFromMethod(Class<?> clazz, Method factory, Set<ScopeInstance> scopeInstances){
        Object bean = clazz.cast(createBeanFromFactoryMethod(factory, scopeInstances));
        logger.debug(
                "Getting instance from factory method: "+
                        factory.getDeclaringClass().getCanonicalName()+"."+
                        factory.getName()
        );
        return bean;
    }

    /**
     * A small description of from where we got our bean
     * @param implementation
     * @param bean
     * @param qualifiers
     * @return
     */
    private  String getFromWhereDescription(final Object implementation, final Object bean, final Set<Annotation> qualifiers){
        if(bean==null)
            return "Bean is Null";
        String src = null;
        if(implementation instanceof Class){
            src= ((Class<?>)implementation).getCanonicalName();
        } else if (implementation instanceof Method) {
            Method factory= (Method)implementation;
            src= factory.getDeclaringClass().getCanonicalName()+"."+factory.getName();
        } else {
            throw new FrameworkException("Can't see from where we got our been!!");
        }
        String gotFrom = "Got from ["+src+"]";
        if(qualifiers!=null && !qualifiers.isEmpty())
            gotFrom+=" using qualifiers "+qualifiers+"";
        gotFrom+=" Hashcode:["+bean.hashCode()+"]";
        return gotFrom;
    }

    /**
     * Create bean instance from given implementation following those steps:
     *      - Find a valid constructor
     *      - Get constructor parameters values
     *      - Create instance
     *      - populate injected fields (see method populateInjectedFields)
     *      - populate injected methods (see method populateInjectedMethods)
     *  throws FrameworkException
     * @param implementation : implementation class
     * @return
     *      @param <T>
     */
    private <T> T createBeanFromImplementation(Class<T> implementation, Set<ScopeInstance> scopeInstances) {
        try {
            final Constructor<T> constructor = findValidConstructor(implementation);
            final Object[] parameters = getConstructorParametersValue(constructor, scopeInstances);
            T instance = constructor.newInstance(parameters);
            sendEvent(BeanLifeCycle.preConstructorCall, instance, parameters, constructor, scopeInstances, implementation);
            populateInjectedFields(instance, scopeInstances);
            populateInjectedMethods(instance, scopeInstances);
            sendEvent(BeanLifeCycle.postInstancePreparation, instance, parameters, constructor, scopeInstances, implementation);
            return instance;
        } catch (FrameworkException e) {
            throw e;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

    /**
     * Create an instance using a methode factory
     * @param factory : method that create a bean
     * @return: instance with same type of method factory return type
     */
    private Object createBeanFromFactoryMethod(Method factory, Set<ScopeInstance> scopeInstances) {
        try {
            Object bean;
            final Constructor<?> constructor = findValidConstructor((Class<?>) factory.getDeclaringClass());
            final Object[] parameters = getConstructorParametersValue(constructor, scopeInstances);
            Object factoryClassObj =  constructor.newInstance(parameters);
            final Object[] factoryParametersValues = getMethodParametersValues(factory, scopeInstances);
            sendEvent(BeanLifeCycle.preFactoryCall, parameters, constructor, scopeInstances, factoryClassObj, factoryParametersValues, factory);
            bean = factory.invoke(factoryClassObj, factoryParametersValues);
            sendEvent(BeanLifeCycle.postFactoryCall, bean, parameters, constructor, scopeInstances, factoryClassObj, factoryParametersValues, factory);
            return bean;
        } catch (FrameworkException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new FrameworkException(e);
        }
    }

    /**
     * Return parameters values for the given constructor
     * Throws FrameworkException if it can't getting parameter instance
     * @param constructor
     * @return: parameters values
     *  @param <T>
     */
    private <T> Object[] getConstructorParametersValue(Constructor<T> constructor, Set<ScopeInstance> scopeInstances) {
        final Parameter[] parameters = constructor.getParameters();
        Stream stream = Arrays.stream(parameters)
                .map(parameter -> {
                    Class<?> paramType = parameter.getType();
                    try {
                        final Set<Annotation> qualifiers = beanQualifierManager.getQualifiers(parameter);
                        final Class<? extends Annotation> scopeType = scopeManager.getParameterScope(parameter);
                        return (Object)resolveBean(paramType, DependencyType.CONSTRUCTOR_ARGUMENT, qualifiers, scopeType, scopeInstances);
                    }catch (Exception e){
                        e.printStackTrace();
                        throw new FrameworkException(
                                "Can't get instance of parameter: ["+paramType.getCanonicalName()
                                        +"] for constructor: ["+constructor.getDeclaringClass().getCanonicalName()+"]"
                        );
                    }
                });
        return stream!=null ? stream.toArray() : new Object[]{};
    }

    /**
     * Return parameters values for the given method
     * Throws FrameworkException if it can't getting a parameter instance
     * @param method
     * @return
     *  @param <T>
     */
    private <T> Object[] getMethodParametersValues(Method method, Set<ScopeInstance> scopeInstances) {
        final Parameter[] parameters = method.getParameters();
        Stream stream = Arrays.stream(parameters)
                .map(parameter -> {
                    Class<?> paramType = parameter.getType();
                    try {
                        final Set<Annotation> qualifiers = beanQualifierManager.getQualifiers(parameter);
                        final Class<? extends Annotation> scopeType = scopeManager.getParameterScope(parameter);
                        return (Object)resolveBean(paramType, DependencyType.METHOD_ARGUMENT, qualifiers, scopeType, scopeInstances);
                    }catch (Exception e){
                        e.printStackTrace();
                        throw new FrameworkException(
                                "Can't get instance of parameter: "+paramType.getCanonicalName()
                                        +" for method: "+method.getDeclaringClass().getCanonicalName()+"."+method.getName()
                        );
                    }
                });

        return stream!=null ? stream.toArray() : new Object[]{};
    }

    /**
     * Find a constructor for the given class:
     *  - The first constructor if there is only one
     *  - Return the first one declared with @Inject
     *  - Throw FrameworkException if Many or No constructors is declared with @Inject
     */
    @SuppressWarnings("unchecked")
    private <T> Constructor<T> findValidConstructor(Class<T> clazz) {
        final Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
        if (constructors.length == 1) {
            return constructors[0];
        }

        final Set<Constructor<T>> constructorsWithAnnotation = Arrays.stream(constructors)
                .filter(this::isInjectable)
                .collect(Collectors.toSet());
        if (constructorsWithAnnotation.size() > 1) {
            throw new FrameworkException("There are more than 1 constructor with Inject annotation: " + clazz.getName());
        }
        return constructorsWithAnnotation.stream()
                .findFirst()
                .orElseThrow(
                        () -> new FrameworkException(
                                "Cannot find constructor with annotation Inject: " + clazz.getName()
                        )
                );
    }

    /**
     * Populate only fields with @Inject annotation and where type is interface
     * Injectable fields:
     *                 are annotated with @Inject.
     *                 are not final.
     *                 may have any otherwise valid name.
     *           Ex:
     *             @Inject FieldModifiers opt Type VariableDeclarators;
     * @param instance
     * @return: map of (field,value)
     */
    private Map<Field,Object> populateInjectedFields(Object instance, Set<ScopeInstance> scopeInstances){
        Class instanceClass = instance.getClass();
        final Field[] declaredFields = instanceClass.getDeclaredFields();
        final Map<Field,Object> processedFields = new HashMap<>();
        sendEvent(BeanLifeCycle.preInjectFields, instanceClass, instance, declaredFields, scopeInstances);
        for (Field field : declaredFields){
            if(!isValidInjectedField(field))
                continue;
            final Set<Annotation> qualifiers = beanQualifierManager.getQualifiers(field);
            final Class<? extends Annotation> scopeType = scopeManager.getFieldScope(field);
            sendEvent(BeanLifeCycle.preInjectField, instanceClass, instance, field, qualifiers, scopeType, scopeInstances);
            Object fieldValue = resolveBean(field.getType(), DependencyType.FIELD, qualifiers, scopeType, scopeInstances);
            try {
                field.setAccessible(true);
                field.set(instance, fieldValue);
                sendEvent(BeanLifeCycle.postInjectField, instanceClass, instance, field, fieldValue, scopeInstances);
                processedFields.put(field, fieldValue);
            }catch (Exception e){
                logger.error(
                        "Can't set value for field ["+field.getName()+"] "+
                                "from class "+instanceClass.getName());
                throw new FrameworkException(e);
            }
        }

        sendEvent(BeanLifeCycle.postInjectFields, instance, instanceClass, declaredFields, processedFields, scopeInstances);
        return processedFields;
    }

    /**
     * Injectable methods:
     *                 are annotated with @Inject.
     *                 are not abstract.
     *                 do not declare type parameters of their own. ( from java injection spec)
     *                 may return a result
     *                 may have any otherwise valid name.
     *                 accept zero or more dependencies as arguments.
     *             EX:
     *                 @Inject MethodModifiers opt ResultType Identifier(FormalParameterListopt) Throwsopt MethodBody
     * @param instance
     * @return : list of processed methods and their parameters values
     */
    private Map<Method,List> populateInjectedMethods(Object instance, Set<ScopeInstance> scopeInstances){
        Class instanceClass = instance.getClass();
        final Method[] methods = instanceClass.getMethods();
        final Map<Method,List> processedMethods = new HashMap<>();
        sendEvent(BeanLifeCycle.preInjectMethods, instanceClass, instance, methods, scopeInstances);
        for (Method method : methods){
            if(!isValidInjectedMethod(method))
                continue;
            Parameter[] parameters = method.getParameters();
            List<Object> parametersValues = new ArrayList<>();
            sendEvent(BeanLifeCycle.preInjectMethodParams, instanceClass, instance, method, parameters, scopeInstances);
            for (Parameter parameter : parameters){
                boolean isParameterOfTypeInterface = parameter
                        .getType()
                        .isInterface();
                if(!isParameterOfTypeInterface){
                    throw new FrameworkException(
                            "Parameter ["+parameter.getName()+"] "+
                                    "in injected  ["+instanceClass.getName()+"."+method.getName()+"] "+
                                    "but is not of type interface"
                    );
                }

                final Set<Annotation> qualifiers = beanQualifierManager.getQualifiers(parameter);
                final Class<? extends Annotation> scopeType = scopeManager.getParameterScope(parameter);
                sendEvent(BeanLifeCycle.preInjectMethodParam, instanceClass, instance, method, parameter, qualifiers, scopeType, scopeInstances);
                Object paramValue = resolveBean(parameter.getType(), DependencyType.METHOD_ARGUMENT, qualifiers, scopeType, scopeInstances);
                sendEvent(BeanLifeCycle.postInjectMethodParam, instanceClass, instance, method, parameter, paramValue, qualifiers, scopeType, scopeInstances);
                parametersValues.add(paramValue);
            }

            try {
                sendEvent(BeanLifeCycle.preInjectMethod, instanceClass, instance, method, parametersValues, scopeInstances);
                method.invoke(instance, parametersValues.toArray());
                sendEvent(BeanLifeCycle.postInjectMethod, instanceClass, instance, method, parametersValues, scopeInstances);
                processedMethods.put(method, parametersValues);
            }catch (Exception e){
                logger.error(
                        "Can't call injected method: ["+instanceClass.getName()+"."+method.getName()+"] " +
                                "with params ["+ Arrays.stream(parameters).map(Parameter::getName).collect(Collectors.toList())+"] "
                );
                e.printStackTrace();
                throw new FrameworkException(e);
            }
        }
        sendEvent(BeanLifeCycle.postInjectMethods, instance, instanceClass, methods, processedMethods, scopeInstances);
        return processedMethods;
    }

    private void sendEvent(String eventType, Object... args){
        // call lifecycle methods
        Class<? extends Annotation> lifeCycleAnnotation = null;
        Object instance = null;
        if(BeanLifeCycle.postInjectFields.equals(eventType)){
            lifeCycleAnnotation = PostPropertiesSet.class;
            instance = args[0];
        }else if(BeanLifeCycle.postInstancePreparation.equals(eventType)){
            lifeCycleAnnotation = PostInit.class;
            instance = args[0];
        }
        if(instance!=null && lifeCycleAnnotation!=null)
            innerCallLifeCycleMethods(instance, lifeCycleAnnotation);

        // send event to beanLifeCycle
        if(beanLifeCycle!=null){
            try {
                Thread eventThread = new Thread(()->{
                    beanLifeCycle.onEvent(eventType, args);
                });
                eventThread.setDaemon(true);
                eventThread.start();
            }catch (Throwable throwable){
                logger.error(throwable);
            }
        }
    }

    private void innerCallLifeCycleMethods(Object instance, Class<? extends Annotation> lifeCycleAnnotation){
        Class<?> instanceClass = instance.getClass();
        logger.info("Calling methods annotated with ["+lifeCycleAnnotation+"] on instance ["+instance+"] started");
        Set<Method> methods = Arrays.stream(instanceClass.getMethods())
                .filter(method -> AnnotationTools.getAnnotation(method, lifeCycleAnnotation)!=null)
                .collect(Collectors.toSet());
        Set<Method> calledMethods = new HashSet<>();
        for (Method method : methods){
            boolean called = callLifeCycleMethod(instance, method);
            if(called){
                calledMethods.add(method);
            }
        }
        logger.info("Calling "+calledMethods+" methods annotated with ["+lifeCycleAnnotation+"] on instance ["+instance+"] ended");
    }

    private boolean callLifeCycleMethod(Object instance, Method method){
        Class<?> instanceClass = instance.getClass();
        if(method.getParameters().length>0){
            logger.error("Life Cycle Method ["+method.toGenericString()+"] can't have parameters");
            return false;
        }
        try{
            method.invoke(instance, new Object[]{});
            logger.info("Life Cycle method ["+method.toGenericString()+"] called on instance ["+instance+"]");
            return true;
        }catch (Throwable throwable){
            logger.error("Can't call life cycle method ["+method.toGenericString()+"] on instance ["+instance+"]");
            logger.error(throwable);
        }
        return false;
    }
}
