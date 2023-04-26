package org.tools;

import org.agent.Agent;
import org.tools.agent.AgentConfig;
import org.tools.agent.AgentLoader;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GlobalTools {
    private static final Log logger = Log.getInstance(GlobalTools.class);

    public static boolean isValidPackageName(String packageName){
        return packageName!=null && !packageName.isEmpty();// && Package.getPackage(packageName)!=null;
    }

    public static void tryInitAgent(String applicationName, String agentMainClass, Map<String, Object> options){
        try {
           initAgent(applicationName, agentMainClass, options);
        }catch (Throwable throwable){
            logger.warn("Can't init java agent, "+throwable.getMessage());
        }
    }

    public static void initAgent(String applicationName, String agentMainClass, Map<String, Object> options) throws ClassNotFoundException {
        String agentPackage = Agent.class.getPackage().getName();
        String agentMain = Agent.class.getCanonicalName();
        if (agentMainClass!=null){
            Class givenMainClass = Class.forName(agentMainClass);
            agentMain = givenMainClass.getCanonicalName();
            agentPackage = givenMainClass.getPackage().getName();
        }
        try {
            AgentConfig agentConfig = AgentLoader.searchAndAttachAgent(
                    applicationName,
                    agentMain,
                    options);
            logger.info("Java Agent ["+agentConfig.getAgentJarFile()+"] attached to ["+agentConfig.getTargetVmPID()+"]");
        }catch (Throwable throwable){
            logger.error("Can't find a java agent jar file, "+throwable.getMessage());
            try {
                AgentConfig agentConfig = AgentLoader.buildAndAttachAgent(
                        applicationName,
                        agentPackage,
                        agentMain,
                        options);
                logger.info("Java Agent ["+agentConfig.getAgentJarFile()+"] attached to ["+agentConfig.getTargetVmPID()+"]");
            }catch (Throwable throwable1){
                logger.error("Can't attach java agent to current application, "+throwable1.getMessage());
            }
        }
    }

    public static long getObjectSize(Object obj){
        try {
            return Agent.getObjectSize(obj);
        } catch (Exception e) {
            logger.error("Can't get object size, "+e.getMessage());
            return 0;
        }
    }
    public static boolean matchAll(final String str, String[] regexps){
        if(str==null)
            return false;
        for (String regex : regexps){
            if(regex!=null && !regex.trim().isEmpty() && !str.matches(regex))
                return false;
        }
        return true;
    }


    public static boolean matchAny(final String str, String[] regexs){
        if(str==null || regexs==null)
            return false;
        for (String regex : regexs){
            if(regex!=null && !regex.trim().isEmpty() && str.matches(regex))
                return true;
        }
        return false;
    }

    public static boolean matchAny(final String str, Set<String> regexs){
        if(str==null || regexs==null)
            return false;
        for (String regex : regexs){
            if(regex!=null && !regex.trim().isEmpty() && str.matches(regex))
                return true;
        }
        return false;
    }
}
