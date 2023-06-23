package org.aspect.agent.transformers;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspect.proxy.AopJavassitProxy;
import org.aspect.proxy.Wrapper;
import org.aspect.scanners.AspectScanManager;

import java.io.ByteArrayInputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

public class AspectWeaverInterceptor extends CustomTransformer {
    private static final Logger logger = LogManager.getLogger(AspectWeaverInterceptor.class);

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        try {
            String normalizedClassName = normalizeClassName(className);
            if(toExclude(normalizedClassName)){
                return byteCode;
            }
            try {
                ClassPool classPool = ClassPool.getDefault();
                CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                ctClass = AopJavassitProxy.weaveClassMethods(ctClass);
                // getting bytes
                byteCode = ctClass.toBytecode();
                ctClass.detach();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
        return byteCode;
    }
}
