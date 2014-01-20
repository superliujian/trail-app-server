package uk.co.prenderj.trailsrv.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import uk.co.prenderj.trailsrv.util.Log;
import uk.co.prenderj.trailsrv.util.SimpleTimer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

@SuppressWarnings("restriction")
public abstract class BaseHandler implements HttpHandler {
    private String contextPath;
    private List<String> acceptedMethods;
    private SimpleTimer timeout = SimpleTimer.getCompletedTimer(15 * 1000);
    
    public BaseHandler(String contextPath, String... acceptedMethods) {
        this.contextPath = contextPath;
        this.acceptedMethods = Arrays.asList(acceptedMethods);
    }
    
    public BaseHandler(String contextPath, SimpleTimer timeout, String... acceptedMethods) {
        this(contextPath, acceptedMethods);
        this.timeout = timeout;
    }
    
    public abstract void call(HttpExchange ex) throws Exception;
    
    @Override
    public void handle(HttpExchange ex) throws IOException {
        Log.v(String.format("Request: method = %s, context = %s", ex.getRequestMethod(), getContextPath()));
        if (acceptedMethods.contains(ex.getRequestMethod())) {
            try {
                resetTimeout();
                call(ex);
            } catch (Exception e) {
                Log.e("Uncaught exception in handler '" + getClass().getName() + "'", e);
                ex.sendResponseHeaders(500, 0); // Internal server error
            } finally {
                ex.close();
            }
        }
    }
    
    public void resetTimeout() {
        timeout.reset();
    }
    
    public long getTimeoutRemaining() {
        return timeout.getTimeRemaining();
    }
    
    public void setTimeout(SimpleTimer timeout) {
        this.timeout = timeout;
    }
    
    public SimpleTimer getTimeout() {
        return timeout;
    }
    
    public final String getContextPath() {
        return contextPath;
    }
}
