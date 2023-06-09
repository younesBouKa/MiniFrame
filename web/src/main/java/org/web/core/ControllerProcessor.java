package org.web.core;

import com.sun.net.httpserver.HttpHandler;
import org.tools.annotations.AnnotationTools;
import org.web.annotations.others.Controller;
import org.web.data.RouteHandler;

import java.util.Map;

public interface ControllerProcessor {
    Map<String, HttpHandler> getHttpHandlers();
    Map<String, RouteHandler> getRouteHandlers();
    default boolean isValidControllerClass(Class aclass){
        Controller annotation = (Controller) AnnotationTools.getAnnotation(aclass, Controller.class);
        return  annotation!=null
                && annotation.root()!=null
                && !annotation.root().isEmpty();
    }
}

