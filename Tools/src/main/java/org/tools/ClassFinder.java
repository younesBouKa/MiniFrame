package org.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ClassFinder {
    private static boolean initialized;
    private static long lastUpdateTimeStamp = 0;
    private static long updateCount = 0;
    private static final Set<Consumer<EventObject>> reloadListeners = new HashSet<>();
    private static final String JAVA_CLASS_PATH = "java.class.path";
    private static Set<String> CACHE;
    private static final Log logger = Log.getInstance(ClassFinder.class);
    private static final String classpath;
    private static final String classPathSeparator;
    private static final String filePathSeparator;
    private static final Set<String> paths;
    private static final String javaHome;
    private static final File libDir;
    private static Predicate<String> classNameLoadingFilter = null;

    static {
        javaHome = System.getProperty("java.home");
        classpath = System.getProperty(JAVA_CLASS_PATH);
        classPathSeparator = System.getProperty("path.separator");
        filePathSeparator = System.getProperty("file.separator");
        paths = Arrays.stream(classpath.split(classPathSeparator)).collect(Collectors.toSet());
        libDir = new File(javaHome + File.separator + "lib");
        //showSystemProperties();
        showUsedProperties();
        update(true, false);
    }

    /**
     * Add paths to class path
     * @param pathsToAdd
     */
    public static void addToClassPath(Set<String> pathsToAdd){
        boolean pathChanged = paths.addAll(pathsToAdd);
        String preparedCP = String.join(classPathSeparator, paths);
        System.setProperty(JAVA_CLASS_PATH, preparedCP);
        if(pathChanged){
            logger.info("New paths added to class path: "+pathsToAdd);
            update(true, false);
        }
    }

    /**
     * Can be used before calling other methods to optimize scanning phase and minimize size of cache
     * @param loadFilter
     */
    public static void setLoadingFilter(Predicate<String> loadFilter){
        classNameLoadingFilter = loadFilter;
    }

    /*-------------------------- get classes -----------------------------*/
    public static Set<Class> getClassesWithFilter(Predicate<Class> classFilter) {
        return getClassesWithFilter(classFilter, false, false);
    }

    public static Set<Class> getClassesWithFilter(Predicate<Class> filter, boolean reload, boolean scanJavaLibs) {
        return getAllKnownClasses(reload, scanJavaLibs)
                .stream()
                .map(ClassFinder::loadClass)
                .filter(Objects::nonNull)
                .filter(filter)
                .collect(Collectors.toSet());
    }

    /*------------------------------ get class names ------------------------------*/
    public static Set<String> getClassNamesWithNameFilter(Predicate<String> classNameFilter) {
        return getClassNamesWithNameFilter(classNameFilter, false, false);
    }

    public static Set<String> getClassNamesWithNameFilter(Predicate<String> classNameFilter, boolean reload, boolean scanJavaLibs) {
        return getAllKnownClasses(reload, scanJavaLibs)
                .stream()
                .filter(classNameFilter)
                .collect(Collectors.toSet());
    }

    /*----------------------------- tools methods ---------------------------------*/
    public static Class loadClass(String className){
        try {
            return Class.forName(className, false, ClassFinder.class.getClassLoader());
        }catch (InternalError internalError){
            //logger.error("loadClass InternalError:"+internalError.getMessage());
        }catch (UnsatisfiedLinkError unsatisfiedLinkError){
            //logger.error("loadClass UnsatisfiedLinkError:"+unsatisfiedLinkError.getMessage());
        }catch (ExceptionInInitializerError exceptionInInitializerError){
            //logger.error("loadClass ExceptionInInitializerError:"+exceptionInInitializerError.getMessage());
        }catch (NoClassDefFoundError noClassDefFoundError){
            //logger.error("loadClass NoClassDefFoundError:"+noClassDefFoundError.getMessage());
        }catch (ClassNotFoundException classNotFoundException){
            //logger.error("loadClass ClassNotFoundException:"+classNotFoundException.getMessage());
        }catch (Throwable e){
            //logger.error(e);
        }
        return null;
    }

    public static void showUsedProperties(){
        logger.debug("-------------------------ClassFinder properties----------------------");
        logger.debug(
                "javaHome: " + javaHome+"\n"+
                "classPathSeparator: " +classPathSeparator+"\n"+
                "filePathSeparator: " +filePathSeparator+"\n"+
                "classpath: " +classpath+"\n"+
                "paths: "+ paths
        );
        logger.debug("-------------------------ClassFinder properties----------------------");
    }

    public static void showSystemProperties(){
        logger.debug("-------------------------ClassFinder SystemProperties----------------------");
        Properties properties = System.getProperties();
        for(String key: properties.stringPropertyNames()){
            logger.debug(key+"="+properties.getProperty(key));;
        }
        logger.debug("-------------------------ClassFinder SystemProperties----------------------");
    }

    public static long getLastUpdateTimeStamp(){
        return lastUpdateTimeStamp;
    }

    public static boolean isInitialized(){
        return initialized;
    }

    /*--------------------- Inner methods -----------------------------*/
    private static void update(boolean reload, boolean scanJavaLibs){
        updateCount++;
        logger.info("Update Class Finder cache for the ["+updateCount+"] time(s), last update timestamp ["+lastUpdateTimeStamp+"]");
        if(reload)
            logger.warn("Reload flag is activated, Class Finder Cache will be reloaded");
        if(classNameLoadingFilter==null){
            logger.warn("For Scanning optimization you can set loading filter in order to filter non needed classes");
            classNameLoadingFilter = className -> true;
        }
        CACHE =  new HashSet<>();
        logger.info("ClassFinder cache loading started ...");
        long startTime = System.currentTimeMillis();
        findClasses((className)->{
            if(className!=null
                    && !className.contains("$")
                    && classNameLoadingFilter.test(className)
            ){
                CACHE.add(className);
            }
            return true;
        }, scanJavaLibs);
        long duration = System.currentTimeMillis() - startTime;
        logger.info("ClassFinder cache loading ended in ["+duration+"] Millis with ["+CACHE.size()+"] class(es)");
        //callReloadListeners();
        lastUpdateTimeStamp = System.currentTimeMillis() + 10;
        initialized = true;
    }

    private static Set<String> getAllKnownClasses(boolean reload, boolean scanJavaLibs) {
        if(!initialized || reload){
            update(reload, scanJavaLibs);
        }
        return CACHE;
    }

    private static Set<Class> getAssignableClasses(Class interfaceOrSuperclass) {
        return getClassesWithFilter((clazz) ->{
            return interfaceOrSuperclass.isAssignableFrom(clazz);
        });
    }

    private static List<File> getClassLocationsForCurrentClasspath() {
        List<File> urls = new ArrayList<File>();
        String javaClassPath = classpath;
        if (javaClassPath != null) {
            for (String path : javaClassPath.split(classPathSeparator)) {
                urls.add(new File(path));
            }
        }
        return urls;
    }

    private static List<Class> getMatchingClasses(String validPackagePrefix, Class interfaceOrSuperclass) {
        throw new IllegalStateException("Not yet implemented!");
    }

    private static List<Class> getMatchingClasses(String validPackagePrefix) {
        throw new IllegalStateException("Not yet implemented!");
    }

    private interface Visitor<T> {
        /**
         * @return {@code true} if the algorithm should visit more results,
         * {@code false} if it should terminate now.
         */
        public boolean visit(T t);
    }

    private static void findClasses(Visitor<String> visitor, boolean scanJavaLib) {
        if(scanJavaLib)
            logger.warn("ScanJavaLib is activated, it takes more time to find classes");
        if (scanJavaLib && libDir.exists()) {
            logger.info("Search for classes in lib directory: "+libDir);
            findClasses(libDir, libDir, true, visitor);
        }

        String libAbsolutPathStr = libDir.getAbsolutePath();
        logger.info("Search for classes in paths: "+paths);
        for (String path : paths) {
            File file = new File(path);
            String fileAbsolutPathStr = file.getAbsolutePath();
            boolean toProcess =  file.exists()
                    && (!fileAbsolutPathStr.startsWith(libAbsolutPathStr) || scanJavaLib);
            if (toProcess ) {
                findClasses(file, file, true, visitor);
            }
        }
    }

    private static boolean findClasses(File root, File file, boolean includeJars, Visitor<String> visitor) {
        if(file==null)
            return false;
        if (file.isDirectory() && file.listFiles()!=null) {
            for (File child : file.listFiles()) {
                if (!findClasses(root, child, includeJars, visitor)) {
                    return false;
                }
            }
        } else {
            if (file.getName().toLowerCase().endsWith(".jar") && includeJars) {
                JarFile jar = null;
                try {
                    jar = new JarFile(file);
                } catch (Exception ex) {
                    logger.error("Can't read jar file: "+file.getName());
                }
                if (jar != null) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        int extIndex = name.lastIndexOf(".class");
                        if (extIndex > 0) {
                            String className = name
                                    .substring(0, extIndex)
                                    .replaceAll("/", ".");
                            if (!visitor.visit(className)) {
                                return false;
                            }
                        }
                    }
                }
            }
            else if (file.getName().toLowerCase().endsWith(".class")) {
                String className = createClassName(root, file);
                if (!visitor.visit(className)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String createClassName(File root, File file) {
        StringBuilder sb = new StringBuilder();
        String fileName = file.getName();
        sb.append(fileName.substring(0, fileName.lastIndexOf(".class")));
        file = file.getParentFile();
        while (file != null && !file.equals(root)) {
            sb.insert(0, '.').insert(0, file.getName());
            file = file.getParentFile();
        }
        return sb.toString();
    }

    private static List<Class> getClassesFromDirectory(File path) {
        List<Class> classes = new ArrayList<Class>();
        logger.debug("getClassesFromDirectory: Getting classes for " + path);

        // get jar files from top-level directory
        List<File> jarFiles = listFiles(path, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {return name.endsWith(".jar");}
        }, false);
        for (File file : jarFiles) {
            classes.addAll(getClassesFromJarFile(file));
        }

        // get all class-files
        List<File> classFiles = listFiles(path, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }
        }, true);

        // List<URL> urlList = new ArrayList<URL>();
        // List<String> classNameList = new ArrayList<String>();
        int substringBeginIndex = path.getAbsolutePath().length() + 1;
        for (File classfile : classFiles) {
            String className = classfile.getAbsolutePath().substring(substringBeginIndex);
            className = fromFileToClassName(className);
            logger.debug("Found class %s in path %s: ", className, path);
            try {
                Class clazz = loadClass(className);
                if(clazz!=null)
                    classes.add(clazz);
            } catch (Throwable e) {
                logger.error("Couldn't create class %s. %s: ", className, e);
            }

        }

        return classes;
    }

    private static List<Class> getClassesFromJarFile(File path) {
        List<Class> classes = new ArrayList<Class>();
        logger.debug("getClassesFromJarFile: Getting classes for " + path);

        try {
            if (path.canRead()) {
                JarFile jar = new JarFile(path);
                Enumeration<JarEntry> en = jar.entries();
                while (en.hasMoreElements()) {
                    JarEntry entry = en.nextElement();
                    if (entry.getName().endsWith("class")) {
                        String className = fromFileToClassName(entry.getName());
                        logger.debug("\tgetClassesFromJarFile: found " + className);
                        Class clazz = loadClass(className);
                        if(clazz!=null)
                            classes.add(clazz);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to read classes from jar file: " + path);
            logger.error(e);
        }

        return classes;
    }

    private static Collection<? extends Class> getClassesFromPath(File path) {
        if (path.isDirectory()) {
            return getClassesFromDirectory(path);
        } else {
            return getClassesFromJarFile(path);
        }
    }

    private static String fromFileToClassName(final String fileName) {
        return fileName.substring(0, fileName.length() - 6).replaceAll("/|\\\\", "\\.");
    }

    private static List<File> listFiles(File directory, FilenameFilter filter, boolean recurse) {
        List<File> files = new ArrayList<File>();
        File[] entries = directory.listFiles();

        // Go over entries
        for (File entry : entries) {
            // If there is no filter or the filter accepts the
            // file / directory, add it to the list
            if (filter == null || filter.accept(directory, entry.getName())) {
                files.add(entry);
            }

            // If the file is a directory and the recurse flag
            // is set, recurse into the directory
            if (recurse && entry.isDirectory()) {
                files.addAll(listFiles(entry, filter, recurse));
            }
        }

        // Return collection of files
        return files;
    }

    /*

    public static boolean registerReloadListener(Consumer<EventObject> listener){
        return reloadListeners.add(listener);
    }

    public static boolean removeReloadListener(Consumer<EventObject> listener){
        return reloadListeners.remove(listener);
    }

    private static int callReloadListeners(){
        logger.debug("Calling Class finder Reload listeners");
        int calledListenerCount = 0;
        for(Consumer<EventObject> listener : reloadListeners){
            try {
                listener.accept(new EventObject(CACHE.size()));
                calledListenerCount++;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        logger.debug("["+calledListenerCount+"] Reload listeners was called");
        return calledListenerCount;
    }
*/

}