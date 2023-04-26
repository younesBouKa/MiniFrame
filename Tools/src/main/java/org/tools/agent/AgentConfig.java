package org.tools.agent;

import org.tools.exceptions.FrameworkException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class AgentConfig {
    private File agentJarFile;
    private String targetVmPID;
    private String args;

    public AgentConfig(File agentJarFile, String targetVmPID, String args) throws Exception {
        validAgentJarFile(agentJarFile);
        this.agentJarFile = agentJarFile;
        this.targetVmPID = targetVmPID;
        this.args = args;
    }

    public AgentConfig() {

    }

    public File getAgentJarFile() {
        return agentJarFile;
    }

    public void setAgentJarFile(File agentJarFile) throws Exception {
        validAgentJarFile(agentJarFile);
        this.agentJarFile = agentJarFile;
    }

    public String getTargetVmPID() {
        return targetVmPID;
    }

    public void setTargetVmPID(String targetVmPID) {
        this.targetVmPID = targetVmPID;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    private boolean validAgentJarFile(File file) throws Exception {
        try {
            JarFile jarFile = new JarFile(file);
            Manifest manifest = jarFile.getManifest();
            if(manifest!=null){
                Attributes mainAttributes = manifest.getMainAttributes();
                if(mainAttributes.get(new Attributes.Name("Agent-Class"))!=null)
                    return true;
                if(mainAttributes.get(new Attributes.Name("Premain-Class"))!=null)
                    return true;
                throw new FrameworkException("Agent jar file doesn't have manifest file doesn't contain 'Agent-Class' or 'Premain-Class'");
            }else {
                throw new FrameworkException("Agent jar file doesn't contain a valid manifest file");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new FrameworkException("File ["+file.getAbsolutePath()+"] is not a valid agent jar file");
        }
    }

    @Override
    public String toString() {
        return "AgentConfig{" +
                "agentJarFile=" + agentJarFile +
                ", targetVmPID='" + targetVmPID + '\'' +
                ", args='" + args + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentConfig)) return false;
        AgentConfig that = (AgentConfig) o;
        return Objects.equals(getAgentJarFile(), that.getAgentJarFile()) && Objects.equals(getTargetVmPID(), that.getTargetVmPID()) && Objects.equals(getArgs(), that.getArgs());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAgentJarFile(), getTargetVmPID(), getArgs());
    }
}
