package org.injection.core.qualifiers;

import org.injection.annotations.qualifiers.RegexQualifier;
import org.injection.annotations.qualifiers.markers.*;
import org.injection.annotations.QualifierConfig;
import org.tools.ClassFinder;
import org.tools.Log;
import org.tools.annotations.AnnotationTools;
import org.tools.exceptions.FrameworkException;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BeanQualifierManagerImpl implements BeanQualifierManager {
    private static final Log logger = Log.getInstance(BeanQualifierManagerImpl.class);
    private long lastUpdateTimeStamp = 0;
    private long updateCount = 0;
    private static final List<Class<? extends Annotation>> configQualifiers;
    private final Map<Class<? extends Annotation>, QualifierPredicate> qualifiers;
    private Annotation defaultQualifier;

    static {
        configQualifiers = Arrays
                .asList(EvalWithOR.class, EvalWithAND.class, FirstFound.class, ElseFirstFound.class);
    }

    public BeanQualifierManagerImpl(){
        qualifiers = new HashMap<>();
        //update(true);
    }
    /*----------------------- Core methods -------------------------*/
    public Map<Class<? extends Annotation>, QualifierPredicate> getAvailableQualifiers(){
        update(false);
        return qualifiers;
    }

    public QualifierPredicate addQualifier(Class<? extends Annotation> qualifierAnnotationClass, QualifierPredicate qualifierPredicate){
        if(!isValidQualifierAnnotation(qualifierAnnotationClass)){
            logger.error("Annotation ["+qualifierAnnotationClass+"] is not a valid qualifier");
            return null;
        }

        if(qualifierPredicate == null){
            qualifierPredicate = (annotation, beanSource) ->{
                if(annotation==null || beanSource==null)
                    return false;
                Class<? extends Annotation> annotationType = annotation.annotationType();
                Annotation sourceAnnotation = AnnotationTools.getAnnotation(beanSource, annotationType);
                if(sourceAnnotation==null)
                    return false;
                Class<? extends Annotation> sourceAnnotationType = sourceAnnotation.annotationType();
                return annotationType.equals(sourceAnnotationType);
            };
        }
        synchronized (qualifiers){
            return qualifiers.put(qualifierAnnotationClass, qualifierPredicate);
        }
    }

    public QualifierPredicate removeQualifier(Class<? extends Annotation> qualifierAnnotationClass){
        if(qualifiers!=null && !qualifiers.isEmpty() && qualifiers.containsKey(qualifierAnnotationClass)){
            synchronized (qualifiers){
                return qualifiers.remove(qualifierAnnotationClass);
            }
        }
        return null;
    }

    public boolean match(final Object beanSource, final Set<Annotation> qualifiers, boolean withAndEvaluation){
        if(qualifiers==null || qualifiers.isEmpty())
            return true; // by default beanSource is matching
        int configQualifiersNbr = 0;
        for (Annotation qualifierAnnotation : qualifiers){
            if(isConfigQualifier(qualifierAnnotation.annotationType())){
                configQualifiersNbr++;
                continue;
            }
            Predicate<Object> qualifierFilter = getQualifierPredicate(qualifierAnnotation);
            boolean matching = qualifierFilter!=null && qualifierFilter.test(beanSource);
            if(matching && !withAndEvaluation) // or evaluation => return true in first matching found
                return true;
            if (!matching && withAndEvaluation) // and evaluation => return false in first not matching
                return false;
        }
        if(configQualifiersNbr == qualifiers.size())
            logger.error("All qualifiers passed to match method are config qualifiers");
        // return true <=> isAnd if all matching else is or evaluation then  return false
        return withAndEvaluation && (configQualifiersNbr!=qualifiers.size());
    }

    @Override
    public void setDefaultQualifier(Annotation defaultQualifier) {
        if(defaultQualifier !=null && !isValidQualifierAnnotation(defaultQualifier.annotationType()))
            throw new FrameworkException("Default qualifier "+defaultQualifier+" is not valid");
        this.defaultQualifier = defaultQualifier;
    }

    @Override
    public Annotation getDefaultQualifier(Class<?> beanType) {
        return this.defaultQualifier;
    }

    public Set<Class> filterImplementations(final Set<Class> beanSources, final Set<Annotation> qualifiers){
        if(beanSources==null || beanSources.isEmpty() || qualifiers==null || qualifiers.isEmpty())
            return beanSources;
        Class firstFound = beanSources.stream().findFirst().get();
        Set<Class<? extends Annotation>> qualifiersTypes = qualifiers.stream().map(Annotation::annotationType).collect(Collectors.toSet());
        if(qualifiersTypes.contains(FirstFound.class)){
            if(qualifiers.size()>1)
                logger.warn("Injection point is annotated with @FirstFound qualifier, other qualifiers will be ignored");
            return Collections.singleton(firstFound);
        }
        Set<Class> matches = new HashSet<>();
        boolean isAllConfigQualifiers = qualifiersTypes.stream().allMatch(this::isConfigQualifier);
        boolean isAndEvaluation = qualifiersTypes.contains(EvalWithAND.class);
        boolean isOrEvaluation = qualifiersTypes.contains(EvalWithOR.class);
        if(isAndEvaluation && isOrEvaluation){
            logger.error("Opposite qualifiers @EvalWithAND and @EvalWithOR detected on the same injection point, you should eliminate one");
            return matches;
        }else if (isAllConfigQualifiers && (isAndEvaluation || isOrEvaluation)){
            logger.error("@EvalWithAND and @EvalWithOR can't be applied on config qualifiers, try to verify your annotated injection point");
            return matches;
        }else {
            for(Class obj : beanSources){
                boolean doesMatch = match(obj, qualifiers, isAndEvaluation);
                if(doesMatch)
                    matches.add(obj);
            }
        }
        boolean elseFirstFoundExists = qualifiersTypes.contains(ElseFirstFound.class);
        if(matches.isEmpty() && elseFirstFoundExists){
            if(qualifiers.size()>1)
                logger.info("No implementation matches given qualifiers, And @ElseFirstFound qualifier detected on injection point, we use first found");
            matches.add(firstFound);
        }
        return matches;
    }

    public Set<Method> filterFactories(final Set<Method> beanSources, final Set<Annotation> qualifiers){
        if(beanSources==null || beanSources.isEmpty() || qualifiers==null || qualifiers.isEmpty())
            return beanSources;
        Method firstFound = beanSources.stream().findFirst().get();
        Set<Class<? extends Annotation>> qualifiersTypes = qualifiers.stream().map(Annotation::annotationType).collect(Collectors.toSet());
        if(qualifiersTypes.contains(FirstFound.class)){
            if(qualifiers.size()>1)
                logger.warn("Injection point is annotated with @FirstFound qualifier, other qualifiers will be ignored");
            return Collections.singleton(firstFound);
        }
        Set<Method> matches = new HashSet<>();
        boolean isAllConfigQualifiers = qualifiersTypes.stream().allMatch(this::isConfigQualifier);
        boolean isAndEvaluation = qualifiersTypes.contains(EvalWithAND.class);
        boolean isOrEvaluation = qualifiersTypes.contains(EvalWithOR.class);
        if(isAndEvaluation && isOrEvaluation){
            logger.error("Opposite qualifiers @EvalWithAND and @EvalWithOR detected on the same injection point, you should eliminate one");
            return matches;
        }else if (isAllConfigQualifiers && (isAndEvaluation || isOrEvaluation)){
            logger.error("@EvalWithAND and @EvalWithOR can't be applied on config qualifiers, try to verify your annotated injection point");
            return matches;
        }else {
            for(Method obj : beanSources){
                boolean doesMatch = match(obj, qualifiers, isAndEvaluation);
                if(doesMatch)
                    matches.add(obj);
            }
        }
        boolean elseFirstFoundExists = qualifiersTypes.contains(ElseFirstFound.class);
        if(matches.isEmpty() && elseFirstFoundExists){
            if(qualifiers.size()>1)
                logger.info("No implementation matches given qualifiers, And @ElseFirstFound qualifier detected on injection point, we use first found");
            matches.add(firstFound);
        }
        return matches;
    }

    /*----------------------- Inner methods -------------------------*/
    public void update(boolean force){
        long lastUpdate = ClassFinder.getLastUpdateTimeStamp();
        if(lastUpdateTimeStamp < lastUpdate || force){
            updateCount++;
            logger.debug("Update Bean Qualifier Manager cache for the ["+updateCount+"] time(s), last update timestamp ["+lastUpdateTimeStamp+"]");
            lastUpdateTimeStamp = lastUpdate;
            scanForAvailableQualifier();
            if(qualifiers.isEmpty())
                logger.error("No qualifier class was found.");
        }
    }
    private Predicate<Object> getQualifierPredicate(Annotation qualifierAnnotation){
        Map<Class<? extends Annotation>, QualifierPredicate> qualifiers = getAvailableQualifiers();
        for(Class<? extends Annotation> annotationClass : qualifiers.keySet()){
            if(qualifierAnnotation.annotationType().isAssignableFrom(annotationClass)){
                QualifierPredicate qualifierPredicate = qualifiers.get(annotationClass);
                return (beanSource)-> qualifierPredicate.accept(qualifierAnnotation, beanSource);
            }
        }
        return null;
    }
    private boolean isValidQualifierConfigClass(Class clazz){
        QualifierConfig qualifierConfig = (QualifierConfig)  AnnotationTools.getAnnotation(clazz, QualifierConfig.class);
        if(qualifierConfig==null)
            return false;
        boolean isAbstract = Modifier.isAbstract(clazz.getModifiers());
        if(isAbstract){
            logger.error("Classes annotated with '@QualifierConfig' cant be abstract");
            return false;
        }
        boolean implementQualifierPredicate = QualifierPredicate.class.isAssignableFrom(clazz);
        if(!implementQualifierPredicate){
            logger.error("Classes annotated with '@QualifierConfig' should implement 'org.injection.core.qualifiers.QualifierPredicate' interface");
            return false;
        }
        Class<? extends Annotation> qualifierAnnotation = qualifierConfig.qualifierAnnotation();
        boolean isValidQualifierAnnotation = isValidQualifierAnnotation(qualifierAnnotation);
        if(!isValidQualifierAnnotation)
            logger.error("QualifierConfig should contain a valid Qualifier annotation class \n" +
                    "Try to annotate ["+qualifierAnnotation+"] with @javax.inject.Qualifier");
        return isValidQualifierAnnotation;
    }
    private void scanForAvailableQualifier(){
        logger.info("Start scanning for available qualifiers ...");
        long duration = System.currentTimeMillis();
        // add marker qualifiers
        Set<Class> flagQualifiers = ClassFinder.getClassesWithFilter(this::isValidQualifierAnnotation);
        flagQualifiers.forEach(aClass -> {
            addMarkerQualifier((Class<? extends Annotation>)aClass);
        });
        // add building annotation
        addBuildInQualifiers();
        // add qualifiers with predicate
        Set<Class> predicateQualifiers = ClassFinder.getClassesWithFilter(this::isValidQualifierConfigClass);
        predicateQualifiers.forEach(aClass -> {
                    QualifierConfig qualifierConfig = (QualifierConfig) AnnotationTools.getAnnotation(aClass, QualifierConfig.class);
                    Class<? extends Annotation> qualifierAnnotation = qualifierConfig.qualifierAnnotation();
                    QualifierPredicate qualifierPredicate = getQualifierConfigInstance(aClass);
                    addQualifier(qualifierAnnotation, qualifierPredicate);
                });
        duration = System.currentTimeMillis() - duration;
        logger.info("Scanning for available qualifiers ended in ["+duration+"] Millis with ["+qualifiers.size()+"] qualifier(s)");
    }
    private void addBuildInQualifiers(){
        // add @RegexQualifier
        addQualifier(RegexQualifier.class, (annotation, beanSource)-> {
            if(!(annotation instanceof RegexQualifier) || beanSource==null)
                return false;
            RegexQualifier regexQualifier = (RegexQualifier) annotation;
            try {
                "Test".matches(regexQualifier.regex());
            }catch (Throwable throwable){
                throw new FrameworkException("Regex from ["+regexQualifier+"] is not valid: "+throwable.getMessage());
            }
            return (beanSource instanceof Class)
                    && ((Class)beanSource).getCanonicalName().matches(regexQualifier.regex());
        });
        // add @javax.inject.Named
        addQualifier(Named.class, (annotation, beanSource)-> {
            if(!(annotation instanceof Named))
                return false;
            Named injectionPointNamedAnnotation = (Named) annotation;
            Annotation sourceAnnotation = AnnotationTools.getAnnotation(beanSource, Named.class);
            if(!(sourceAnnotation instanceof Named))
                return false;
            Named sourceNamedAnnotation = (Named) sourceAnnotation;
            String injectionPointNamedValue = injectionPointNamedAnnotation.value();
            String sourceNamedValue = sourceNamedAnnotation.value();
            return injectionPointNamedValue!=null
                    && injectionPointNamedValue.equals(sourceNamedValue);
        });
        // add markers to be recognized
        configQualifiers.forEach(aClass -> {
            addQualifier(aClass, (annotation, beanSource)-> {
                throw new FrameworkException("@"+aClass.getCanonicalName()+" qualifier is just a marker and can't have à predicate");
            });
        });
    }
    private QualifierPredicate getQualifierConfigInstance(Class qualifierConfigClass){
        try {
            Constructor constructor = qualifierConfigClass.getConstructor();
            return  (QualifierPredicate) constructor.newInstance();
        }catch (Exception e){
            throw new FrameworkException("Can't get instance of qualifier config class: ["+qualifierConfigClass+"]" +
                    "\nusing a non argument constructor");
        }
    }
    private boolean isConfigQualifier(Class<? extends Annotation> annotationClass){
        return  configQualifiers.contains(annotationClass);
    }
}
