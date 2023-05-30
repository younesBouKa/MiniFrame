package org.aspect.scanners;

import org.tools.ClassFinder;
import org.tools.Log;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;

public class AspectScanManagerImpl implements AspectScanManager {
    private static final Log logger = Log.getInstance(AspectScanManagerImpl.class);
    private final Map<Annotation, Method> adviceCache = new Hashtable<>();
    private long lastUpdateTimeStamp = 0;
    private long updateCount = 0;

    public AspectScanManagerImpl(){
        update(true);
    }
    /*-------------------------- Core methods  --------------------------------*/
    public Map<Annotation, Method> getAdvices(Method targetMethod, Class<? extends Annotation> adviceAnnotationType){
        Map<Annotation, Method> advices = new Hashtable<>();
        for (Annotation annotation : adviceCache.keySet()){
            if(
                    annotation.annotationType().isAssignableFrom(adviceAnnotationType)
                            && doesPointCutMatch(targetMethod, annotation)
            ){
                advices.put(annotation, adviceCache.get(annotation));
            }
        }
        return advices;
    }
    public void addAdviceMethod(Method method, Annotation adviceAnnotation){
        if(isValidAdviceAnnotation(adviceAnnotation))
            adviceCache.put(adviceAnnotation, method);
        else
            logger.warn("Annotation "+adviceAnnotation+" is not a valid advice annotation");
    }
    /*---------------------- inner methods --------------------------*/
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
