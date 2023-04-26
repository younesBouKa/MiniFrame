package org.injection.core.global;

import org.injection.annotations.BeanScanPackages;
import org.tools.annotations.AnnotationTools;
import org.tools.ClassFinder;
import org.tools.GlobalTools;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BeansClassPool implements ClassPool {
    private static final Log logger = Log.getInstance(BeansClassPool.class);
    private long lastUpdateTimeStamp = 0;
    private long updateCount = 0;
    private Set<String> packagesToScan;
    private Set<String> excludeClassRegex;
    private Set<String> availableClasses;
    private Set<String> possibleRootClasses;

    public BeansClassPool(){
        availableClasses = new HashSet<>();
        //update(true);
    }

    public Set<String> getAvailableClasses(){
        update(false);
        return availableClasses;
    }

    public Set<String> getPackagesToScan() {
        update(false);
        return packagesToScan;
    }

    public Set<String> getExcludeClassRegex() {
        update(false);
        return excludeClassRegex;
    }

    /*--------------------- get class --------------------------*/
    public Set<Class> getClassesWithClassFilter(Predicate<Class> classFilter) {
        if(classFilter==null)
            classFilter = (clazz)-> true;
        return getAvailableClasses()
                .stream()
                .map(ClassFinder::loadClass)
                .filter(Objects::nonNull)
                .filter(classFilter)
                .collect(Collectors.toSet());
    }
    /*--------------------- get class names --------------------------*/
    public Set<String> getClassNamesWithClassFilter(Predicate<Class> classFilter) {
        if(classFilter==null)
            classFilter = (clazz)-> true;
        return getAvailableClasses()
                .stream()
                .map(ClassFinder::loadClass)
                .filter(Objects::nonNull)
                .filter(classFilter)
                .map(Class::getCanonicalName)
                .collect(Collectors.toSet());
    }
    /*--------------------- inner methods --------------------------*/
    public void update(boolean force){
        long lastUpdate = ClassFinder.getLastUpdateTimeStamp();
        if(lastUpdateTimeStamp < lastUpdate || force){
            updateCount++;
            logger.debug("Update Beans Class Pool cache for the ["+updateCount+"] time(s), last update timestamp ["+lastUpdateTimeStamp+"]");
            lastUpdateTimeStamp = lastUpdate;
            preparePackagesToScan();
            scanForAvailableClasses();
            if(availableClasses.isEmpty())
                logger.error("Beans Class Pool doesn't contain any class");
        }
    }

    /**
     * Scan packages for available classes
     * Throws FrameworkException if no root class is set or no package to scan
     */
    private void scanForAvailableClasses(){
        Set<String> packagesToScan = this.packagesToScan;
        Set<String> excludeClassRegex = this.excludeClassRegex;
        if(packagesToScan==null || packagesToScan.isEmpty()){
            logger.warn("No packages to scan found, We scan all packages in ClassPath");
        }else{
            logger.debug("Scanning for available classes in packages "+packagesToScan+"");
        }
        if(excludeClassRegex==null || excludeClassRegex.isEmpty()){
            logger.warn("No Exclude class regex was set");
        }else{
            logger.debug("Using Exclude class regex "+excludeClassRegex+"");
        }
        Predicate<String> classNamePredicate = (String className) -> {
            boolean isPackageOk = packagesToScan==null ||
                    packagesToScan.isEmpty() ||
                    packagesToScan
                    .stream()
                    .anyMatch(className::startsWith);
            boolean isClassOk = excludeClassRegex==null ||
                    excludeClassRegex.isEmpty() ||
                    !GlobalTools.matchAny(className, excludeClassRegex);
            return isPackageOk && isClassOk;
        };
        availableClasses = ClassFinder.getClassNamesWithNameFilter(classNamePredicate);
        logger.debug("Scanning for available classes ended: ["+availableClasses.size()+"]");
        if(availableClasses.isEmpty() && !packagesToScan.isEmpty()){
            logger.error(
                    "No class available in context config "+
                            "try to verify 'BeanScanPackages' annotation"
            );
        }
    }

    /**
     * Get packages names:
     *  - Return packages names if application class is annotated with @BeanScanPackages
     *  - Throw FrameworkException if any package is invalid
     *
     */
    private void preparePackagesToScan(){
        final Set<String> packages = new HashSet<>();
        final Set<String> excludeRegexps = new HashSet<>();
        final Set<String> foundPossibleRootClasses = new HashSet<>();
        Set<Class> classesWithBeanScanPackagesAnnotation = ClassFinder.getClassesWithFilter(clazz->{
            return clazz.isAnnotationPresent(BeanScanPackages.class);
        });
        if(classesWithBeanScanPackagesAnnotation.isEmpty()){
            logger.warn("No class is annotated with @BeanScanPackages");
        }
        classesWithBeanScanPackagesAnnotation.stream()
                .forEach(application->{
                    // get packages from annotation if exist
                    if(AnnotationTools.isAnnotationPresent(application, BeanScanPackages.class)){
                        BeanScanPackages beanScanPackagesAnnotation =
                                (BeanScanPackages) AnnotationTools
                                        .getAnnotation(application, BeanScanPackages.class);
                        try{
                            GlobalTools.matchAny("TEST_ONLY", beanScanPackagesAnnotation.excludes());
                            excludeRegexps.addAll(
                                    Arrays.stream(beanScanPackagesAnnotation.excludes())
                                            .map(String::trim)
                                            .filter(str->!str.isEmpty())
                                            .collect(Collectors.toList())
                            );
                        }catch(Exception e){
                            //e.printStackTrace();
                            throw new FrameworkException(
                                    "Excludes value of  BeanScanPackages annotation is an invalid pattern: "+e.getMessage()
                            );
                        }
                        String[] packagesName = beanScanPackagesAnnotation.packages();
                        Arrays.stream(packagesName)
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(packName -> !packName.isEmpty())
                                .forEach(packName -> {
                                    if(!GlobalTools.isValidPackageName(packName))
                                        throw new FrameworkException(
                                                "Package name from BeanScanPackages annotation on class: "+ application.getCanonicalName()+" is invalid : "+packName
                                        );
                                    packages.add(packName);
                                });
                        foundPossibleRootClasses.add(application.getCanonicalName());
                    }
                });
        packagesToScan = packages;
        excludeClassRegex = excludeRegexps;
        possibleRootClasses = foundPossibleRootClasses;
        logger.debug(
                "Preparation of packages to scan ended with:\n" +
                "Packages: "+ packagesToScan+"\n"+
                "Exclude Class Regex: "+excludeRegexps+"\n"+
                "Possible Root Classes: "+possibleRootClasses
        );
    }

}
