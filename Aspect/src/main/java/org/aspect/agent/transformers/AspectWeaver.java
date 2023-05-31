package org.aspect.agent.transformers;

import javassist.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AspectWeaver {
    private static final Logger logger = LogManager.getLogger(AspectWeaver.class);

    public void insertTimingIntoMethod(String targetClass, String targetMethod) throws NotFoundException, CannotCompileException, IOException {
        final String targetFolder = "./target/javassist";
        try {
            final ClassPool pool = ClassPool.getDefault();
            // Tell Javassist where to look for classes - into our ClassLoader
            pool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()));
            final CtClass compiledClass = pool.get(targetClass);
            final CtMethod method = compiledClass.getDeclaredMethod(targetMethod);

            // Add something to the beginning of the method:
            method.addLocalVariable("startMs", CtClass.longType);
            method.insertBefore("startMs = System.currentTimeMillis();");
            // And also to its very end:
            method.insertAfter("{final long endMs = System.currentTimeMillis();" +
                    "iterate.jz2011.codeinjection.javassist.PerformanceMonitor.logPerformance(\"" +
                    targetMethod + "\",(endMs-startMs));}");

            compiledClass.writeFile(targetFolder);
            // Enjoy the new $targetFolder/iterate/jz2011/codeinjection/javassist/TargetClass.class

            logger.info(method.getGenericSignature() + " has been modified and saved under " + targetFolder);
        } catch (NotFoundException e) {
            logger.warn("Failed to find the target class to modify, " +
                    targetClass + ", verify that it ClassPool has been configured to look " +
                    "into the right location");
        }
    }
}
