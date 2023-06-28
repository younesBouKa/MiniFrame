package org.web.server.config;

import java.io.File;
import java.util.*;

public class ServerConfig {
    private Map<String,String> contextParams = new HashMap<>();
    private Set<String> listenerClasses = new HashSet<>();
    private List<ServletConfig> servletConfigList = new ArrayList<>();
    private List<FilterConfig> filterConfigList = new ArrayList<>();
    private int port = 8080;
    private String contextPath = "/app";
    private String welcomeFile = null;
    private boolean cookies = true;
    private int sessionTimeOut = 30;
    private String docBase = new File(".").getAbsolutePath();
    private Map<String,String> users = new HashMap<>();
    private Map<String,String> roles = new HashMap<>();

    public Map<String, String> getContextParams() {
        return contextParams;
    }

    public void setContextParams(Map<String, String> contextParams) {
        this.contextParams = contextParams;
    }

    public Set<String> getListenerClasses() {
        return listenerClasses;
    }

    public void setListenerClasses(Set<String> listenerClasses) {
        this.listenerClasses = listenerClasses;
    }

    public List<ServletConfig> getServletConfigList() {
        return servletConfigList;
    }

    public void setServletConfigList(List<ServletConfig> servletConfigList) {
        this.servletConfigList = servletConfigList;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    public List<FilterConfig> getFilterConfigList() {
        return filterConfigList;
    }

    public void setFilterConfigList(List<FilterConfig> filterConfigList) {
        this.filterConfigList = filterConfigList;
    }

    public String getWelcomeFile() {
        return welcomeFile;
    }

    public void setWelcomeFile(String welcomeFile) {
        this.welcomeFile = welcomeFile;
    }

    public boolean isCookies() {
        return cookies;
    }

    public void setCookies(boolean cookies) {
        this.cookies = cookies;
    }

    public int getSessionTimeOut() {
        return sessionTimeOut;
    }

    public void setSessionTimeOut(int sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

    public Map<String, String> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, String> roles) {
        this.roles = roles;
    }

    /*----------------------------------------------------------------*/
    public void addUser(String user, String password){
        if(users==null)
            users = new HashMap<>();
        users.put(user, password);
    }

    public void removeUser(String user){
        if(users!=null)
            users.remove(user);
    }

    public void addRole(String user, String role){
        if(roles==null)
            roles = new HashMap<>();
        roles.put(user, role);
    }

    public void removeRole(String user, String role){
        if(roles!=null)
            roles.remove(user);
    }

    public void addFilterConfig(FilterConfig filterConfig){
        if(filterConfigList==null)
            filterConfigList = new ArrayList<>();
        filterConfigList.add(filterConfig);
    }

    public void removeFilterConfig(FilterConfig filterConfig){
        if(filterConfigList!=null)
            filterConfigList.remove(filterConfig);
    }

    public void addListener(String listenerClass){
        if(listenerClasses==null)
            listenerClasses = new HashSet<>();
        listenerClasses.add(listenerClass);
    }

    public void removeListener(String listenerClass){
        if(listenerClasses!=null)
            listenerClasses.remove(listenerClass);
    }

    public void addContextParam(String name, String value){
        if(contextParams==null)
            contextParams = new HashMap<>();
        contextParams.put(name, value);
    }

    public void removeContextParam(String name){
        if(contextParams!=null)
            contextParams.remove(name);
    }

    public void addServletConfig(ServletConfig servletConfig){
        if(servletConfigList==null)
            servletConfigList = new ArrayList<>();
        if(servletConfig.getServletName()==null)
            if(servletConfig.getServletClass()!=null)
                servletConfig.setServletName(servletConfig.getServletClass().trim());
        if(servletConfig.getUrlPattern()==null)
            if(servletConfig.getServletClass()!=null)
                servletConfig.setUrlPattern("/"+servletConfig.getServletClass().trim());
        servletConfigList.add(servletConfig);
    }

    public void removeServletConfig(ServletConfig servletConfig){
        if(servletConfigList!=null)
            servletConfigList.remove(servletConfig);
    }

}
