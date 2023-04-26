package org.agent.transformers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class ClassLogger extends CustomTransformer {
    private static final Logger logger = LogManager.getLogger(ClassLogger.class);
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
            logger.debug("ClassLogger: "+className);
            processedClasses.add(normalizedClassName);
        } catch (Throwable error) {
            logger.error(error);
        } finally {
            return classfileBuffer;
        }
    }
}
