package org.injection.proxy;

import java.lang.reflect.Method;

public interface BeforeCallFunction {
    void apply(Object proxy, Method method, Object[] args);
}