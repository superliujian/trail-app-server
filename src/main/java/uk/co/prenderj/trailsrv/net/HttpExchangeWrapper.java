package uk.co.prenderj.trailsrv.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

public class HttpExchangeWrapper {
    private HttpExchange ex;
    
    public HttpExchangeWrapper(HttpExchange ex) {
        this.ex = ex;
    }
    
    public PostParams readPostParams() throws IOException {
        return new PostParams(ex.getRequestBody());
    }
    
    public boolean isPost() {
        return ex.getRequestMethod().equals("POST");
    }
    
    public boolean isGet() {
        return ex.getRequestMethod().equals("GET");
    }
    
    public void setContentType(String contentType) {
        getResponseHeaders().set("Content-Type", contentType);
    }

    public void close() {
        ex.close();
    }

    public Object getAttribute(String key) {
        return ex.getAttribute(key);
    }

    public HttpContext getHttpContext() {
        return ex.getHttpContext();
    }

    public InetSocketAddress getLocalAddress() {
        return ex.getLocalAddress();
    }

    public HttpPrincipal getPrincipal() {
        return ex.getPrincipal();
    }

    public String getProtocol() {
        return ex.getProtocol();
    }

    public InetSocketAddress getRemoteAddress() {
        return ex.getRemoteAddress();
    }

    public InputStream getRequestBody() {
        return ex.getRequestBody();
    }

    public Headers getRequestHeaders() {
        return ex.getRequestHeaders();
    }

    public String getRequestMethod() {
        return ex.getRequestMethod();
    }

    public URI getRequestURI() {
        return ex.getRequestURI();
    }

    public OutputStream getResponseBody() {
        return ex.getResponseBody();
    }

    public int getResponseCode() {
        return ex.getResponseCode();
    }

    public Headers getResponseHeaders() {
        return ex.getResponseHeaders();
    }

    @Override
	public int hashCode() {
        return ex.hashCode();
    }
    
    /**
     * Sends response headers with a content length of 0 (indicating chunked transfer).
     * @param status the status code
     * @throws IOException if a stream error occurs
     */
    public void sendResponseHeaders(int status) throws IOException {
        ex.sendResponseHeaders(status, 0);
    }

    public void sendResponseHeaders(int status, long contentLength) throws IOException {
        ex.sendResponseHeaders(status, contentLength);
    }

    public void setAttribute(String key, Object value) {
        ex.setAttribute(key, value);
    }

    public void setStreams(InputStream in, OutputStream out) {
        ex.setStreams(in, out);
    }

    @Override
	public String toString() {
        return ex.toString();
    }
    
    @Override
	public boolean equals(Object o) {
        return ex.equals(o);
    }
}
