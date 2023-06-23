package org.aspect.proxy;

import javassist.*;
import org.aspect.scanners.AspectScanManagerImpl;
import org.tools.Log;

import java.io.IOException;

public class JavassistInstrumenter {
    private static final Log logger = Log.getInstance(JavassistInstrumenter.class);

    public void insertTimingIntoMethod(String targetClass, String targetMethod) throws NotFoundException, CannotCompileException, IOException {

        final String targetFolder = ".";

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
            method.insertAfter(
                    "{final long endMs = System.currentTimeMillis();" +
                    "System.out.println(\"" +targetMethod + "\"+(endMs-startMs));}"
            );

            compiledClass.writeFile("C:\\Users\\younes.boukanoucha\\IdeaProjects\\MiniFrame\\Aspect\\target\\classes");
            logger.info(targetClass + "." + targetMethod +
                    " has been modified and saved under " + targetFolder);
        } catch (NotFoundException e) {
            logger.warn("Failed to find the target class to modify, " +
                    targetClass + ", verify that it ClassPool has been configured to look " +
                    "into the right location");
        }
    }

    public static void main(String[] args) throws Exception {
        final String defaultTargetClass = "org.aspect.scanners.AspectScanManagerImpl";
        final String defaultTargetMethod = "update";
        new JavassistInstrumenter().insertTimingIntoMethod(defaultTargetClass, defaultTargetMethod);
        AspectScanManagerImpl scanManager = new AspectScanManagerImpl();
        scanManager.update(true);
    }
}