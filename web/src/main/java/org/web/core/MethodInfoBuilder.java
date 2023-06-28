package org.web.core;

import org.web.annotations.params.global.ParamSrc;
import org.web.data.MethodInfo;
import org.web.data.ParamInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

public interface MethodInfoBuilder extends AutoConfigurable {

    MethodInfo build(Method method);
    Set<ParamInfo> prepareParamInfo(Method method);
    String getParamNameFromMethodAnnotation(Method method , Parameter parameter);
    ParamSrc getParamSourceFromMethodAnnotation(Method method , Parameter parameter);

}
