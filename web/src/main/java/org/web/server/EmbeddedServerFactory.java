package org.web.server;

import javax.servlet.ServletException;

public interface EmbeddedServerFactory {
    void init();
    void init(ServerConfig config);

    void setPort(int port);
    void initContext(String contextPath, String docBase) throws ServletException;
    void setup(ServerConfig config);

    void addFilter(FilterConfig filterConfig);
    void removeFilter(FilterConfig filterConfig);

    void addListener(String listenerClass);
    void removeListener(String listenerClass);

    void addContextParam(String name, String value);
    void removeContextParam(String name);

    void addServlet(ServletConfig servletConfig);

    void addUser(String user, String password);
    void addRole(String user, String role);

    void start();
    void stop();
    void destroy();
}
