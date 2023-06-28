package org.web.servlets;

import org.web.server.config.ServerConfig;
import org.web.server.config.ServletConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class DemoServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("Demo Servlet initialized");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();

        writer.println("<html><title>Welcome</title><body>");
        writer.println("<h1>Have a Great Day!</h1>");
        writer.println("</body></html>");
    }

    public static ServletConfig getDemoServletConfig(){
        ServletConfig demoServletConfig = new ServletConfig();
        demoServletConfig.setServletName("Demo");
        demoServletConfig.setServletClass(DemoServlet.class.getCanonicalName());
        demoServletConfig.setLoadOnStartup(1);
        demoServletConfig.setUrlPattern("/demo");
        return demoServletConfig;
    }

    public static ServerConfig getDemoServerConfig(){
        ServerConfig serverConfig = new ServerConfig();
        // add demo servlet config
        ServletConfig demoServletConfig = getDemoServletConfig();
        serverConfig.addServletConfig(demoServletConfig);
        return serverConfig;
    }
}
