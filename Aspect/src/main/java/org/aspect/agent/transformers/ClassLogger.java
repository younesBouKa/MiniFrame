package org.aspect.agent.transformers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tools.ClassFinder;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class ClassLogger extends CustomTransformer {
    private static final Logger logger = LogManager.getLogger(ClassLogger.class);
    public ClassLogger(){
        logger.info("ClassLogger Transformer initialized");
    }

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            String normalizedClassName = normalizeClassName(className);
            if(toExclude(normalizedClassName)){
                return classfileBuffer;
            }
            Class currentClass = ClassFinder.loadClass(normalizedClassName);
            logger.debug("ClassLogger: className= "+className+", currentClass= "+currentClass);
            processedClasses.add(normalizedClassName);
        } catch (Throwable error) {
            logger.error("Instrumentation Error: ",error);
        }
        return classfileBuffer;
    }
}
