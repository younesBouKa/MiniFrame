package org.web.servlets;

import org.tools.Log;
import org.web.server.config.ServerConfig;
import org.web.server.config.ServletConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
public class ServiceServlet extends HttpServlet {
    private static final Log logger = Log.getInstance(ServiceServlet.class);

    @Override
    public void init() throws ServletException {
        System.out.println("Service Servlet initialized");
        super.init();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();

        writer.println("<html><title>Welcome</title><body>");
        writer.println("<h1>Have a Great Day!</h1>");
        writer.println("</body></html>");
    }

    public static ServletConfig getServiceServletConfig(){
        ServletConfig servletConfig = new ServletConfig();
        servletConfig.setServletName("Service");
        servletConfig.setServletClass(ServiceServlet.class.getCanonicalName());
        servletConfig.setLoadOnStartup(1);
        servletConfig.setUrlPattern("/service");
        return servletConfig;
    }

    public static ServerConfig getServiceServerConfig(){
        ServerConfig serverConfig = new ServerConfig();
        // add demo servlet config
        ServletConfig servletConfig = getServiceServletConfig();
        serverConfig.addServletConfig(servletConfig);
        return serverConfig;
    }
}
