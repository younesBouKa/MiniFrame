package org.injection.dependency;

import org.injection.enums.DependencyType;

public class Dependency {
    private Class firstDependency;
    private DependencyType dependencyType;
    private Class secondDependency;

    public Dependency(Class firstDependency, DependencyType dependencyType, Class secondDependency) {
        this.firstDependency = firstDependency;
        this.dependencyType = dependencyType;
        this.secondDependency = secondDependency;
    }

    public Class getFirstDependency() {
        return firstDependency;
    }

    public void setFirstDependency(Class firstDependency) {
        this.firstDependency = firstDependency;
    }

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public Class getSecondDependency() {
        return secondDependency;
    }

    public void setSecondDependency(Class secondDependency) {
        this.secondDependency = secondDependency;
    }

    @Override
    public String toString() {
        return dependencyType
                .format(
                        firstDependency.getCanonicalName(),
                        secondDependency.getCanonicalName()
                );
    }
}
