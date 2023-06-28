package org.web.remoting;

import org.tools.Log;
import org.web.server.EmbeddedServerFactory;
import org.web.server.TomcatEmbeddedServerFactory;
import org.web.server.config.ServerConfig;
import org.web.server.config.ServletConfig;
import org.web.servlets.ServiceServlet;

import java.io.File;

public class ServiceExporter {
    private static EmbeddedServerFactory embeddedServerFactory;
    private static ServerConfig serverConfig;
    private static final Log logger = Log.getInstance(ServiceExporter.class);

    public void expose(String serviceUrl, Class<?> serviceInterface, Object serviceInstance) {
        checkServerSetup();
        ServletConfig servletConfig = new ServletConfig();
        servletConfig.setServletName(serviceInterface.getCanonicalName());
        servletConfig.setServletInstance(new ServiceServlet());
        servletConfig.setUrlPattern(serviceUrl);
        serverConfig.addServletConfig(servletConfig);
    }

    /*---------------------------- helper methods ----------------------------------*/
    private void checkServerSetup() {
        if (embeddedServerFactory == null || serverConfig == null) {
            embeddedServerFactory = new TomcatEmbeddedServerFactory();
            // create server config instance
            serverConfig = new ServerConfig();
            serverConfig.setPort(8080);
            serverConfig.setContextPath("/services");
            serverConfig.setDocBase(new File(".").getAbsolutePath());
        }
    }

    public static void main(String[] args) {

    }
}
