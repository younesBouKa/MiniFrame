package org.web.servlets;

import org.tools.Log;
import org.tools.exceptions.FrameworkException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StaticServlet extends HttpServlet {
    private static final Log logger = Log.getInstance(StaticServlet.class);
    public static String STATIC_RESOURCE_FOLDER_PARAM_NAME = "staticResourceFolder";
    private String staticResourceFolder = "/WEB-INF/static";
    public static String PATH_PREFIX_PARAM_NAME = "pathPrefix";
    private String pathPrefix = "/static";

    @Override
    public void init() throws ServletException {
        logger.info("Static resource Servlet initialized");
        staticResourceFolder = getServletConfig().getInitParameter(STATIC_RESOURCE_FOLDER_PARAM_NAME);
        if(staticResourceFolder==null || staticResourceFolder.trim().isEmpty())
            staticResourceFolder = getServletContext().getInitParameter(STATIC_RESOURCE_FOLDER_PARAM_NAME);

        pathPrefix = getServletConfig().getInitParameter(PATH_PREFIX_PARAM_NAME);
        if(pathPrefix==null || pathPrefix.trim().isEmpty())
            pathPrefix = getServletContext().getInitParameter(PATH_PREFIX_PARAM_NAME);
        super.init();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.startsWith(pathPrefix))
            path = path.replace(pathPrefix,staticResourceFolder);
        RequestDispatcher requestDispatcher = req.getRequestDispatcher(path);
        if(requestDispatcher==null)
            throw new FrameworkException("Can't find request dispatcher for static resource : "+path);
        else
            requestDispatcher.forward(req, resp);
    }
}
