package org.injection.core.providers;

import org.injection.core.global.InjectionEvaluator;
import org.injection.core.qualifiers.BeanQualifierManager;
import org.injection.core.scan.BeanScanManager;
import org.injection.core.data.BeanConfig;
import org.injection.dependency.LoopDetection;
import org.injection.enums.BeanSourceType;
import org.injection.enums.DependencyType;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BeanResolverImpl implements InjectionEvaluator {
    private static final Log logger = Log.getInstance(BeanResolverImpl.class);
    private final BeanScanManager beanScanManager;
    private final BeanQualifierManager beanQualifierManager;
    private final LoopDetection loopDetection = new LoopDetection();

    public BeanResolverImpl(BeanScanManager beanScanManager,
                            BeanQualifierManager beanQualifierManager){
        this.beanScanManager = beanScanManager;
        this.beanQualifierManager = beanQualifierManager;
    }
    /*----------------------------------- core methods ---------------------------------------*/
    public <T> T getBeanInstance(Class<T> beanType, Set<Annotation> qualifiers) {
        logger.info("[getBeanInstance] Start getting bean instance of type:["+beanType+"] " +
                "\nWith initial qualifiers:["+qualifiers+"]");
        if(beanType==null)
            throw new FrameworkException("Bean type can't be null");
        T beanInstance = null;
        try {
            // start creating bean
            beanInstance = innerGetBeanInstance(beanType, qualifiers);
        }catch (Exception exception){
            logger.error(exception);
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
    private synchronized <T> T innerGetBeanInstance(Class<T> beanType, Set<Annotation> qualifiers){
        loopDetection.init();
        try{
            return resolveBean(beanType, DependencyType.FIRST, qualifiers);
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
     * @return
     * @param <T>
     */
    private synchronized <T> T resolveBean(Class<T> beanType, DependencyType dependencyType, Set<Annotation> qualifiers){
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
            T bean = getBean(beanType, qualifiers);
            String gotFrom = "Got from "+bean.getClass().getCanonicalName()+" with qualifiers: "+qualifiers+"";
            loopDetection.resolveDependency(beanType, gotFrom);
            return bean;
        }catch (Exception e){
            String cause = "With qualifiers: "+qualifiers;
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
    private  synchronized <T> T getBean(Class<T> beanType, Set<Annotation> qualifiers) {
        // search for possible implementation/factory using given qualifier
        final BeanConfig beanConfig = getBeanConfig(beanType, qualifiers);
        logger.info("[getBean] Get bean with type:["+beanType.getCanonicalName()+"] with qualifiers:"+qualifiers+"");
        Object beanToReturn = null;
        logger.debug("Start creating new instance of bean ["+beanType.getCanonicalName()+"]");
        if(beanConfig.getSourceType().equals(BeanSourceType.CLASS)){
            beanToReturn = buildFromClass(beanType, (Class<?>) beanConfig.getSource());
        }else if(beanConfig.getSourceType().equals(BeanSourceType.METHOD)){
            beanToReturn = buildFromMethod(beanType, (Method) beanConfig.getSource());
        }else{
            throw new FrameworkException("Bean source type unknown ["+beanConfig+"]");
        }

        // add to container or throw exception
        if(beanToReturn!=null){
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

    private Object buildFromClass(Class<?> clazz, Class<?> implementation){
        Object bean = createBeanFromImplementation(implementation);
        logger.debug(
                "Getting instance of ["+clazz.getCanonicalName()+"] from implementation class: "+
                        bean.getClass()
        );
        return bean;
    }

    private Object buildFromMethod(Class<?> clazz, Method factory){
        Object bean = clazz.cast(createBeanFromFactoryMethod(factory));
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
    private <T> T createBeanFromImplementation(Class<T> implementation) {
        try {
            final Constructor<T> constructor = findValidConstructor(implementation);
            final Object[] parameters = getConstructorParametersValue(constructor);
            T instance = constructor.newInstance(parameters);
            populateInjectedFields(instance);
            populateInjectedMethods(instance);
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
    private Object createBeanFromFactoryMethod(Method factory) {
        try {
            Object bean;
            final Constructor<?> constructor = findValidConstructor((Class<?>) factory.getDeclaringClass());
            final Object[] parameters = getConstructorParametersValue(constructor);
            Object factoryClassObj =  constructor.newInstance(parameters);
            final Object[] factoryParametersValues = getMethodParametersValues(factory);
            bean = factory.invoke(factoryClassObj, factoryParametersValues);
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
    private <T> Object[] getConstructorParametersValue(Constructor<T> constructor) {
        final Parameter[] parameters = constructor.getParameters();
        Stream stream = Arrays.stream(parameters)
                .map(parameter -> {
                    Class<?> paramType = parameter.getType();
                    try {
                        final Set<Annotation> qualifiers = beanQualifierManager.getQualifiers(parameter);
                        return (Object)resolveBean(paramType, DependencyType.CONSTRUCTOR_ARGUMENT, qualifiers);
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
    private <T> Object[] getMethodParametersValues(Method method) {
        final Parameter[] parameters = method.getParameters();
        Stream stream = Arrays.stream(parameters)
                .map(parameter -> {
                    Class<?> paramType = parameter.getType();
                    try {
                        final Set<Annotation> qualifiers = beanQualifierManager.getQualifiers(parameter);
                        return (Object)resolveBean(paramType, DependencyType.METHOD_ARGUMENT, qualifiers);
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
    private Map<Field,Object> populateInjectedFields(Object instance){
        Class instanceClass = instance.getClass();
        final Field[] declaredFields = instanceClass.getDeclaredFields();
        final Map<Field,Object> processedFields = new HashMap<>();
        for (Field field : declaredFields){
            if(!isValidInjectedField(field))
                continue;
            final Set<Annotation> qualifiers = beanQualifierManager.getQualifiers(field);
            Object fieldValue = resolveBean(field.getType(), DependencyType.FIELD, qualifiers);
            try {
                field.setAccessible(true);
                field.set(instance, fieldValue);
                processedFields.put(field, fieldValue);
            }catch (Exception e){
                logger.error(
                        "Can't set value for field ["+field.getName()+"] "+
                                "from class "+instanceClass.getName());
                throw new FrameworkException(e);
            }
        }
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
    private Map<Method,List> populateInjectedMethods(Object instance){
        Class instanceClass = instance.getClass();
        final Method[] methods = instanceClass.getMethods();
        final Map<Method,List> processedMethods = new HashMap<>();
        for (Method method : methods){
            if(!isValidInjectedMethod(method))
                continue;
            Parameter[] parameters = method.getParameters();
            List<Object> parametersValues = new ArrayList<>();
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
                Object paramValue = resolveBean(parameter.getType(), DependencyType.METHOD_ARGUMENT, qualifiers);
                parametersValues.add(paramValue);
            }

            try {
                method.invoke(instance, parametersValues.toArray());
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
        return processedMethods;
    }

}
