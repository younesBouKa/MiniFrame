package org.aspect.agent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspect.agent.transformers.ClassLogger;
import org.aspect.agent.transformers.ObjectLifeCycleInterceptor;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class AgentMain {
    private static Instrumentation globalInstrumentation;
    private static final Logger logger = LogManager.getLogger(AgentMain.class);

    static {
        addToolsToCP();
    }

    private static void addToolsToCP(){
        File toolsJarFile = searchForToolsJarFile();
        if(toolsJarFile==null)
            return;
        String toolsJarPath = toolsJarFile.getAbsolutePath();
        String JAVA_CLASS_PATH = "java.class.path";
        String PATH_SEPARATOR = "path.separator";
        String classPathSeparator = System.getProperty(PATH_SEPARATOR);
        String classpath = System.getProperty(JAVA_CLASS_PATH);
        Set<String> existingPaths = Arrays.stream(classpath.split(classPathSeparator))
                .collect(Collectors.toSet());
        existingPaths.add(toolsJarPath);
        String preparedCP = String.join(classPathSeparator,existingPaths);
        System.setProperty(JAVA_CLASS_PATH, preparedCP);
        logger.debug("Tools jar file ["+toolsJarPath+"] added to ["+JAVA_CLASS_PATH+"] system property");
    }

    public static File searchForToolsJarFile(){
        URL resource = AgentMain.class.getResource("/tools.jar");
        if(resource==null){
            logger.debug("Can't find tools.jar file in project");
            String javaJdkJreDir = System.getProperty("java.home");
            if(javaJdkJreDir!=null && !javaJdkJreDir.isEmpty()){
                File file = new File(javaJdkJreDir);
                if(file.exists() && file.isDirectory()){
                    File javaJdkLibDir = file.getParentFile();
                    if (javaJdkLibDir.exists() && javaJdkLibDir.isDirectory()){
                        File[] possiblesFiles = javaJdkLibDir.listFiles(pathname -> pathname.getAbsolutePath().endsWith("tools.jar"));
                        if(possiblesFiles!=null && possiblesFiles.length>0){
                            logger.debug("Possibles tools.jar files: "+possiblesFiles);
                            return Arrays.stream(possiblesFiles).findFirst().get();
                        }else {
                            logger.debug("Can't find any tools.jar file in "+javaJdkLibDir);
                        }
                    }
                }
            }
            return null;
        }else{
            return new File(resource.getFile());
        }
    }

    public static void premain(String args, Instrumentation instrumentation){
        logger.debug("Hello from 'premain' in AgentMain with args: "+args);
        globalInstrumentation = instrumentation;
        try {
            process("premain", args, instrumentation);
        }catch (Throwable throwable){
            logger.error(throwable.getMessage());
        }
    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        logger.debug("Hello from 'agentmain' in AgentMain with args: "+args);
        globalInstrumentation = instrumentation;
        try {
            process("agentmain", args, instrumentation);
        }catch (Throwable throwable){
            logger.error(throwable.getMessage());
        }
    }

    public static long getObjectSize(Object obj) throws Exception {
        if(globalInstrumentation==null)
            throw new Exception("AgentMain no initialized");
        return globalInstrumentation.getObjectSize(obj);
    }

    public static Class[] getAllLoadedClasses() throws Exception {
        if(globalInstrumentation==null)
            throw new Exception("AgentMain no initialized");
        return globalInstrumentation.getAllLoadedClasses();
    }


    public static Class[] getInitiatedClasses(ClassLoader classLoader) throws Exception {
        if(globalInstrumentation==null)
            throw new Exception("AgentMain no initialized");
        return globalInstrumentation.getInitiatedClasses(classLoader);
    }

    public static void addToBootstrapClassLoaderSearch(JarFile jarFile) throws Exception {
        if(globalInstrumentation==null)
            throw new Exception("AgentMain no initialized");
        globalInstrumentation.appendToBootstrapClassLoaderSearch(jarFile);
    }

    public static void addToSystemClassLoaderSearch(JarFile jarFile) throws Exception {
        if(globalInstrumentation==null)
            throw new Exception("AgentMain no initialized");
        globalInstrumentation.appendToSystemClassLoaderSearch(jarFile);
    }

    /**
     * Process args
     * @param typeMethod
     * @param args
     * @param instrumentation
     */
    public static void process(String typeMethod, String args, Instrumentation instrumentation){
        Map<String, Object> params = getParams(args);
        logger.debug("Hello from '"+typeMethod+"' in AgentMain with params: "+params);
        String transformerType = (String) params.getOrDefault("type","null");
        String classNameFilterRegex = (String) params
                .getOrDefault("class_name_filter_regex",
                        (String) params
                                .getOrDefault("cnfr", "")
                );
        String[] classFilterRegexps = new String[]{classNameFilterRegex};
        if(transformerType.equals("lifecycle")){
            ObjectLifeCycleInterceptor objectLifeCycleInterceptor = new ObjectLifeCycleInterceptor();
            objectLifeCycleInterceptor.setClassFilterRegexps(classFilterRegexps);
            instrumentation.addTransformer(objectLifeCycleInterceptor);
        }else if(transformerType.equals("logger")){
            ClassLogger classLogger = new ClassLogger();
            classLogger.setClassFilterRegexps(classFilterRegexps);
            instrumentation.addTransformer(classLogger);
        }else{
            logger.warn("No options was set to define agent transformer choose between types: [lifecycle|logger]");
        }
    }

    /**
     * args must be separated with "&"
     * key value must be separated with ':'
      */
    public static Map<String, Object> getParams(String args){
        Map<String,Object> params = new HashMap<>();
        if(args==null)
            return params;
        String[] pairs = args.split("&");
        if(pairs.length>0){
            String key, value;
            for (String pair: pairs){
                String[] keyValue = pair.split(":");
                if(keyValue.length>=1){
                    key = keyValue[0];
                    value = keyValue[1];
                    params.put(key, value);
                }
            }
        }
        return params;
    }
}