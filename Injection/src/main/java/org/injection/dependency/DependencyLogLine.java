package org.injection.dependency;

import org.injection.enums.ResolutionStatus;

public class DependencyLogLine{
    private Dependency dependency;
    private ResolutionStatus resolutionStatus;
    private Object fromWhere;

    public DependencyLogLine(Dependency dependency, ResolutionStatus resolutionStatus, Object fromWhere) {
        this.dependency = dependency;
        this.resolutionStatus = resolutionStatus;
        this.fromWhere = fromWhere;
    }

    public Dependency getDependency() {
        return dependency;
    }

    public void setDependency(Dependency dependency) {
        this.dependency = dependency;
    }

    public ResolutionStatus getResolutionStatus() {
        return resolutionStatus;
    }

    public void setResolutionStatus(ResolutionStatus resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    @Override
    public String toString() {
        String msg = dependency+" => "+resolutionStatus;
        if(fromWhere!=null){
            msg+= " "+fromWhere;
        }
        return msg;
    }

    public Object getFromWhere() {
        return fromWhere;
    }

    public void setFromWhere(Object fromWhere) {
        this.fromWhere = fromWhere;
    }
}
