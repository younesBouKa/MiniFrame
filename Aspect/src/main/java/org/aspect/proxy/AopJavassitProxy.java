package org.aspect.proxy;

import javassist.*;
import org.tools.ClassFinder;
import org.tools.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;

public class AopJavassitProxy<T> {
    private static final Log logger = Log.getInstance(AopJavassitProxy.class);

    public static Object newInstance(Object instance) {
        Class<?> instanceClass = instance.getClass();
        wrapClassMethods(instanceClass.getCanonicalName());
        return instance;
    }

    public static CtClass weaveClassMethods(CtClass clazz) {
        String weavedClassesDir = "C:\\Users\\younes.boukanoucha\\IdeaProjects\\MiniFrame\\Demo\\target\\classes";
        String className = clazz.getName();
        ClassPool pool = ClassPool.getDefault();
        //pool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        CtField weavedField = null;
        try {
            weavedField = clazz.getField("weaved");
        } catch (NotFoundException notFoundException) {
            logger.info("class "+className+" not yet weaved");
        }
        if (weavedField != null){
            logger.info("class "+className+" already weaved");
            return clazz;
        }
        CtClass ctJointPointClass = null;
        try {
            ctJointPointClass = pool.getCtClass(JoinPoint.class.getCanonicalName());
        }catch (NotFoundException notFoundException){
            logger.error(notFoundException);
            return clazz;
        }
        CtClass ctThrowableClass = null;
        try {
            ctThrowableClass = pool.getCtClass(Throwable.class.getCanonicalName());
        }catch (NotFoundException notFoundException){
            logger.error(notFoundException);
            return clazz;
        }
        CtClass ctObjectClass = null;
        try {
            ctObjectClass = pool.getCtClass(Object.class.getCanonicalName());
        }catch (NotFoundException notFoundException){
            logger.error(notFoundException);
            return clazz;
        }
        // add joinPoint field
       /* CtField jointPointField = null;
        try {
            jointPointField = clazz.getField("joinPoint");
        } catch (NotFoundException notFoundException) {
            logger.info("jointPointField not yet created");
        }
        if(jointPointField==null){
            try {
                jointPointField = new CtField(ctJointPointClass, "joinPoint", clazz);
                clazz.addField(jointPointField, "null");
                //jointPointField = CtField.make("private org.aspect.proxy.JoinPoint joinPoint = null;", ctJointPointClass);
                //clazz.addField(jointPointField);
            } catch (CannotCompileException e) {
                logger.error(e);
                return;
            }
        }*/

        for (CtMethod ctMethod : clazz.getMethods()) {
            if(ctMethod.isEmpty())
                continue;
            try {
                ctMethod.insertBefore("System.out.println(\"method weaved\");");
            }catch (CannotCompileException cannotCompileException){
                logger.error("Can't insert in method: "+ctMethod.getLongName());
                continue;
            }
            String beforeStrSrc = "" +
                    " org.aspect.proxy.JoinPoint joinPoint = new org.aspect.proxy.JoinPoint();" +
                    " joinPoint.setTargetMethod(\"" + ctMethod.getLongName() + "\");" +
                    " joinPoint.setTargetClass(\"" + className + "\");" +
                    " joinPoint.setArgs($args);" +
                    " org.aspect.processor.ProxyEventHandler.execBeforeCall(joinPoint);" +
                    "";
            String afterStrSrc =
                    " org.aspect.proxy.JoinPoint joinPoint = new org.aspect.proxy.JoinPoint();" +
                            " joinPoint.setTargetMethod(\"" + ctMethod.getLongName() + "\");" +
                            " joinPoint.setTargetClass(\"" + className + "\");" +
                            " joinPoint.setArgs($args);" +
                            " joinPoint.setReturnVal(Object.class.cast($_));" + // TODO to see cast later
                            " $_ = ($r)org.aspect.processor.ProxyEventHandler.execBeforeReturn(joinPoint);" +
                            " joinPoint.setReturnVal(Object.class.cast($_));" +
                            " org.aspect.processor.ProxyEventHandler.execAfterCall(joinPoint);";

            try {
                ctMethod.insertBefore(beforeStrSrc);
            }catch (CannotCompileException cannotCompileException) {
                logger.error(cannotCompileException);
                continue;
            }
            try {
                ctMethod.insertAfter(afterStrSrc);
            }catch (CannotCompileException cannotCompileException) {
                logger.error(cannotCompileException);
            }

            String throwableSrc = "{" +
                    " org.aspect.proxy.JoinPoint joinPoint = new org.aspect.proxy.JoinPoint();" +
                    " joinPoint.setTargetMethod(\"" + ctMethod.getLongName() + "\");" +
                    " joinPoint.setTargetClass(\"" + className + "\");" +
                    " joinPoint.setArgs($args);" +
                    "joinPoint.setThrowable($e);" +
                    "org.aspect.processor.ProxyEventHandler.execOnException(joinPoint);" +
                    "throw $e;" +
                    "}";
            try {
                ctMethod.addCatch(throwableSrc, ctThrowableClass);
            }catch (CannotCompileException cannotCompileException) {
                logger.error(cannotCompileException);
            }
            //logger.info("Method "+ctMethod.getLongName()+" weaved successfully");
        }
        // add weaved flag
        try {
            weavedField = CtField.make("private static boolean weaved = true;", clazz);
            clazz.addField(weavedField);
        } catch (CannotCompileException e) {
            logger.error(e);
        }

        try {
            clazz.writeFile(weavedClassesDir);
        } catch (CannotCompileException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        }
        DirClassPath weavedDirClassPath = new DirClassPath(weavedClassesDir);
        pool.appendClassPath(weavedDirClassPath);
        ClassFinder.addToClassPath(Collections.singleton(weavedClassesDir));

        logger.info("Class "+clazz.getName()+" weaved successfully");
        return clazz;
    }

