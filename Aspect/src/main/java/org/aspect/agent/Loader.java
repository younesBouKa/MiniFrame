package org.aspect.agent;

import org.tools.agent.AgentLoader;

import java.util.Map;

public class Loader {
    public static void init(){
        try {
            buildAgent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void buildAgent() throws Exception {
        Map<String, Object> options = AgentLoader.getDefaultOptions();
        options.put("type", "logger");
        options.put("cnfr", "(org\\.demo\\.).*"); // class name filter regex
        AgentLoader.buildAndAttachAgent(
                "org.demo.Launcher",
                "org.aspect",
                "org.aspect.agent.AgentMain",
                options
        );
    }
}
