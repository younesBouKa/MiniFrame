package org.aspect.proxy;

import org.aspect.annotations.Advice;

public class JoinPoint {
    private String targetClass;
    private String targetMethod;
    private Object[] args;
    private Object returnVal;
    private Throwable throwable;
    private Class<?> adviceClass;
    private String adviceMethod;
    private Advice advice;


    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object getReturnVal() {
        return returnVal;
    }

    public void setReturnVal(Object returnVal) {
        this.returnVal = returnVal;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public Advice getAdvice() {
        return advice;
    }

    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public Class<?> getAdviceClass() {
        return adviceClass;
    }

    public void setAdviceClass(Class<?> adviceClass) {
        this.adviceClass = adviceClass;
    }

    public String getAdviceMethod() {
        return adviceMethod;
    }

    public void setAdviceMethod(String adviceMethod) {
        this.adviceMethod = adviceMethod;
    }
}
