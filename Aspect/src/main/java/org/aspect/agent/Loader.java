package org.aspect.agent;

import org.tools.agent.AgentLoader;

import java.util.Map;

public class Loader {

    public static void buildAgent() {
        Map<String, Object> options = AgentLoader.getDefaultOptions();
        options.put("type", "weaver");
        options.put("cnfr", "(org\\.demo\\.).*"); // class name filter regex
        try {
            Class.forName("org.aspect.TestOnly").getPackage();
            AgentLoader.buildAndAttachAgent(
                    "org.demo.Launcher",
                    "org.aspect",
                    "org.aspect.agent.AgentMain",
                    options
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void attachAgent() throws Exception {
        Map<String, Object> options = AgentLoader.getDefaultOptions();
        options.put("type", "weaver");
        options.put("cnfr", "(org\\.demo\\.).*"); // class name filter regex
        try {
            Class.forName("org.aspect.TestOnly").getPackage();
            AgentLoader.searchAndAttachAgent(
                    "org.demo.Launcher",
                    "org.aspect.agent.AgentMain",
                    options
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
