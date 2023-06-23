package org.web.core.helpers;

import com.sun.net.httpserver.HttpExchange;
import org.tools.annotations.AnnotationTools;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;
import org.web.annotations.methods.*;
import org.web.core.ControllerConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ControllerConfigImpl implements ControllerConfig {
    private static final Log logger = Log.getInstance(ControllerConfigImpl.class);
    private static Set<Class> paramInjectableClasses;
    private static Set<Class<? extends Annotation>> ROUTE_ANNOTATIONS;

    static {
        paramInjectableClasses = Stream
                .of(    HttpExchange.class,
                        HttpServletRequest.class,
                        HttpServletResponse.class,
                        Principal.class,
                        HttpSession.class,
                        Part.class
                )
                .collect(Collectors.toSet());
        ROUTE_ANNOTATIONS = Stream
                .of(Post.class, Get.class, Put.class, Delete.class, Route.class)
                .collect(Collectors.toSet());
    }

    public ControllerConfigImpl(){

    }

    public Set<Class> getInjectableParamsClasses(){
        return paramInjectableClasses;
    }

    public boolean isInjectableParam(Class paramType){
        for(Class clazz : getInjectableParamsClasses()){
            if(clazz.equals(paramType) || clazz.isAssignableFrom(paramType))
                return true;
        }
        return false;
    }

    public boolean isRouteAnnotation(Class<? extends Annotation> annotationClass){
        for(Class<? extends Annotation> clazz : getRouteAnnotations()){
            if(clazz.equals(annotationClass) || AnnotationTools.getAnnotation(annotationClass, clazz)!=null)
                return true;
        }
        return false;
    }

    public Set<Class<? extends Annotation>> getRouteAnnotations() {
        return ROUTE_ANNOTATIONS;
    }

    /**
     * Inject some params type into method parameters
     * see Config.isInjectableParam
     * @param request
     * @param response
     * @param paramType
     * @return
     */
    public Object getRouteInjectedParamValue(
            HttpServletRequest request,
            HttpServletResponse response,
            Class<?> paramType,
            String paramName
    ) {
        Object injectedValue = null;
        if(isInjectableParam(paramType)){
            if(paramType.isAssignableFrom(HttpServletRequest.class))
                injectedValue = paramType.cast(request);
            if(paramType.isAssignableFrom(HttpServletResponse.class))
                injectedValue = paramType.cast(response);
            if(paramType.isAssignableFrom(Principal.class))
                injectedValue = paramType.cast(request.getUserPrincipal());
            if(paramType.isAssignableFrom(HttpSession.class))
                injectedValue = paramType.cast(request.getSession());
            if(paramType.isAssignableFrom(Part.class)){
                try {
                    if(paramName!=null) {
                        injectedValue = paramType.cast(request.getPart(paramName));
                    }
                    if (!request.getParts().isEmpty())
                        injectedValue = paramType.cast(request.getParts().toArray()[0]);
                } catch (IOException e) {
                    throw new FrameworkException(e);
                } catch (ServletException e) {
                    throw new FrameworkException(e);
                }
            }
            if(injectedValue==null)
                logger.error("Can't get value of injected parameter "+paramType);
        }
        return injectedValue;
    }

    /**
     * Format values
     * @param value
     * @param type
     * @return
     */
    public Object getFormattedValue(Object value, Class<?> type){
        if(value==null)
            return value;
        try {
            if(type.equals(Integer.class) || type.equals(int.class))
                return Integer.valueOf((String) value);
            if(type.equals(Double.class) || type.equals(double.class))
                return Double.valueOf((String) value);
            if(type.equals(Float.class) || type.equals(float.class))
                return Float.valueOf((String) value);
            if(type.equals(Byte.class) || type.equals(byte.class))
                return Byte.valueOf((String) value);
            if(type.equals(Character.class) || type.equals(char.class))
                return value;
            if(type.equals(Boolean.class) || type.equals(boolean.class))
                return Boolean.valueOf((String) value);
            if(type.equals(Short.class) || type.equals(short.class))
                return Short.valueOf((String) value);
            if(type.equals(Long.class) || type.equals(long.class))
                return Long.valueOf((String) value);
            if(type.equals(String.class))
                return String.valueOf(value);
            if(type.isAssignableFrom(value.getClass()))
                return type.cast(value);
            logger.warn("Format value, type: "+type.getName()+", value type: "+value.getClass());
        }catch (Throwable throwable){
            logger.error(throwable);
        }
        return value;
    }
}
