package org.demo.aspects;

import org.aspect.annotations.Aspect;
import org.aspect.annotations.InitAspect;
import org.aspect.annotations.Order;
import org.aspect.annotations.pointcuts.AnnotatedWith;
import org.aspect.annotations.pointcuts.Expression;
import org.aspect.annotations.pointcuts.TargetClass;
import org.aspect.annotations.positions.After;
import org.aspect.annotations.positions.Around;
import org.aspect.annotations.positions.Before;
import org.aspect.annotations.positions.BeforeReturn;
import org.aspect.annotations.types.Call;
import org.aspect.annotations.types.Exception;
import org.demo.services.UserService;
import org.injection.annotations.Component;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;

import java.lang.reflect.Method;

@Aspect
public class UserServiceAspect {
    private static final Log logger = Log.getInstance(UserServiceAspect.class);

    @InitAspect
    private void init(){
        logger.info("Init aspect instance");
    }

    @Before
    @Expression(regex="(.*)hashCode(.*)")
    @Call
    public void hashCodeAdvice(Method method, Object[] args, Object targetInstance, Object returnValue){
        logger.info("BeforeCall : hashCodeAdvice : "+method.getName());
    }

    @After
    @TargetClass(target = UserService.class)
    @Call
    public void loginAdvice(Method method, Object[] args, Object targetInstance, Object returnValue){
        logger.info("AfterCall : loginAdvice : "+method.getName()+" : "+returnValue);
    }

    @BeforeReturn
    @AnnotatedWith(annotationClass = Component.class)
    @Call
    public void beforeReturnLoginAdvice(Method method, Object[] args, Object targetInstance, Object returnValue){
        logger.info("BeforeReturn : beforeReturnLoginAdvice : "+method.getName()+" : "+returnValue);
    }


    @BeforeReturn
    @AnnotatedWith(annotationClass = Component.class)
    @Order(order = 10)
    @Call
    public void beforeReturnLoginOtherAdvice(Method method, Object[] args, Object targetInstance, Object returnValue){
        logger.info("BeforeReturn : beforeReturnLoginOtherAdvice : "+method.getName()+" : "+ returnValue);
    }

    @Expression(regex= "(.)*")
    @Exception(types = {NullPointerException.class, ArithmeticException.class, FrameworkException.class})
    public void onExceptionAdvice(Method method, Object[] args, Object targetInstance, Throwable throwable){
        logger.info("OnException : onExceptionAdvice : "+method.getName()+" : "+ throwable);
    }

    @Before
    @Call
    @Expression(regex = "(.*)titi(.*)")
    public void beforeCallTitiAdvice(Method method, Object[] args, Object targetInstance, Object returnValue){
        logger.info("BeforeCall : beforeCallTitiAdvice : "+method.getName()+" : "+ targetInstance);
    }

    @TargetClass(target= UserService.class)
    @Around
    @Call
    public void targetClassAdvice(Method method, Object[] args, Object targetInstance, Object returnValue){
        logger.info("TargetClass : targetClassAdvice : "+method.getName()+" : "+ targetInstance);
    }
}
