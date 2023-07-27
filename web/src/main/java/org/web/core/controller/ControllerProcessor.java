package org.web.core.controller;

import org.tools.annotations.AnnotationTools;
import org.web.annotations.others.Controller;
import org.web.core.AutoConfigurable;
import org.web.data.RouteHandler;

import java.util.Map;

public interface ControllerProcessor extends AutoConfigurable {
    Map<String, RouteHandler> getRouteHandlers();
    default boolean isValidControllerClass(Class aclass){
        Controller annotation = (Controller) AnnotationTools.getAnnotation(aclass, Controller.class);
        return  annotation!=null
                && annotation.root()!=null
                && !annotation.root().isEmpty();
    }
}

