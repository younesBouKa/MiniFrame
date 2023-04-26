package org.demo.Server;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import org.tools.Log;

import java.io.IOException;

public class CustomFilter extends Filter {
    private static final Log logger = Log.getInstance(CustomFilter.class);
    @Override
    public void doFilter(HttpExchange httpExchange, Chain chain) throws IOException {
        logger.debug("CustomFilter: "+httpExchange.getRequestURI());
        chain.doFilter(httpExchange);
    }

    @Override
    public String description() {
        return "CustomFilter";
    }
}
