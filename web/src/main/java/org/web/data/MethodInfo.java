package org.web.data;

import java.util.Set;

public class MethodInfo {
    private String name;
    private String path;
    private String httpMethod;
    private Set<ParamInfo> paramInfoSet;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Set<ParamInfo> getParamInfoSet() {
        return paramInfoSet;
    }

    public void setParamInfoSet(Set<ParamInfo> paramInfoSet) {
        this.paramInfoSet = paramInfoSet;
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", paramInfoSet=" + paramInfoSet +
                '}';
    }
}
