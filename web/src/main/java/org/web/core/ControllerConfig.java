package org.web.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Set;

public interface ControllerConfig {
    Set<Class> getInjectableParamsClasses();
    boolean isInjectableParam(Class paramType);
    boolean isRouteAnnotation(Class<? extends Annotation> annotationClass);
    Set<Class<? extends Annotation>> getRouteAnnotations();
    Object getRouteInjectedParamValue(HttpServletRequest request, HttpServletResponse response, Class<?> paramType);
    Object getFormattedValue(Object value, Class<?> type);
}
