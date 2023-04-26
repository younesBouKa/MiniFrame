package org.tools.annotations;

import org.tools.ClassFinder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

public class AnnotationTree {
    private static Set<Class> allAvailableAnnotations;
    private static Set<Class> rootAnnotations;
    private static Set<Class> leafAnnotations;
    private static Set<Class> orphanAnnotations;

    static {
        scanForAnnotations(null);
    }

    private static void scanForAnnotations(Predicate<Class> filter){
        if(filter==null)
            filter = Class::isAnnotation;
        allAvailableAnnotations = ClassFinder.getClassesWithFilter(filter);
        rootAnnotations = prepareRootAnnotations();
        leafAnnotations = prepareLeafAnnotations();
        orphanAnnotations = prepareOrphanAnnotations();
    }

    public static Set<Class> prepareRootAnnotations(){
        Set<Class> routAnnotations = new HashSet<>();
        for (Class clazz : allAvailableAnnotations){
            if(isRootAnnotation(clazz)){
                routAnnotations.add(clazz);
            }
        }
        return routAnnotations;
    }

    public static boolean isRootAnnotation(Class annotationClass){
        if(isRecursiveAnnotation(annotationClass))
            return true;
        for (Annotation anno : annotationClass.getAnnotations()){
            if(!isRecursiveAnnotation(anno.annotationType()))
                return false;
        }
        return true;
    }

    public static Set<Class> prepareLeafAnnotations(){
        Set<Class> leafAnnotations = new HashSet<>();
        for (Class clazz : allAvailableAnnotations){
            if(isLeafAnnotation(clazz)){
                leafAnnotations.add(clazz);
            }
        }
        return leafAnnotations;
    }

    public static boolean isLeafAnnotation(Class annotationClass){
        for (Class clazz : allAvailableAnnotations){
            if(clazz.equals(annotationClass))
                continue;
            if(clazz.getAnnotation(annotationClass)!=null)
                return false;
        }
        return true;
    }

    public static boolean isRecursiveAnnotation(Class annotationClass){
        return AnnotationTools
                .getRecursiveAnnotations()
                .contains(annotationClass);
    }

    public static List<Class> getPathOfAnnotations(Class from, Class to){
        List<Class> path = new ArrayList<>();
        if(to.equals(from)){
            path.add(to);
            return path;
        }
        if(isRootAnnotation(from))
            return path;
        path.add(from);
        for(Annotation annotation : from.getAnnotations()){
            path.addAll(getPathOfAnnotations(annotation.annotationType(), to));
        }
        return path;
    }


    public static Set<Class> prepareOrphanAnnotations(){
        Set<Class> orphanAnnotations = new HashSet<>();
        for (Class clazz : allAvailableAnnotations){
            if(isRootAnnotation(clazz) && isLeafAnnotation(clazz)){
                orphanAnnotations.add(clazz);
            }
        }
        return orphanAnnotations;
    }

    public static Set<Class> getOrphanAnnotations(){
        return orphanAnnotations;
    }

    public static Set<Class> getRootAnnotations(){
        return rootAnnotations;
    }

    public static Set<Class> getLeafAnnotations(){
        return leafAnnotations;
    }

    public static Set<Class> getAllAvailableAnnotations() {
        return allAvailableAnnotations;
    }

    public static Map<String, Object> getAllAnnotationsValues(Annotation from, Class to){
        Map<String, Object> values = new HashMap<>();
        List<Class> path = getPathOfAnnotations(from.annotationType(), to);
        if(path.size()>0)
            values.putAll(getAnnotationValues(from));
        for (int i=0; i<path.size(); i++){ // TODO order to see later
            Class currentChild = path.get(i);
            Annotation annotation = AnnotationTools.getAnnotation(from.annotationType(), currentChild);
            values.putAll(getAnnotationValues(annotation));
        }
        return values;
    }

    public static Map<String, Object> getAnnotationValues(Annotation annotation){
        Map<String, Object> values = new HashMap<>();
        if (annotation == null)
            return values;
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            try {
                Object value = method.invoke(annotation, new Object[]{});
                if (value != null)
                    values.put(method.getName(), value);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return values;
    }
}
