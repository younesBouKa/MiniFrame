package org.demo.aspects;

import org.aspect.annotations.Aspect;
import org.aspect.annotations.InitAspect;
import org.aspect.annotations.advices.*;
import org.demo.services.UserService;
import org.tools.Log;

import java.lang.reflect.Method;

@Aspect
public class UserServiceAspect {
    private static final Log logger = Log.getInstance(UserServiceAspect.class);

    @InitAspect
    private void init(){
        logger.info("Init aspect instance");
    }

    @BeforeCall(methodSignature = "(.*)hashCode(.*)")
    public void hashCodeAdvice(Method method, Object[] args, Object targetInstance){
        logger.info("BeforeCall : hashCodeAdvice : "+method.getName());
    }

    @AfterCall(methodSignature = "(.*)login(.*)")
    public void loginAdvice(Method method, Object[] args, Object targetInstance, Object returnValue){
        logger.info("AfterCall : loginAdvice : "+method.getName()+" : "+returnValue);
    }

    @BeforeReturn(methodSignature = "(.*)login(.*)")
    public void beforeReturnLoginAdvice(Method method, Object[] args, Object targetInstance, Object returnValue){
        logger.info("BeforeReturn : beforeReturnLoginAdvice : "+method.getName()+" : "+returnValue);
    }

    @BeforeReturn(methodSignature = "(.*)login(.*)", order = 3)
    public void beforeReturnLoginOtherAdvice(Method method, Object[] args, Object targetInstance, Object returnValue){
        logger.info("BeforeReturn : beforeReturnLoginOtherAdvice : "+method.getName()+" : "+ returnValue);
    }


    @OnException(exception = Throwable.class)
    public void onExceptionAdvice(Method method, Object[] args, Object targetInstance, Throwable throwable){
        logger.info("OnException : onExceptionAdvice : "+method.getName()+" : "+ throwable);
    }

    @BeforeCall(methodSignature = "(.*)titi(.*)")
    public void beforeCallTitiAdvice(Method method, Object[] args, Object targetInstance){
        logger.info("BeforeCall : beforeCallTitiAdvice : "+method.getName()+" : "+ targetInstance);
    }

    @TargetClass(target= UserService.class)
    public void targetClassAdvice(Method method, Object[] args, Object targetInstance){
        logger.info("TargetClass : targetClassAdvice : "+method.getName()+" : "+ targetInstance);
    }
}
