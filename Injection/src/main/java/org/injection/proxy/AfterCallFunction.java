package org.injection.proxy;

import java.lang.reflect.Method;

public interface AfterCallFunction {
    void apply(Object proxy, Method method, Object[] args, Object returnVal);
}