package org.tools.agent;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AgentTools {
    private static final Log logger = Log.getInstance(AgentTools.class);
    private static final String KEY_VALUE_SEPARATOR = ":";
    private static final String PAIRS_SEPARATOR = "&";

    public static VirtualMachine attachAgent(AgentConfig agentConfig){
        try {
            String agentJarFilePath = agentConfig.getAgentJarFile().getAbsolutePath();
            logger.debug("Attaching Agent with config: [" + agentConfig +"]");
            String targetVmPID = agentConfig.getTargetVmPID();
            VirtualMachine targetJVMAttach = VirtualMachine.attach(targetVmPID);
            String args = agentConfig.getArgs()!=null
                    && !agentConfig.getArgs().trim().isEmpty()
                    ? agentConfig.getArgs().trim()
                    : null;
            targetJVMAttach.loadAgent(agentJarFilePath, args);
            targetJVMAttach.detach();
            logger.debug("Java Agent attached to target JVM and loaded successfully");
            return targetJVMAttach;
        } catch (Exception e) {
            e.printStackTrace();
            throw new FrameworkException("Error while attaching Agent jar file: ["+agentConfig.getAgentJarFile()+"], "+e.getMessage());
        }
    }

    public static List<VirtualMachineDescriptor> getTargetVirtualMachineDescriptor(String applicationName){
        List<VirtualMachineDescriptor> allJVMs = VirtualMachine.list();
        if(allJVMs.isEmpty()){
            logger.error("No JVM was found");
            return Collections.emptyList();
        }else{
            logger.info("Available JVMs: "+allJVMs);
        }
        List<VirtualMachineDescriptor> foundedVMs = allJVMs
                .stream()
                .filter(jvm -> {
                    String jvmDisplayName = jvm.displayName();
                    String jvmToString = jvm.toString();
                    logger.debug("JVM: ["+ jvmToString+"]");
                    return jvmDisplayName.contains(applicationName);
                })
                .collect(Collectors.toList());
        return foundedVMs;
    }

    public static String getAgentArgsFromMap(Map<String, Object> map){
        StringBuilder args = new StringBuilder();
        int i=0;
        for (String key: map.keySet()){
            String pair = key + KEY_VALUE_SEPARATOR + map.getOrDefault(key, "");
            args.append(pair);
            i++;
            if(i<map.size()){
                args.append(PAIRS_SEPARATOR);
            }
        }
        return args.toString();
    }

    public  static String getJarFileByClass(Class clazz) {
        if(clazz==null)
            return null;
        URL classResource = clazz.getResource(clazz.getSimpleName() + ".class");
        if (classResource == null) {
            throw new FrameworkException("class resource is null");
        }
        String url = classResource.toString();
        if (url.startsWith("jar:file:")) {
            // extract 'file:......jarName.jar' part from the url string
            String path = url.replaceAll("^jar:(file:.*[.]jar)!/.*", "$1");
            try {
                return Paths.get(new URL(path).toURI()).toString();
            } catch (Exception e) {
                logger.error("Invalid Jar File URL String, "+e.getMessage());
            }
        }else{
            logger.error("["+clazz.getSimpleName()+"] doesn't bellow to a jar file: "+url);
        }
        return null;
    }

    /**
     * Create agent jar file from package on the fly
     */
    public static File buildAgentJarFromPackage(String packageName, String agentMainClass) throws Exception {
        if(packageName==null || Package.getPackage(packageName)==null){
            throw new FrameworkException("Can't find package with given name ["+packageName+"]");
        }
        URL packageURL = ClassLoader
                .getSystemClassLoader()
                .getResource(packageName.replaceAll("\\.", "/"));
        if (packageURL == null) {
            throw new FrameworkException("Can't load package ["+packageName+"]");
        }
        File packageDir = new File(packageURL.getPath());
        if (!packageDir.exists()) {
            throw new FrameworkException("Package doesn't exist ["+packageName+"]");
        }
        if (!packageDir.isDirectory()) {
            throw new FrameworkException("Package is not a directory ["+packageDir.getAbsolutePath()+"]");
        }
        // searching for manifest file if exist
        File foundedManifestFile = searchForManifestFile(packageDir);
        Manifest manifest;
        if (foundedManifestFile == null) {
            logger.warn("No Manifest file in this package ["+packageDir+"]");
            if(agentMainClass==null || !agentMainClass.contains(packageName)){
                throw new FrameworkException("AgentMainClass ["+agentMainClass+"] doesn't bellow to package ["+packageName+"]");
            }
            Class agentMain = ClassLoader.getSystemClassLoader().loadClass(agentMainClass);
            if(!isValidAgentMainClass(agentMain)){
                throw new FrameworkException("AgentMainClass ["+agentMainClass+"] is invalid");
            }
            manifest = prepareAgentManifest(agentMainClass, agentMainClass);
        } else {
            logger.debug("Manifest file to use : " + foundedManifestFile.getAbsoluteFile());
            manifest = new Manifest();
            manifest.read(Files.newInputStream(foundedManifestFile.toPath()));
        }

        File jarFile = prepareJarFile("injection_agent_", packageDir, packageName, manifest);
        return jarFile;
    }

    private static File prepareJarFile(String prefix, File packageDir, String packageName, Manifest manifest) throws IOException {
        File jarFile = File.createTempFile(prefix, ".jar");
        jarFile.deleteOnExit();
        JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarFile.toPath()), manifest);
        addPackageFilesToJar(jar, packageDir, packageName);
        jar.close();
        return jarFile;
    }

    private static JarOutputStream addPackageFilesToJar(JarOutputStream jar, File packageDir, String packageName) throws IOException {
        if(packageDir.isFile()){
            String filePathWithDotes = packageDir.getPath().replaceAll(Pattern.quote(File.separator), ".");
           int startPackageNameIndex = filePathWithDotes.lastIndexOf(packageName);
            String path = packageDir.getPath().substring(startPackageNameIndex);
            jar.putNextEntry(new JarEntry(path));
            packageDir.setReadable(true, false);
            jar.write(Files.readAllBytes(packageDir.toPath()));
        }else if(packageDir.isDirectory() && packageDir.listFiles()!=null){
            for (File anotherFile : packageDir.listFiles()) {
                addPackageFilesToJar(jar, anotherFile, packageName);
            }
        }
        return jar;
    }

    public static File searchForManifestFile(File dir){
        if(dir.isFile() && dir.getName().toLowerCase().endsWith(".mf"))
            return dir;
        else if(dir.isDirectory() && dir.listFiles()!=null){
            for(File file : dir.listFiles()){
                File founded = searchForManifestFile(file);
                if(founded!=null)
                    return founded;
            }
        }
        return null;
    }

    public static boolean isValidAgentMainClass(String className){
        if(className==null)
            return false;
        Class agentMainClass;
        try {
            agentMainClass = ClassLoader.getSystemClassLoader()
                    .loadClass(className);
        }catch (Exception e){
            throw new FrameworkException("Class ["+className+"] can't be loaded");
        }
        return isValidAgentMainClass(agentMainClass);
    }

    public static boolean isValidAgentMainClass(Class clazz){
        if(clazz==null)
            return false;
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods){
            if(isValidAgentMethod(method, AgentMethodName.agentmain)
                    || isValidAgentMethod(method, AgentMethodName.premain)){
                return true;
            }
        }
        logger.error("Class doesn't have any valid 'agentmain' or 'premain' method");
        return false;
    }

    public static boolean isValidAgentMethod(Method method, AgentMethodName agentMethodName) {
        if(method==null)
            return false;
        String methodName = method.getName();
        int methodModifier = method.getModifiers();
        if (
                methodName.equals(agentMethodName.name())
                && Modifier.isStatic(methodModifier)
                && Modifier.isPublic(methodModifier)
        ){
            Parameter[] parameters = method.getParameters();
            if(parameters.length==2
                    && parameters[0].getType().equals(String.class)
                    && parameters[1].getType().equals(java.lang.instrument.Instrumentation.class))
                return true;
            else
                logger.error("Method ["+method.getName()+"] doesn't have valid parameters");
        }
        return false;
    }

    /* public static File searchForAgentMainClass(File file){
         if(file.isFile()){
             String fileName = file.getName();
             if(fileName.endsWith(".class") && isValidAgentMainClassFile(file)){
                 return file;
             }
             if(fileName.endsWith(".jar")){
                 try {
                     JarFile jarFile = new JarFile(file);
                     Enumeration<JarEntry> jarEntries = jarFile.entries();
                     JarEntry jarEntry;
                     while ( (jarEntry = jarEntries.nextElement())!=null){
                         if (!jarEntry.isDirectory() && jarEntry.getName().endsWith(".class")){
                             String entryName = jarEntry.getName();
                             String entryNameWithoutExt = entryName.substring(0, entryName.lastIndexOf("."));
                             String entryExt = entryName.substring(entryName.lastIndexOf("."));
                             File entryFile = File.createTempFile(
                                     jarFile.getName()+
                                             File.separator+
                                             entryNameWithoutExt,
                                     entryExt);
                             entryFile.deleteOnExit();
                             if (isValidAgentMainClassFile(entryFile))
                                 return entryFile;
                         }else{
                             logger.error("searchForAgentMainClass : Directory in jar file, not yet implemented");
                             return null;
                         }
                     }
                 }catch (Exception e){
                     logger.error("Can't read file ["+file.getAbsolutePath()+"]");
                 }
             }
         }
         else if(file.isDirectory() && file.listFiles()!=null){
             for(File child : file.listFiles()){
                 File founded = searchForAgentMainClass(child);
                 if(founded!=null)
                     return founded;
             }
         }
         return null;
     }

     private static boolean isValidAgentMainClassFile(File classFile){
        return false;
     }
 */
    public static Manifest prepareAgentManifest(String agentMainClass, String agentPremainClass){
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(new Attributes.Name("Agent-Class"), agentMainClass);
        manifest.getMainAttributes().put(new Attributes.Name("Can-Redefine-Classes"), "true");
        manifest.getMainAttributes().put(new Attributes.Name("Can-Retransform-Classes"), "true");
        manifest.getMainAttributes().put(new Attributes.Name("Premain-Class"), agentPremainClass);
        return manifest;
    }

    public static void main(String[] args) throws Exception {
        File jarFile = buildAgentJarFromPackage("org.agent", "org.agent.Agent");
        logger.debug(jarFile);
    }

}