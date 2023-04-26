package org.tools.agent;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.tools.Log;
import org.tools.exceptions.FrameworkException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentLoader {
    private static final Log logger = Log.getInstance(AgentLoader.class);
    private static final Map<String,Object> agentParams;
    private static final List<AgentConfig> loadedAgents;

    static {
        agentParams = new HashMap<>();
        loadedAgents = new ArrayList<>();
    }

    public static AgentConfig searchAndAttachAgent(String applicationName, String agentMainClass, Map<String,Object> options) throws Exception {
        if(applicationName==null){
            throw new FrameworkException("Application name can't be null");
        }
        List<VirtualMachineDescriptor> foundedTargetVMs = AgentTools.getTargetVirtualMachineDescriptor(applicationName);
        if(foundedTargetVMs==null || foundedTargetVMs.size()==0){
            throw new FrameworkException("Can't find VM with name ["+applicationName+"]");
        }
        if(foundedTargetVMs.size()>1){
            logger.debug("Many VMs founded with name ["+applicationName+"] "+foundedTargetVMs);
            logger.debug("Agent will be attached to first JVM");
        }
        String targetVmPid = foundedTargetVMs.get(0).id();
        logger.debug("Target VM to use :  "+foundedTargetVMs.get(0)+" with PID: "+targetVmPid);
        if(agentMainClass==null){
            throw new FrameworkException("Agent Main class can't be null");
        }
        Class agentMain;
        try {
            agentMain = ClassLoader.getSystemClassLoader()
                    .loadClass(agentMainClass);
        }catch (Exception e){
            throw new FrameworkException("Agent Main class can't be loaded");
        }
        if(!AgentTools.isValidAgentMainClass(agentMain)){
            throw new FrameworkException("Agent Main class is invalid");
        }
        String agentJarFilePath = AgentTools.getJarFileByClass(agentMain);
        File agentJarFile;
        if(agentJarFilePath==null){
            throw new FrameworkException("Can't find jar file containing agent main class: ["+agentMainClass+"]");
        }else{
            agentJarFile = new File(agentJarFilePath);
        }
        if(options==null || options.isEmpty())
            options = getDefaultOptions();
        String agentArgs = AgentTools.getAgentArgsFromMap(options);
        AgentConfig agentConfig = new AgentConfig(agentJarFile, targetVmPid, agentArgs);
        loadAgent(agentConfig);
        return agentConfig;
    }

    public static AgentConfig buildAndAttachAgent(String applicationName, String packageName, String agentMainClass, Map<String,Object> options) throws Exception {
        if(applicationName==null){
            throw new FrameworkException("application Name can't be null");
        }
        if(packageName==null){
            throw new FrameworkException("package Name can't be null");
        }
        List<VirtualMachineDescriptor> foundedTargetVMs = AgentTools.getTargetVirtualMachineDescriptor(applicationName);
        if(foundedTargetVMs==null || foundedTargetVMs.size()==0){
            throw new FrameworkException("Can't find VM with name ["+applicationName+"]");
        }
        if(foundedTargetVMs.size()>1){
            logger.debug("Many VMs founded with name ["+applicationName+"] "+foundedTargetVMs);
            logger.debug("Agent will be attached to first JVM");
        }
        String targetVmPid = foundedTargetVMs.get(0).id();
        logger.debug("Target VM to use :  "+foundedTargetVMs.get(0)+" with PID: "+targetVmPid);
        if(agentMainClass==null){
            throw new FrameworkException("Agent Main class can't be null");
        }
        if(!AgentTools.isValidAgentMainClass(agentMainClass)){
            throw new FrameworkException("Agent Main class is invalid");
        }
        File agentJarFile;
        try {
            agentJarFile = AgentTools.buildAgentJarFromPackage(packageName, agentMainClass);
            if(agentJarFile==null){
                throw new FrameworkException("Can't build agent jar file from package ["+packageName+"] and agent main class ["+agentMainClass+"]");
            }
        }catch (Exception e){
            throw new FrameworkException("Can't build agent jar file from package ["+packageName+"] and agent main class ["+agentMainClass+"]");
        }
        if(options==null || options.isEmpty())
            options = getDefaultOptions();
        String agentArgs = AgentTools.getAgentArgsFromMap(options);
        AgentConfig agentConfig = new AgentConfig(agentJarFile, targetVmPid, agentArgs);
        loadAgent(agentConfig);
        return agentConfig;
    }

    public static void loadAgent(AgentConfig agentConfig) {
        if(agentConfig.getAgentJarFile()==null){
            throw new FrameworkException("Agent jar file can't be null");
        }
        String agentJarFilePath = agentConfig.getAgentJarFile().getAbsolutePath();
        String targetVmPid = agentConfig.getTargetVmPID();
        String args = agentConfig.getArgs();

        if(targetVmPid==null){
            throw new FrameworkException("Target VM PID can't be null");
        }
        if(args==null || args.isEmpty()){
            logger.warn("Args are null, using default args");
            args = AgentTools.getAgentArgsFromMap(getDefaultOptions());
        }
        logger.debug("Start attaching Java Agent JAR file: ["+agentJarFilePath+"]" );
        logger.debug("Attaching Java Agent to JVM with PID: ["+targetVmPid+"]");
        logger.debug("Using args: ["+args+"]");
        VirtualMachine targetVM = AgentTools.attachAgent(agentConfig);
        if(targetVM!=null)
            loadedAgents.add(agentConfig);
    }

    public static void setDefaultOptions(Map<String, Object> defaultOptions){
        agentParams.putAll(defaultOptions);
    }

    public static Map<String, Object> getDefaultOptions(){
        return agentParams;
    }

    public static void main(String[] args) throws Exception {

    }
}