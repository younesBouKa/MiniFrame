package org.web.data;

import org.web.annotations.params.global.ParamSrc;

import java.lang.reflect.Parameter;

public class ParamInfo {
    private Parameter parameter;
    private String usedName;
    private ParamSrc paramSrc;
    private String name;
    private Class type;

    public ParamInfo() {
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getUsedName() {
        return usedName;
    }

    public void setUsedName(String usedName) {
        this.usedName = usedName;
    }

    public ParamSrc getParamType() {
        return paramSrc;
    }

    public void setParamType(ParamSrc paramSrc) {
        this.paramSrc = paramSrc;
    }

    @Override
    public String toString() {
        return "MethodParamInfo{" +
                "name='" + name +
                ", type=" + type +
                ", usedName=" + usedName +
                ", paramType=" + paramSrc +
                ", parameter=" + parameter +
                '}';
    }
}
