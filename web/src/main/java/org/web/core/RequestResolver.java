package org.web.core;

import org.web.data.RouteHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface RequestResolver {
    Map<String, RouteHandler> getRouteHandlers();
    default RouteHandler resolveRouteHandler(HttpServletRequest request){
        String httpMethod = request.getMethod();
        String uri = request.getPathInfo();
        RouteHandler foundRouteHandler = null;
        for (RouteHandler handler: getRouteHandlers().values()) {
            if(!handler.getMethodHttp().equals(httpMethod))
                continue;
            String handlerPath = handler.getRootPath()+handler.getMethodInfo().getPath();
            String[] handlerPathParts = handlerPath.split("//");
            String[] requestPathParts = uri.split("//");
            if(handlerPathParts.length != requestPathParts.length)
                continue;
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
            if(pathsFits){
                foundRouteHandler = handler;
                break;
            }
        }
        return foundRouteHandler;
    }

}
