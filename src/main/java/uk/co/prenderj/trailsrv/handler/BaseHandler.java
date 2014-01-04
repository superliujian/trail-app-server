package uk.co.prenderj.trailsrv.handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import uk.co.prenderj.trailsrv.Server;
import uk.co.prenderj.trailsrv.net.HttpExchangeWrapper;
import uk.co.prenderj.trailsrv.util.Log;

import com.google.common.collect.Iterables;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class BaseHandler implements HttpHandler {
    private Server srv;
    private String contextPath;
    private List<String> acceptedMethods;

    public BaseHandler(Server srv, String contextPath, String... acceptedMethods) {
        this.srv = srv;
        this.contextPath = contextPath;
        this.acceptedMethods = Arrays.asList(acceptedMethods);
    }

    public abstract void call(HttpExchangeWrapper ex) throws IOException, RuntimeException;

    @Override
    public void handle(HttpExchange ex) throws IOException {
        Log.v(String.format("Request: method = %s, context = %s", ex.getRequestMethod(), getContextPath()));
        if (acceptedMethods.contains(ex.getRequestMethod())) {
            HttpExchangeWrapper wrapper = new HttpExchangeWrapper(ex);
            try {
                call(wrapper);
            } catch (IOException | RuntimeException e) {
                wrapper.sendResponseHeaders(400); // Internal server error
            } finally {
                wrapper.close();
            }
        }
    }

    public final Server getServer() {
        return srv;
    }

    public final String getContextPath() {
        return contextPath;
    }
}