    public static byte[] weaveClassMethods(String className) {
        String weavedClassesDir = "C:\\Users\\younes.boukanoucha\\IdeaProjects\\MiniFrame\\Demo\\target\\classes";
        ClassPool.doPruning = true;
        ClassPool pool = ClassPool.getDefault();
        //pool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        CtClass clazz = null;
        byte[] originalClassBytes = new byte[0];
        try {
            clazz = pool.getCtClass(className);
        } catch (NotFoundException notFoundException) {
            logger.error(notFoundException);
            return originalClassBytes;
        }
        try {
            originalClassBytes = clazz.toBytecode();
        } catch (IOException | CannotCompileException e) {
            logger.error(e);
            return originalClassBytes;
        }
        CtField weavedField = null;
        try {
            weavedField = clazz.getField("weaved");
        } catch (NotFoundException notFoundException) {
            logger.info("class "+className+" not yet weaved");
        }
        if (weavedField != null){
            logger.info("class "+className+" already weaved");
            return originalClassBytes;
        }
        CtClass ctJointPointClass = null;
        try {
            ctJointPointClass = pool.getCtClass(JoinPoint.class.getCanonicalName());
        }catch (NotFoundException notFoundException){
            logger.error(notFoundException);
            return originalClassBytes;
        }
        CtClass ctThrowableClass = null;
        try {
            ctThrowableClass = pool.getCtClass(Throwable.class.getCanonicalName());
        }catch (NotFoundException notFoundException){
            logger.error(notFoundException);
            return originalClassBytes;
        }
        CtClass ctObjectClass = null;
        try {
            ctObjectClass = pool.getCtClass(Object.class.getCanonicalName());
        }catch (NotFoundException notFoundException){
            logger.error(notFoundException);
            return originalClassBytes;
        }
        // add joinPoint field
       /* CtField jointPointField = null;
        try {
            jointPointField = clazz.getField("joinPoint");
        } catch (NotFoundException notFoundException) {
            logger.info("jointPointField not yet created");
        }
        if(jointPointField==null){
            try {
                jointPointField = new CtField(ctJointPointClass, "joinPoint", clazz);
                clazz.addField(jointPointField, "null");
                //jointPointField = CtField.make("private org.aspect.proxy.JoinPoint joinPoint = null;", ctJointPointClass);
                //clazz.addField(jointPointField);
            } catch (CannotCompileException e) {
                logger.error(e);
                return;
            }
        }*/

        for (CtMethod ctMethod : clazz.getMethods()) {
            if(ctMethod.isEmpty())
                continue;
            try {
                ctMethod.insertBefore("System.out.println(\"method weaved\");");
            }catch (CannotCompileException cannotCompileException){
                logger.error("Can't insert in method: "+ctMethod.getLongName());
                continue;
            }
            String beforeStrSrc = "" +
                    " org.aspect.proxy.JoinPoint joinPoint = new org.aspect.proxy.JoinPoint();" +
                    " joinPoint.setTargetMethod(\"" + ctMethod.getLongName() + "\");" +
                    " joinPoint.setTargetClass(\"" + className + "\");" +
                    " joinPoint.setArgs($args);" +
                    " org.aspect.processor.ProxyEventHandler.execBeforeCall(joinPoint);" +
                    "";
            String afterStrSrc =
                    " org.aspect.proxy.JoinPoint joinPoint = new org.aspect.proxy.JoinPoint();" +
                            " joinPoint.setTargetMethod(\"" + ctMethod.getLongName() + "\");" +
                            " joinPoint.setTargetClass(\"" + className + "\");" +
                            " joinPoint.setArgs($args);" +
                            " joinPoint.setReturnVal(Object.class.cast($_));" + // TODO to see cast later
                            " $_ = ($r)org.aspect.processor.ProxyEventHandler.execBeforeReturn(joinPoint);" +
                            " joinPoint.setReturnVal(Object.class.cast($_));" +
                            " org.aspect.processor.ProxyEventHandler.execAfterCall(joinPoint);";

            try {
                ctMethod.insertBefore(beforeStrSrc);
            }catch (CannotCompileException cannotCompileException) {
                logger.error(cannotCompileException);
                continue;
            }
            try {
                ctMethod.insertAfter(afterStrSrc);
            }catch (CannotCompileException cannotCompileException) {
                logger.error(cannotCompileException);
            }

            String throwableSrc = "{" +
                    " org.aspect.proxy.JoinPoint joinPoint = new org.aspect.proxy.JoinPoint();" +
                    " joinPoint.setTargetMethod(\"" + ctMethod.getLongName() + "\");" +
                    " joinPoint.setTargetClass(\"" + className + "\");" +
                    " joinPoint.setArgs($args);" +
                    "joinPoint.setThrowable($e);" +
                    "org.aspect.processor.ProxyEventHandler.execOnException(joinPoint);" +
                    "throw $e;" +
                    "}";
            try {
                ctMethod.addCatch(throwableSrc, ctThrowableClass);
            }catch (CannotCompileException cannotCompileException) {
                logger.error(cannotCompileException);
            }
            //logger.info("Method "+ctMethod.getLongName()+" weaved successfully");
        }
        // add weaved flag
        try {
            weavedField = CtField.make("private static boolean weaved = true;", clazz);
            clazz.addField(weavedField);
        } catch (CannotCompileException e) {
            logger.error(e);
        }

        try {
            clazz.writeFile(weavedClassesDir);
        } catch (CannotCompileException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        }
        DirClassPath weavedDirClassPath = new DirClassPath(weavedClassesDir);
        pool.appendClassPath(weavedDirClassPath);
        ClassFinder.addToClassPath(Collections.singleton(weavedClassesDir));

        logger.info("Class "+clazz.getName()+" weaved successfully");
        try {
            return clazz.toBytecode();
        } catch (IOException e) {
            logger.error(e);
        } catch (CannotCompileException e) {
            logger.error(e);
        }
        return originalClassBytes;
    }

