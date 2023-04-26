package org.injection.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyHandler<T> implements InvocationHandler {

    private final BeforeCallFunction beforeCall;
    private final AfterCallFunction afterCall;

    public ProxyHandler(BeforeCallFunction beforeCall,
                        AfterCallFunction afterCall) {
        this.beforeCall = beforeCall;
        this.afterCall = afterCall;
    }

    @Override
    public Object invoke(Object instance, Method method, Object[] args) throws Throwable {
        if(beforeCall!=null){
            try {
                beforeCall.apply(instance, method, args);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        Object returnedValue = method.invoke(instance, args);
        if(afterCall!=null){
            try {
                afterCall.apply(instance, method, args, returnedValue);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return returnedValue;
    }
}