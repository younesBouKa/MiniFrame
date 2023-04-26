package org.injection.core.global;

import java.util.Set;
import java.util.function.Predicate;

public interface ClassPool {

    Set<String> getAvailableClasses();

     Set<String> getPackagesToScan();

     Set<String> getExcludeClassRegex();

    /*--------------------- get class --------------------------*/
     Set<Class> getClassesWithClassFilter(Predicate<Class> classFilter);

    /*--------------------- get class names --------------------------*/
     Set<String> getClassNamesWithClassFilter(Predicate<Class> classFilter);
}
