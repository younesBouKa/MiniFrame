package org.tools.annotations;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnnotationTools {
    private static final List<Class> recursiveAnnotations;
    private static Map<Object, List<Annotation>> cache;

    static {
        cache = new HashMap<>();
        recursiveAnnotations = Stream
                .of(
                        Retention.class,
                        Target.class,
                        Documented.class,
                        Deprecated.class//,
                        //Inherited.class,
                        //Native.class,
                        //Repeatable.class
                )
                .collect(Collectors.toList());
    }

    public static boolean isAnnotationPresent(Object obj, Class annotationClass){
        Annotation annotation = null;
        if(obj instanceof Field)
            annotation = getAnnotation((Field) obj, annotationClass);
        if(obj instanceof Parameter)
            annotation = getAnnotation((Parameter)obj, annotationClass);
        if(obj instanceof Constructor)
            annotation = getAnnotation((Constructor)obj, annotationClass);
        if(obj instanceof Method)
            annotation = getAnnotation((Method) obj, annotationClass);
        if(obj instanceof Executable)
            annotation = getAnnotation((Executable) obj, annotationClass);
        if(obj instanceof Class)
            annotation = getAnnotation((Class) obj, annotationClass);
        return annotation!=null;
    }

    public static Annotation getAnnotation(Object obj, Class annotationClass){
        Annotation annotation = null;
        if(obj instanceof Field)
            annotation = getAnnotation((Field) obj, annotationClass);
        if(obj instanceof Parameter)
            annotation = getAnnotation((Parameter)obj, annotationClass);
        if(obj instanceof Constructor)
            annotation = getAnnotation((Constructor)obj, annotationClass);
        if(obj instanceof Method)
            annotation = getAnnotation((Method) obj, annotationClass);
        if(obj instanceof Executable)
            annotation = getAnnotation((Executable) obj, annotationClass);
        if(obj instanceof Class)
            annotation = getAnnotation((Class) obj, annotationClass);
        return annotation;
    }

    public static Annotation getAnnotation(Constructor constructor, Class annotationClass){
        String key = constructor.getDeclaringClass().getCanonicalName()+"."
                +constructor.getName()+"."
                +constructor.getParameterCount()+"."
                +constructor.hashCode();
        Annotation annotation = getFromCache(key, annotationClass);
        if(annotation!=null){
            return annotation;
        }
        annotation = searchInAnnotations(constructor.getAnnotations(), annotationClass);
        addToCache(key, annotation);
        return annotation;
    }

    public static Annotation getAnnotation(Parameter parameter, Class annotationClass){
        String key = parameter.getDeclaringExecutable().getName()+"."
                +parameter.getName();
        Annotation annotation = getFromCache(key, annotationClass);
        if(annotation!=null){
            return annotation;
        }
        annotation = searchInAnnotations(parameter.getAnnotations(), annotationClass);
        addToCache(key, annotation);
        return annotation;
    }

    public static Annotation getAnnotation(Field field, Class annotationClass){
        String key = field.getDeclaringClass().getCanonicalName()+"."+field.getName();
        Annotation annotation = getFromCache(key, annotationClass);
        if(annotation!=null){
            return annotation;
        }
        annotation = searchInAnnotations(field.getAnnotations(), annotationClass);
        addToCache(key, annotation);
        return annotation;
    }

    public static Annotation getAnnotation(Executable executable, Class annotationClass){
        String key = executable.getDeclaringClass().getCanonicalName()+"."+executable.getName();
        Annotation annotation = getFromCache(key, annotationClass);
        if(annotation!=null){
            return annotation;
        }
        annotation = searchInAnnotations(executable.getAnnotations(), annotationClass);
        addToCache(key, annotation);
        return annotation;
    }


    public static Annotation getAnnotation(Method method, Class annotationClass){
        String key = method.getDeclaringClass().getCanonicalName()+"."+
                method.getName()+"."+
                method.getReturnType().getCanonicalName()+"."+
                method.getParameterCount();
        Annotation annotation = getFromCache(key, annotationClass);
        if(annotation!=null){
            return annotation;
        }
        annotation = searchInAnnotations(method.getAnnotations(), annotationClass);
        addToCache(key, annotation);
        return annotation;
    }

    public static Annotation getAnnotation(Class clazz, Class annotationClass){
        String key = clazz.getCanonicalName();
        Annotation annotation = getFromCache(key, annotationClass);
        if(annotation!=null){
            return annotation;
        }
        annotation = searchInAnnotations(clazz.getAnnotations(), annotationClass);
        addToCache(key, annotation);
        return annotation;
    }

    /**
     * Add annotation with its type to cache
     * Key must be unique for class, method, field or param
     * @param key
     * @param annotation
     * @return
     */
    public static boolean addToCache(Object key, Annotation annotation){
        if(key!=null && annotation!=null){
            if(!cache.containsKey(key))
                cache.put(key, new ArrayList<>());
            return cache.get(key)
                    .add(annotation);
        }
        return false;
    }

    /**
     * Get annotation from cache, return null if not founded
     * @param key
     * @param annotationClass
     * @return
     */
    public static Annotation getFromCache(Object key, Class annotationClass){
        if(
                key!=null
                && annotationClass!=null
                && cache.containsKey(key)
                && !cache.get(key).isEmpty()
        ) {
            Annotation annotation = cache
                    .get(key)
                    .stream()
                    .filter(anno-> anno!=null && anno.annotationType().equals(annotationClass))
                    .findFirst()
                    .orElse(null);
            return annotation;
        }
        return null;
    }

    /**
     * Search for given annotation class in given annotation
     * If found return it else call getAnnotation(annotation, annotationClass) to search recursively
     * Return null if no annotation was found
     * @param annotations
     * @param annotationClass
     * @return
     */
    public static Annotation searchInAnnotations(Annotation[] annotations, Class annotationClass){
        for(Annotation annotation : annotations){
            if(annotation.annotationType().equals(annotationClass))
                return annotation;
        }

        for(Annotation annotation : annotations){
            Annotation anno = getAnnotation(annotation, annotationClass);
            if(anno!=null)
                return anno;
        }

        return null;
    }

    /**
     * Search recursively for given annotation class in given annotation:
     *      - return given annotation :
     *          + if its the same type of annotation class
     *          + if annotation bellow to recursive annotation Target, Retention, Documented
     *      - Else search if the first level
     *      - Else search recursively in second level
     *      - If no annotation found return null
     * @param annotation
     * @param annotationClass
     * @return
     */
    private static Annotation getAnnotation(Annotation annotation, Class annotationClass){
        if(annotation==null || annotationClass==null)
            return null;
        if(annotation.annotationType().equals(annotationClass))
            return annotation;
        if(recursiveAnnotations.contains(annotation.annotationType()))
            return null;
        Annotation[] annotations = annotation
                .annotationType()
                .getDeclaredAnnotations();
        for(Annotation anno_1 : annotations){
            if(anno_1.annotationType().equals(annotationClass))
                return anno_1;
        }

        for(Annotation anno_1 : annotations){
            if(!annotation.annotationType().equals(anno_1.annotationType())) {
                Annotation anno_2 = getAnnotation(anno_1, annotationClass);
                if (anno_2 != null)
                    return anno_2;
            }
        }
        return null;
    }

    public static List<Class> getRecursiveAnnotations(){
        return recursiveAnnotations;
    }
}
