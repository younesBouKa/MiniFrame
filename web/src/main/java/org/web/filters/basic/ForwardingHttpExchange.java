package org.web.filters.basic;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

class ForwardingHttpExchange extends HttpExchange {
    private final HttpExchange inner;

    ForwardingHttpExchange(HttpExchange inner) {
        this.inner = inner;
    }

    @Override
    public Headers getRequestHeaders() {
        return inner.getRequestHeaders();
    }

    @Override
    public Headers getResponseHeaders() {
        return inner.getResponseHeaders();
    }

    @Override
    public URI getRequestURI() {
        return inner.getRequestURI();
    }

    @Override
    public String getRequestMethod() {
        return inner.getRequestMethod();
    }

    @Override
    public HttpContext getHttpContext() {
        return inner.getHttpContext();
    }

    @Override
    public void close() {
        inner.close();
    }

    @Override
    public InputStream getRequestBody() {
        return inner.getRequestBody();
    }

    @Override
    public OutputStream getResponseBody() {
        return inner.getResponseBody();
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
        inner.sendResponseHeaders(rCode, responseLength);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return inner.getRemoteAddress();
    }

    @Override
    public int getResponseCode() {
        return inner.getResponseCode();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return inner.getLocalAddress();
    }

    @Override
    public String getProtocol() {
        return inner.getProtocol();
    }

    @Override
    public Object getAttribute(String name) {
        return inner.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        inner.setAttribute(name, value);
    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {
        inner.setStreams(i, o);
    }

    @Override
    public HttpPrincipal getPrincipal() {
        return inner.getPrincipal();
    }
}