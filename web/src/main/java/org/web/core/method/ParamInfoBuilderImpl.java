package org.web.core.method;

import org.tools.annotations.AnnotationTools;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;
import org.web.WebConfig;
import org.web.annotations.params.global.Names;
import org.web.annotations.params.global.Param;
import org.web.annotations.params.global.ParamSrc;
import org.web.annotations.params.global.Source;
import org.web.annotations.params.types.BodyParam;
import org.web.annotations.params.types.HeaderParam;
import org.web.annotations.params.types.PathParam;
import org.web.annotations.params.types.QueryParam;
import org.web.core.config.ControllerConfig;
import org.web.data.ParamInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParamInfoBuilderImpl implements ParamInfoBuilder {
    private static final Log logger = Log.getInstance(ParamInfoBuilderImpl.class);
    private static final Set<Class> paramTypeAnnotations;
    private ControllerConfig controllerConfig;

    static {
        paramTypeAnnotations = Stream
                .of(QueryParam.class, PathParam.class, BodyParam.class, HeaderParam.class)
                .collect(Collectors.toSet());
    }

    public ParamInfoBuilderImpl(ControllerConfig controllerConfig) {
        this.controllerConfig = controllerConfig;
    }


    @Override
    public void autoConfigure() {
        this.controllerConfig = WebConfig.getControllerConfig();
    }


    public ParamInfo build(Parameter parameter, String usedName, ParamSrc source) {
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setParameter(parameter);
        paramInfo.setName(parameter.getName());
        paramInfo.setType(parameter.getType());
        paramInfo.setUsedName(usedName);
        paramInfo.setParamType(source);
        parseForParamNameAndType(paramInfo);
        controlSourceAndUsedName(paramInfo);
        return paramInfo;
    }

    public ParamInfo build(Parameter parameter) {
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setParameter(parameter);
        paramInfo.setName(parameter.getName());
        paramInfo.setType(parameter.getType());
        parseForParamNameAndTypeFromExecutable(paramInfo); // TODO to see later if i have to let it here
        parseForParamNameAndType(paramInfo);
        controlSourceAndUsedName(paramInfo);
        return paramInfo;
    }

    private void controlSourceAndUsedName(ParamInfo paramInfo) {
        if(controllerConfig.isInjectableParam(paramInfo.getParameter().getType()))
            return;
        if (paramInfo.getParamType()==null || paramInfo.getUsedName()==null)
            logger.error("Param from :" +
                    paramInfo.getParameter().getDeclaringExecutable().getDeclaringClass().getCanonicalName()
                    + "." + paramInfo.getParameter().getDeclaringExecutable().getName()
                    + "." + paramInfo.getName()
                    + "[" + paramInfo.getType().getName() + "]"
                    + " must have @Param annotation or one of those annotations : " + paramTypeAnnotations
            );
    }

    public void parseForParamNameAndType(ParamInfo paramInfo){
        if(controllerConfig.isInjectableParam(paramInfo.getParameter().getType()))
            return;
        Annotation directAnnotation = null;
        // search for typed annotation
        for(Class paramType : paramTypeAnnotations){
            directAnnotation = paramInfo.getParameter().getDeclaredAnnotation(paramType);
            if(directAnnotation!=null){
                break;
            }
        }
        // from typed annotation
        if(directAnnotation!=null){
            try {
                Method nameMethod = directAnnotation.annotationType().getMethod("name", null);
                String name = (String) nameMethod.invoke(directAnnotation, null);
                if(name!=null && !name.trim().isEmpty())
                    paramInfo.setUsedName(name);
                else if (name!=null){
                    throw new FrameworkException("Name can't be empty on param ["+paramInfo.getParameter()+"] " +
                            "\nNames annotation on ["+paramInfo.getParameter().getDeclaringExecutable()+"] is invalid, " +
                            "\nNames: ["+directAnnotation+"] ");
                }
                Annotation nonDirectAnnotation = AnnotationTools.getAnnotation(directAnnotation.annotationType(), Source.class);
                if(nonDirectAnnotation!=null){
                    Source paramAnnotation = (Source) nonDirectAnnotation;
                    paramInfo.setParamType(paramAnnotation.src());
                }
                // return;
            } catch (NoSuchMethodException e) {
                throw new FrameworkException(e);
            } catch (InvocationTargetException e) {
                throw new FrameworkException(e);
            } catch (IllegalAccessException e) {
                throw new FrameworkException(e);
            }
        }else{
            directAnnotation = AnnotationTools.getAnnotation(paramInfo.getParameter(), Param.class);
        }
        // from Param annotation if exists
        if(directAnnotation instanceof Param){
            Param paramAnnotation = (Param)directAnnotation;
            paramInfo.setUsedName(paramAnnotation.name());
            paramInfo.setParamType(paramAnnotation.type());
            // return;
        }
    }

    public void parseForParamNameAndTypeFromExecutable(ParamInfo paramInfo){
        if(controllerConfig.isInjectableParam(paramInfo.getParameter().getType()))
            return;
        Annotation sourceAnnotation = AnnotationTools
                .getAnnotation(paramInfo
                                .getParameter()
                                .getDeclaringExecutable(),
                        Source.class);
        if(sourceAnnotation!=null){
            try {
                Method srcMethod = sourceAnnotation
                        .annotationType()
                        .getMethod("src", null);
                ParamSrc src = (ParamSrc) srcMethod.invoke(sourceAnnotation, null);
                if(src!=null)
                    paramInfo.setParamType(src);
                else{
                    throw new FrameworkException("Source annotation on ["+paramInfo
                            .getParameter()
                            .getDeclaringExecutable()+"] can't be empty ["+sourceAnnotation+"]");
                }
            } catch (NoSuchMethodException e) {
                throw new FrameworkException(e);
            } catch (InvocationTargetException e) {
                throw new FrameworkException(e);
            } catch (IllegalAccessException e) {
                throw new FrameworkException(e);
            }
        }
        Annotation namesAnnotation = AnnotationTools
                .getAnnotation(paramInfo
                                .getParameter()
                                .getDeclaringExecutable(),
                        Names.class);
        if(namesAnnotation!=null){
            try {
                Method nameMethod = namesAnnotation
                        .annotationType()
                        .getMethod("names", null);
                String[] names = (String[]) nameMethod.invoke(namesAnnotation, null);
                Executable executable = paramInfo.getParameter().getDeclaringExecutable();
                List<Parameter> validParams = Arrays
                        .stream(executable.getParameters())
                        .filter(parameter1 -> !controllerConfig.isInjectableParam(parameter1.getType())) // TODO pay attention here
                        .collect(Collectors.toList());
                List<String> validParamsTypes = validParams
                        .stream()
                        .map(parameter1 -> parameter1.getType().getName())
                        .collect(Collectors.toList());
                if(names==null)
                    throw new FrameworkException("Names annotation on ["+paramInfo
                            .getParameter()
                            .getDeclaringExecutable()+"] can't be empty ["+namesAnnotation+"]");
                if (names.length>validParams.size())
                    logger.error("Number of names greater than valid parameters " +
                            "\nNames annotation on ["+executable+"] not properly configured, " +
                            "\nNames ["+namesAnnotation+"] " +
                            "\nValid params: ["+validParamsTypes+"]");
                if(names.length<validParams.size())
                    throw new FrameworkException("Number of names less than number of valid parameters " +
                            "\nNames annotation on ["+executable+"] doesn't contain all params," +
                            "\nNames ["+namesAnnotation+"] " +
                            "\nValid params: ["+validParamsTypes+"]");
                int paramIndex = validParams.indexOf(paramInfo.getParameter());
                if(paramIndex>-1 && !names[paramIndex].trim().isEmpty()){
                    paramInfo.setUsedName(names[paramIndex]);
                }else if(paramIndex>-1 ){
                    throw new FrameworkException("Name can't be empty on param ["+paramInfo.getParameter()+"] " +
                            "\nNames annotation on ["+executable+"] is invalid, " +
                            "\nNames: ["+namesAnnotation+"] ");
                }else{
                    throw new FrameworkException("Can't found parameter: ["+paramInfo.getParameter()+"] " +
                            "\nNames annotation on ["+executable+"] is invalid, " +
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
    }
}
