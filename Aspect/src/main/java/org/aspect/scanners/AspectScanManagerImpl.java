package org.aspect.scanners;

import org.aspect.annotations.*;
import org.aspect.annotations.enums.AdviceType;
import org.aspect.annotations.enums.CutPointType;
import org.aspect.annotations.enums.ExecPosition;
import org.aspect.annotations.pointcuts.AnnotatedWith;
import org.aspect.annotations.pointcuts.Expression;
import org.aspect.annotations.pointcuts.TargetClass;
import org.tools.ClassFinder;
import org.tools.Log;
import org.tools.annotations.AnnotationTools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Predicate;

public class AspectScanManagerImpl implements AspectScanManager {
    private static final Log logger = Log.getInstance(AspectScanManagerImpl.class);
    private final Map<Annotation, Method> adviceCache = new Hashtable<>();
    private long lastUpdateTimeStamp = 0;
    private long updateCount = 0;

    public AspectScanManagerImpl(){
        update(true);
    }
    /*-------------------------- Core methods  --------------------------------*/
    public Map<Annotation, Method> getAdvices(Method targetMethod){
        Map<Annotation, Method> advices = new Hashtable<>();
        for (Annotation annotation : adviceCache.keySet()){
            if(doesPointCutMatch(targetMethod, annotation)){
                advices.put(annotation, adviceCache.get(annotation));
            }
        }
        return advices;
    }
    public Map<Annotation, Method> getAdvices(Method targetMethod, Predicate<Annotation> filter){
        Map<Annotation, Method> advices = getAdvices(targetMethod);
        Map<Annotation, Method> filteredAdvices = new Hashtable<>();
        for (Annotation annotation : advices.keySet()){
            if(filter.test(annotation)){
                filteredAdvices.put(annotation, advices.get(annotation));
            }
        }
        return filteredAdvices;
    }
    public void addAdviceMethod(Method method, Annotation adviceAnnotation){
        if(isValidAdviceAnnotation(adviceAnnotation)){
            adviceCache.put(adviceAnnotation, method);
        }
        else
            logger.warn("Annotation "+adviceAnnotation+" is not a valid advice annotation");
    }
    public void addAdviceMethod(Method method){
        Advice adviceAnnotation = extractAdviceParts(method);
        adviceCache.put(adviceAnnotation, method);
    }
    /*---------------------- inner methods --------------------------*/
    public Advice extractAdviceParts(Method method){
        Advice advice = (Advice) AnnotationTools.getAnnotation(method, Advice.class);
        if(advice!=null){
            return advice;
        }
        AdviceType adviceType = AdviceType.CALL;
        ExecPosition execPosition = ExecPosition.BEFORE;
        CutPointType cutPointType = CutPointType.METHOD_REGEX;
        String cutPointValue = "(.)*";
        int order = 1;

        // extract point cut type
        CutPoint cutPoint = (CutPoint)AnnotationTools.getAnnotation(method, CutPoint.class);
        if(cutPoint!=null) {
            cutPointType = cutPoint.cutPointType();
        }
        // extract advice type
        Type type = (Type)AnnotationTools.getAnnotation(method, Type.class);
        if(type!=null) {
            adviceType = type.adviceType();
        }
        // extract exec position
        Position position = (Position)AnnotationTools.getAnnotation(method, Position.class);
        if(position!=null) {
            execPosition = position.execPosition();
        }
        // extract order
        Order ord = (Order)AnnotationTools.getAnnotation(method, Order.class);
        if(ord!=null) {
            order = ord.order();
        }
        // extract cut point value
        AnnotatedWith annotatedWith = (AnnotatedWith)AnnotationTools.getAnnotation(method, AnnotatedWith.class);
        if(annotatedWith!=null) {
            cutPointValue = annotatedWith.annotationClass().getCanonicalName();
        }
        Expression expression = (Expression)AnnotationTools.getAnnotation(method, Expression.class);
        if(expression!=null) {
            cutPointValue = expression.regex();
        }
        TargetClass targetClass = (TargetClass)AnnotationTools.getAnnotation(method, TargetClass.class);
        if(targetClass!=null) {
            cutPointValue = targetClass.target().getCanonicalName();
        }
        return createAdviceAnnotation(adviceType, execPosition, cutPointType, cutPointValue, order);
    }
    public Advice createAdviceAnnotation(AdviceType adviceType, ExecPosition execPosition, CutPointType cutPointType, String cutPointValue, int order){
        return new Advice(){
            @Override
            public Class<? extends Annotation> annotationType() {
                return Advice.class;
            }

            @Override
            public AdviceType adviceType() {
                return adviceType;
            }

            @Override
            public ExecPosition execPosition() {
                return execPosition;
            }

            @Override
            public CutPointType cutPointType() {
                return cutPointType;
            }

            @Override
            public String cutPointValue() {
                return cutPointValue;
            }

            @Override
            public int order() {
                return order;
            }
        };
    }
    public void update(boolean force){
        long lastUpdate = ClassFinder.getLastUpdateTimeStamp();
        if(lastUpdateTimeStamp < lastUpdate || force){
            updateCount++;
            logger.debug("Update aspect scan manager cache for the ["+updateCount+"] time(s), last update timestamp ["+lastUpdateTimeStamp+"]");
            lastUpdateTimeStamp = lastUpdate;
            adviceCache.clear();
            scanForAdvices();
            if(adviceCache.isEmpty())
                logger.error("No advice was found.");
        }
    }
    private void scanForAdvices(){
        logger.debug("Scanning for advices ... ");
        ClassFinder.getClassesWithFilter(this::isValidAspect)
                .forEach(clazz ->{
                    Method[] methods = clazz.getDeclaredMethods();
                    for(Method method : methods){
                        if(isValidAdvice(method)){
                            addAdviceMethod(method);
                        }
                    }
                });
        logger.debug("Scanning for advices ended: ["+adviceCache.size()+"]");
        if(adviceCache.isEmpty())
            logger.warn("No advice is available");
    }

}
