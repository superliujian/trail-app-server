package com.jp.trailsrv.handler;

import java.io.IOException;

import com.jp.trailsrv.Server;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class CommentLoader extends BaseHandler {
	public CommentLoader(Server srv) {
		super(srv, "/comment/load");
	}

	@Override
	public void call(HttpExchange ex) throws IOException {
		if (ex.getRequestMethod().equals("GET")) {
			Headers headers = ex.getResponseHeaders();
			headers.set("Content-Type", "application/xml");
			ex.sendResponseHeaders(200, 0);
			getServer().getCommentCache().write(ex.getResponseBody());
		}
	}
}
