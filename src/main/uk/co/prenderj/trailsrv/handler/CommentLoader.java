package uk.co.prenderj.trailsrv.handler;

import java.io.IOException;

import uk.co.prenderj.trailsrv.Server;
import uk.co.prenderj.trailsrv.net.HttpExchangeWrapper;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class CommentLoader extends BaseHandler {
    public CommentLoader(Server srv) {
        super(srv, "/comment/load", "GET");
    }

    @Override
    public void call(HttpExchangeWrapper ex) throws IOException {
        ex.setContentType("text/csv");
        ex.sendResponseHeaders(200);
        getServer().getCommentCache().write(ex.getResponseBody());
    }
}
