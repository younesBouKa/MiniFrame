package org.web.core;

import org.web.data.RouteHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface RequestResolver extends AutoConfigurable{
    Map<String, RouteHandler> getRouteHandlers();
    default RouteHandler resolveRouteHandler(HttpServletRequest request){
        RouteHandler foundRouteHandler = null;
        for (RouteHandler handler: getRouteHandlers().values()) {
            if(isHandlerMatchingRequest(request, handler)){
                foundRouteHandler = handler;
                break;
            }
        }
        return foundRouteHandler;
    }
    default boolean isHandlerMatchingRequest(HttpServletRequest request, RouteHandler handler){
        String httpMethod = request.getMethod();
        String uri = request.getPathInfo();
        if(!handler.getMethodHttp().equals(httpMethod))
            return false;
        String handlerPath = handler.getRootPath()+handler.getMethodInfo().getPath();
        String[] handlerPathParts = handlerPath.split("//");
        String[] requestPathParts = uri.split("//");
        if(handlerPathParts.length != requestPathParts.length)
            return false;
        boolean pathsFits = true;
        for(int i=0; i<handlerPathParts.length; i++){
            if(
                    (handlerPathParts[i].startsWith("{") && requestPathParts[i].isEmpty()) ||
                            (!handlerPathParts[i].startsWith("{") && !handlerPathParts[i].equals(requestPathParts[i]))
            ){
                pathsFits = false;
                break;
            }
        }
        return pathsFits;
    }

}
