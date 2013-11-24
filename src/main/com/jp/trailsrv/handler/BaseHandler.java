package com.jp.trailsrv.handler;

import java.io.IOException;

import com.jp.trailsrv.Server;
import com.jp.trailsrv.util.Log;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class BaseHandler implements HttpHandler {
	private Server srv;
	private String contextPath;
	
	public BaseHandler(Server srv, String contextPath) {
		this.srv = srv;
		this.contextPath = contextPath;
	}
	
	public abstract void call(HttpExchange ex) throws IOException;

	@Override
	public void handle(HttpExchange ex) throws IOException {
		Log.v(String.format("Request: method = %s, context = %s", ex.getRequestMethod(), getContextPath())); // Log request
		call(ex);
	}

	public final Server getServer() {
		return srv;
	}

	public final String getContextPath() {
		return contextPath;
	}
}
