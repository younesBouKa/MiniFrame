package org.aspect.agent.transformers;

import java.lang.instrument.ClassFileTransformer;
import java.util.HashSet;
import java.util.Set;

public abstract class CustomTransformer implements ClassFileTransformer {
    protected Set<String> processedClasses = new HashSet<>();
    private final String defaultExcludeRegex = "(com\\.intellij|jdk|org\\.jetbrains).*";
    private String[] classFilterRegexps = new String[]{};

    public final String[] getClassFilterRegexps() {
        return classFilterRegexps;
    }

    public final void setClassFilterRegexps(String[] classFilterRegexps) {
        this.classFilterRegexps = classFilterRegexps;
    }

    protected boolean matchDefault(final String str){
        return str!=null && !str.matches(defaultExcludeRegex.trim());
    }

    public final boolean matchAll(final String str){
        if(!matchDefault(str))
            return false;
        if(classFilterRegexps==null)
            return true;
        for (String regex : classFilterRegexps){
            if(regex!=null && !regex.trim().isEmpty() && !str.matches(regex))
                return false;
        }
        return true;
    }

    public final boolean matchAny(final String str){
        if(classFilterRegexps==null || !matchDefault(str))
            return false;
        for (String regex : classFilterRegexps){
            if(regex!=null && !regex.trim().isEmpty() && str.matches(regex))
                return true;
        }
        return false;
    }

    public String normalizeClassName(String className){
        return (className!=null ? className : "").replaceAll("/", ".");
    }

    public boolean toExclude(String normalizedClassName){
        return !matchAll(normalizedClassName); // if it doesn't match all regexps
    }
}
