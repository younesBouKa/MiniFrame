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
import org.aspect.proxy.JoinPoint;
import org.demo.services.UserService;
import org.injection.annotations.Component;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;

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
    public void hashCodeAdvice(JoinPoint joinPoint){
        logger.info("BeforeCall : hashCodeAdvice : "+joinPoint.getTargetMethod());
    }

    @After
    @TargetClass(target = UserService.class)
    @Call
    public void loginAdvice(JoinPoint joinPoint){
        logger.info("AfterCall : loginAdvice : "+joinPoint.getTargetMethod()+" : "+joinPoint.getReturnVal());
    }

    @BeforeReturn
    @AnnotatedWith(annotationClass = Component.class)
    @Call
    public void beforeReturnLoginAdvice(JoinPoint joinPoint){
        logger.info("BeforeReturn : beforeReturnLoginAdvice : "+joinPoint.getTargetMethod()+" : "+joinPoint.getReturnVal());
    }


    @BeforeReturn
    @AnnotatedWith(annotationClass = Component.class)
    @Order(order = 10)
    @Call
    public void beforeReturnLoginOtherAdvice(JoinPoint joinPoint){
        logger.info("BeforeReturn : beforeReturnLoginOtherAdvice : "+joinPoint.getTargetMethod()+" : "+ joinPoint.getReturnVal());
    }

    @Expression(regex= "(.)*")
    @Exception(types = {NullPointerException.class, ArithmeticException.class, FrameworkException.class})
    public void onExceptionAdvice(JoinPoint joinPoint){
        logger.info("OnException : onExceptionAdvice : "+joinPoint.getTargetMethod()+" : "+ joinPoint.getThrowable());
    }

    @Before
    @Call
    @Expression(regex = "(.*)titi(.*)")
    public void beforeCallTitiAdvice(JoinPoint joinPoint){
        logger.info("BeforeCall : beforeCallTitiAdvice : "+joinPoint.getTargetMethod());
    }

    @TargetClass(target= UserService.class)
    @Around
    @Call
    public void targetClassAdvice(JoinPoint joinPoint){
        logger.info("TargetClass : targetClassAdvice : "+joinPoint.getTargetMethod());
    }
}
