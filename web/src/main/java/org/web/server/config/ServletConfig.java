package org.web.server.config;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.Map;

public class ServletConfig {
    private String servletName;
    private String servletClass;
    private Servlet servletInstance;
    private String urlPattern;
    private Map<String, String> initParams = new HashMap<>();
    private int loadOnStartup = 1;
    private boolean enabled = true;
    private MultipartConfigElement multipartConfigElement;

    public Servlet getServletInstance() {
        return servletInstance;
    }

    public void setServletInstance(Servlet servletInstance) {
        this.servletInstance = servletInstance;
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public int getLoadOnStartup() {
        return loadOnStartup;
    }

    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public Map<String, String> getInitParams() {
        return initParams;
    }

    public void setInitParams(Map<String, String> initParams) {
        this.initParams = initParams;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public MultipartConfigElement getMultipartConfigElement() {
        return multipartConfigElement;
    }

    public void setMultipartConfigElement(MultipartConfigElement multipartConfigElement) {
        this.multipartConfigElement = multipartConfigElement;
    }

    /*--------------------------------------------------------*/
    public void addInitParam(String name, String value){
        if(initParams==null)
            initParams = new HashMap<>();
        initParams.put(name, value);
    }

    public void removeInitParam(String name){
        if(initParams!=null)
            initParams.remove(name);
    }
}
