package org.web.core.processors;

import org.tools.Log;
import org.tools.annotations.AnnotationTools;
import org.tools.exceptions.FrameworkException;
import org.web.annotations.methods.Route;
import org.web.annotations.params.global.Names;
import org.web.annotations.params.global.ParamSrc;
import org.web.annotations.params.global.Source;
import org.web.core.ControllerConfig;
import org.web.core.MethodInfoBuilder;
import org.web.core.ParamInfoBuilder;
import org.web.data.MethodInfo;
import org.web.data.ParamInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MethodInfoBuilderImpl implements MethodInfoBuilder {
    private static final Log logger = Log.getInstance(MethodInfoBuilderImpl.class);
    private final ControllerConfig Config;
    private final ParamInfoBuilder paramInfoBuilder;

    public MethodInfoBuilderImpl(ControllerConfig controllerConfig, ParamInfoBuilder paramInfoBuilder){
        this.Config = controllerConfig;
        this.paramInfoBuilder = paramInfoBuilder;
    }

    public MethodInfo build(Method method) {
        Annotation[] annotations = method.getAnnotations();
        MethodInfo methodInfo = null;
        for(Annotation annotation: annotations){ // TODO can be optimized (see later)
            if(annotation instanceof Route){
                Route routeAnnotation = (Route) annotation;
                methodInfo = new MethodInfo();
                methodInfo.setName(method.getName());
                methodInfo.setHttpMethod(routeAnnotation.method().name());
                methodInfo.setPath(routeAnnotation.route());
                methodInfo.setParamInfoSet(prepareParamInfo(method));
                break;
            }else {
                Class annotationType = annotation.annotationType();
                if(annotationType.isAnnotationPresent(Route.class)){
                    try {
                        Method routeMethod = annotationType.getMethod("route", new Class[]{});
                        String route = (String) routeMethod.invoke(annotation, new Object[]{});
                        String httpMethod = ((Route) annotationType.getAnnotation(Route.class))
                                .method().name();
                        methodInfo = new MethodInfo();
                        methodInfo.setName(method.getName());
                        methodInfo.setHttpMethod(httpMethod);
                        methodInfo.setPath(route);
                        methodInfo.setParamInfoSet(prepareParamInfo(method));
                        break;
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            /*
            if(annotation instanceof POST){
                POST post = (POST) annotation;
                methodInfo = new MethodInfo();
                methodInfo.setName(method.getName());
                methodInfo.setHttpMethod(post.method());
                methodInfo.setPath(post.route());
                methodInfo.setParamInfoSet(prepareParamInfo(method));
                break;
            }

            if(annotation instanceof GET){
                GET get = (GET) annotation;
                methodInfo = new MethodInfo();
                methodInfo.setName(method.getName());
                methodInfo.setHttpMethod(get.method());
                methodInfo.setPath(get.route());
                methodInfo.setParamInfoSet(prepareParamInfo(method));
                break;
            }

             */
        }
        return methodInfo;
    }

    public Set<ParamInfo> prepareParamInfo(Method method){
        Set<ParamInfo> methodParamInfoSet = new HashSet<>();
        for (Parameter parameter : method.getParameters()){
            String usedNameFromMethodAnnotation = getParamNameFromMethodAnnotation(method, parameter);
            ParamSrc paramSrcFromMethodAnnotation = getParamSourceFromMethodAnnotation(method, parameter);
            ParamInfo paramInfo = paramInfoBuilder
                    .build(parameter,
                            usedNameFromMethodAnnotation,
                            paramSrcFromMethodAnnotation);
            methodParamInfoSet.add(paramInfo);
        }
        return methodParamInfoSet;
    }

    public String getParamNameFromMethodAnnotation(Method method , Parameter parameter){
        if(Config.isInjectableParam(parameter.getType()))
            return null;
        Annotation namesAnnotation = AnnotationTools
                .getAnnotation(method, Names.class);
        if(namesAnnotation!=null){
            try {
                Method nameMethod = namesAnnotation
                        .annotationType()
                        .getMethod("names", null);
                String[] names = (String[]) nameMethod.invoke(namesAnnotation, null);
                List<Parameter> validParams = Arrays
                        .stream(method.getParameters())
                        .filter(parameter1 -> !Config.isInjectableParam(parameter1.getType())) // TODO pay attention here we can add
                        .collect(Collectors.toList());
                List<String> validParamsTypes = validParams
                        .stream()
                        .map(parameter1 -> parameter1.getType().getName())
                        .collect(Collectors.toList());
                if(names==null)
                    throw new FrameworkException("Names annotation on ["+method+"] can't be empty ["+namesAnnotation+"]");
                if (names.length>validParams.size())
                    logger.error("Number of names greater than valid parameters " +
                            "\nNames annotation on ["+method+"] not properly configured, " +
                            "\nNames ["+namesAnnotation+"] " +
                            "\nValid params: ["+validParamsTypes+"]");
                if(names.length<validParams.size())
                    throw new FrameworkException("Number of names less than number of valid parameters " +
                            "\nNames annotation on ["+method+"] doesn't contain all params," +
                            "\nNames ["+namesAnnotation+"] " +
                            "\nValid params: ["+validParamsTypes+"]");
                int paramIndex = validParams.indexOf(parameter);
                if(paramIndex>-1 && !names[paramIndex].trim().isEmpty()){
                    return names[paramIndex];
                }else if(paramIndex>-1){
                    throw new FrameworkException("Name can't be empty on param ["+parameter+"] " +
                            "\nNames annotation on ["+method+"] is invalid, " +
                            "\nNames: ["+namesAnnotation+"] ");
                }else{
                    throw new FrameworkException("Can't found parameter: ["+parameter+"] " +
                            "\nNames annotation on ["+method+"] is invalid, " +
                            "\nNames: ["+namesAnnotation+"] ");
                }
            } catch (NoSuchMethodException e) {
                throw new FrameworkException(e);
            } catch (InvocationTargetException e) {
                throw new FrameworkException(e);
            } catch (IllegalAccessException e) {
                throw new FrameworkException(e);
            }
        }
        return null;
    }

    public ParamSrc getParamSourceFromMethodAnnotation(Method method , Parameter parameter){
        if(Config.isInjectableParam(parameter.getType()))
            return null;
        Annotation sourceAnnotation = AnnotationTools
                .getAnnotation(method, Source.class);
        if(sourceAnnotation!=null){
            try {
                Method srcMethod = sourceAnnotation
                        .annotationType()
                        .getMethod("src", null);
                ParamSrc src = (ParamSrc) srcMethod.invoke(sourceAnnotation, null);
                if(src!=null)
                    return src;
                else{
                    throw new FrameworkException("Source annotation on ["+parameter+"] can't be empty ["+sourceAnnotation+"]");
                }
            } catch (NoSuchMethodException e) {
                throw new FrameworkException(e);
            } catch (InvocationTargetException e) {
                throw new FrameworkException(e);
            } catch (IllegalAccessException e) {
                throw new FrameworkException(e);
            }
        }
        return null;
    }

}
