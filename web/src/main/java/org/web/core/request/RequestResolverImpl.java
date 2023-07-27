package org.web.core.request;

import org.tools.ClassFinder;
import org.tools.Log;
import org.web.WebConfig;
import org.web.data.RouteHandler;

import java.util.HashMap;
import java.util.Map;

public class RequestResolverImpl implements RequestResolver {
    private static final Log logger = Log.getInstance(RequestResolverImpl.class);
    private Map<String, RouteHandler> routeHandlerMap = new HashMap<>();
    private long lastUpdateTimeStamp = 0;
    private long updateCount = 0;

    public RequestResolverImpl(){
        update(true);
    }

    @Override
    public void autoConfigure() {
        update(true);
    }

    public void update(boolean force){
        long lastUpdate = ClassFinder.getLastUpdateTimeStamp();
        if (lastUpdateTimeStamp < lastUpdate && ClassFinder.isInitialized() || force) {
            updateCount++;
            lastUpdateTimeStamp = lastUpdate;
            prepareRouteHandlers();
        }
    }

    public Map<String, RouteHandler> getRouteHandlers(){
        update(false);
        return routeHandlerMap;
    }

    /*----------------------------- Inner methods -------------------------------*/
    private void prepareRouteHandlers(){
        logger.debug("Scanning for route handlers for the ["+updateCount+"] time(s) ... ");
        long duration = System.currentTimeMillis();
        routeHandlerMap = WebConfig
                .getControllerProcessor(null)
                .getRouteHandlers();
        logger.debug("Scanning for route handlers ended in ["+(System.currentTimeMillis() - duration)+"] Millis " +
                "with "+ routeHandlerMap.size() +" handler(s)");
        if(routeHandlerMap.isEmpty())
            logger.error(
                    "No controller found in project"
            );
    }
}
