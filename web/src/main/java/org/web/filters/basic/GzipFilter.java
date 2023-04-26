package org.web.filters.basic;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import static java.util.Arrays.asList;

public final class GzipFilter extends Filter {
    private static final Pattern COMMA_PATTERN = Pattern.compile("\\s*,\\s*");

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        final String acceptEncoding = exchange.getRequestHeaders().getFirst("Accept-Encoding");
        final List<String> acceptEncodings = asList(COMMA_PATTERN.split(acceptEncoding));
        chain.doFilter(
                acceptEncodings.contains("gzip")
                        ? new GzipHttpExchange(exchange)
                        : exchange
        );
    }

    @Override
    public String description() {
        return "GzipFilter";
    }

    private static class GzipHttpExchange extends ForwardingHttpExchange {

        private GZIPOutputStream wrappedResponseBody = null;

        GzipHttpExchange(HttpExchange inner) {
            super(inner);
        }

        @Override
        public OutputStream getResponseBody() {
            return wrappedResponseBody != null
                    ? wrappedResponseBody
                    : super.getResponseBody();
        }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
            getResponseHeaders().add("Vary", "Accept-Encoding");
            if (responseLength >= 0) {
                getResponseHeaders().add("Content-Encoding", "gzip");
                super.sendResponseHeaders(rCode, 0);
                wrappedResponseBody = new GZIPOutputStream(super.getResponseBody());
                return;
            }
            super.sendResponseHeaders(rCode, responseLength);
        }
    }
}