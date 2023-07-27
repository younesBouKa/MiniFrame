package org.web.core.method;

import org.web.annotations.params.global.ParamSrc;
import org.web.core.AutoConfigurable;
import org.web.data.ParamInfo;

import java.lang.reflect.Parameter;

public interface ParamInfoBuilder extends AutoConfigurable {
    ParamInfo build(Parameter parameter, String usedName, ParamSrc source);
    ParamInfo build(Parameter parameter);
    void parseForParamNameAndType(ParamInfo paramInfo);
    void parseForParamNameAndTypeFromExecutable(ParamInfo paramInfo);
}