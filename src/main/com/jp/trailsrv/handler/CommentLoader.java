package com.jp.trailsrv.handler;

import java.io.IOException;

import com.jp.trailsrv.Server;
import com.sun.net.httpserver.HttpExchange;

public class CommentLoader extends BaseHandler {
	public CommentLoader(Server srv) {
		super(srv, "/comment/load");
	}

	@Override
	public void call(HttpExchange ex) throws IOException {
		// TODO Auto-generated method stub

	}
}
