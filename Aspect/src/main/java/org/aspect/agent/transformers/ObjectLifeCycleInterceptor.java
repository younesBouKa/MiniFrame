package org.aspect.agent.transformers;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class ObjectLifeCycleInterceptor extends CustomTransformer {
    private static final Logger logger = LogManager.getLogger(ObjectLifeCycleInterceptor.class);

    private String beforeConstructorInjectedCode(String normalizedClassName){
        StringBuilder code = new StringBuilder();
        code.append("System.out.println(\"From constructor: "+normalizedClassName+" \");");
        return code.toString();
    }
    private String afterConstructorInjectedCode(String normalizedClassName){
        StringBuilder code = new StringBuilder();
        code.append("try{" +
                "   int hashCode = hashCode();" +
                "   logger.debug(\" hash code: \"+hashCode);" +
                "}catch(Exception e){" +
                "   e.getMessage();" +
                "}finally{" +
                "   System.out.println(\"\");" +
                "}");
        return code.toString();
    }

    private String beforeMethodInjectedCode(String normalizedClassName, CtMethod method){
        StringBuilder code = new StringBuilder();
        String methodName = method.getName();
        code.append("System.out.println(\"Start calling method: "+normalizedClassName+":"+methodName+"\");");
        return code.toString();
    }
    private String afterMethodInjectedCode(String normalizedClassName, CtMethod method){
        StringBuilder code = new StringBuilder();
        String methodName = method.getName();
        code.append("System.out.println(\"End calling method: "+normalizedClassName+":"+methodName+"\");");
        return code.toString();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        try {
            String normalizedClassName = normalizeClassName(className);
            if(toExclude(normalizedClassName)){
                return byteCode;
            }
            //logger.debug("ObjectLifeCycleInterceptor class: "+normalizedClassName);
            try {
                ClassPool classPool = ClassPool.getDefault();
                CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                // send creation event (calling constructor)
                for(CtConstructor constructor: ctClass.getConstructors()){
                    logger.debug("ObjectLifeCycleInterceptor constructor: "+normalizedClassName);
                    constructor.insertBeforeBody(beforeConstructorInjectedCode(normalizedClassName));
                    constructor.insertAfter(afterConstructorInjectedCode(normalizedClassName));
                }
                // send finalize event (calling finalize)
                for(CtMethod method: ctClass.getDeclaredMethods("finalize")){
                    logger.debug("ObjectLifeCycleInterceptor finalize: "+normalizedClassName);
                    method.insertBefore(beforeMethodInjectedCode(normalizedClassName, method));
                    method.insertAfter(afterMethodInjectedCode(normalizedClassName, method));
                }
                // getting bytes
                byteCode = ctClass.toBytecode();
                ctClass.detach();
                //processedClasses.add(normalizedClassName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
        return byteCode;
    }
}