    public static void wrapClassMethods(String className) {
        String weavedClassesDir = "C:\\Users\\younes.boukanoucha\\IdeaProjects\\MiniFrame\\Demo\\target\\classes";
        ClassPool.doPruning = true;
        ClassPool pool = ClassPool.getDefault();
        //pool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        CtClass clazz = null;
        try {
            clazz = pool.getCtClass(className);
        } catch (NotFoundException notFoundException) {
            logger.error(notFoundException);
            return;
        }
        CtField weavedField = null;
        try {
            weavedField = clazz.getField("weaved");
        } catch (NotFoundException notFoundException) {
            logger.info("class "+className+" not yet weaved");
        }
        if (weavedField != null){
            logger.info("class "+className+" already weaved");
            return;
        }
        CtClass ctJointPointClass = null;
        try {
            ctJointPointClass = pool.getCtClass(JoinPoint.class.getCanonicalName());
        }catch (NotFoundException notFoundException){
            logger.error(notFoundException);
            return;
        }
        CtClass ctThrowableClass = null;
        try {
            ctThrowableClass = pool.getCtClass(Throwable.class.getCanonicalName());
        }catch (NotFoundException notFoundException){
            logger.error(notFoundException);
            return;
        }
        CtClass ctObjectClass = null;
        try {
            ctObjectClass = pool.getCtClass(Object.class.getCanonicalName());
        }catch (NotFoundException notFoundException){
            logger.error(notFoundException);
            return;
        }
        // add joinPoint field
       /* CtField jointPointField = null;
        try {
            jointPointField = clazz.getField("joinPoint");
        } catch (NotFoundException notFoundException) {
            logger.info("jointPointField not yet created");
        }
        if(jointPointField==null){
            try {
                jointPointField = new CtField(ctJointPointClass, "joinPoint", clazz);
                clazz.addField(jointPointField, "null");
                //jointPointField = CtField.make("private org.aspect.proxy.JoinPoint joinPoint = null;", ctJointPointClass);
                //clazz.addField(jointPointField);
            } catch (CannotCompileException e) {
                logger.error(e);
                return;
            }
        }*/

        for (CtMethod ctMethod : clazz.getMethods()) {
            if(ctMethod.isEmpty())
                continue;
            try {
                ctMethod.insertBefore("System.out.println(\"method weaved\");");
            }catch (CannotCompileException cannotCompileException){
                logger.error("Can't insert in method: "+ctMethod.getLongName());
                continue;
            }
            String beforeStrSrc = "" +
                    " org.aspect.proxy.JoinPoint joinPoint = new org.aspect.proxy.JoinPoint();" +
                    " joinPoint.setTargetMethod(\"" + ctMethod.getLongName() + "\");" +
                    " joinPoint.setTargetClass(\"" + className + "\");" +
                    " joinPoint.setArgs($args);" +
                    " org.aspect.processor.ProxyEventHandler.execBeforeCall(joinPoint);" +
                    "";
            String afterStrSrc =
                    " org.aspect.proxy.JoinPoint joinPoint = new org.aspect.proxy.JoinPoint();" +
                    " joinPoint.setTargetMethod(\"" + ctMethod.getLongName() + "\");" +
                    " joinPoint.setTargetClass(\"" + className + "\");" +
                    " joinPoint.setArgs($args);" +
                    " joinPoint.setReturnVal(Object.class.cast($_));" + // TODO to see cast later
                    " $_ = ($r)org.aspect.processor.ProxyEventHandler.execBeforeReturn(joinPoint);" +
                    " joinPoint.setReturnVal(Object.class.cast($_));" +
                    " org.aspect.processor.ProxyEventHandler.execAfterCall(joinPoint);";

            try {
                ctMethod.insertBefore(beforeStrSrc);
            }catch (CannotCompileException cannotCompileException) {
                logger.error(cannotCompileException);
                continue;
            }
            try {
                ctMethod.insertAfter(afterStrSrc);
            }catch (CannotCompileException cannotCompileException) {
                logger.error(cannotCompileException);
            }

            String throwableSrc = "{" +
                    " org.aspect.proxy.JoinPoint joinPoint = new org.aspect.proxy.JoinPoint();" +
                    " joinPoint.setTargetMethod(\"" + ctMethod.getLongName() + "\");" +
                    " joinPoint.setTargetClass(\"" + className + "\");" +
                    " joinPoint.setArgs($args);" +
                    "joinPoint.setThrowable($e);" +
                    "org.aspect.processor.ProxyEventHandler.execOnException(joinPoint);" +
                    "throw $e;" +
                    "}";
            try {
                ctMethod.addCatch(throwableSrc, ctThrowableClass);
            }catch (CannotCompileException cannotCompileException) {
                logger.error(cannotCompileException);
            }
            //logger.info("Method "+ctMethod.getLongName()+" weaved successfully");
        }
        // add weaved flag
        try {
            weavedField = CtField.make("private static boolean weaved = true;", clazz);
            clazz.addField(weavedField);
        } catch (CannotCompileException e) {
            logger.error(e);
        }

        try {
            clazz.writeFile(weavedClassesDir);
        } catch (CannotCompileException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        }
        DirClassPath weavedDirClassPath = new DirClassPath(weavedClassesDir);
        pool.appendClassPath(weavedDirClassPath);
        ClassFinder.addToClassPath(Collections.singleton(weavedClassesDir));

        logger.info("Class "+clazz.getName()+" weaved successfully");
    }

    private static void overrideGetter(CtClass clazz, Method getter, Method setter)
            throws CannotCompileException {
        String targetService = getter.getReturnType().getCanonicalName();
        CtMethod newMethod = CtNewMethod.make(
                "public " + targetService + " " + getter.getName() + "() { \n" +
                        "" +
                        "    " + targetService + " retObject =  super." + getter.getName() + "(); "+
                        "    if (retObject == null) {" +
                        "         retObject =  (" + targetService + ") \n" +
                        "                 getRegistry().getService(\"" + targetService + "\"); " +
                        "         super." + setter.getName() + "(retObject);" +
                        "    }" +
                        "    return retObject;" +
                        "" +
                        "}",
                clazz);
        clazz.addMethod(newMethod);
    }

    private static void overrideMethod(CtClass clazz, Method method)
            throws CannotCompileException {
        String targetService = method.getReturnType().getCanonicalName();
        CtMethod newMethod = CtNewMethod.make(
                "public " + targetService + " " + method.getName() + "() { \n" +
                        "" +
                        "    " + targetService + " retObject =  \n" +
                        "            (" + targetService + ")\n" +
                        "            getRegistry().getService(\"" + targetService + "\"); " +
                        "    return retObject;" +
                        "" +
                        "}",
                clazz);
        clazz.addMethod(newMethod);
    }
}