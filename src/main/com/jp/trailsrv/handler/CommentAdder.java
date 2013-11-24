package com.jp.trailsrv.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.jp.trailsrv.Server;
import com.jp.trailsrv.model.Comment;
import com.jp.trailsrv.util.Log;
import com.jp.trailsrv.util.PostParams;
import com.sun.net.httpserver.HttpExchange;

public class CommentAdder extends BaseHandler {
	public CommentAdder(Server srv) {
		super(srv, "/comment/add");
	}

	@Override
	public void call(HttpExchange ex) throws IOException {
		try {
			PostParams params = new PostParams(ex);
			if (!params.containsKeys("lat", "lng", "body")) {
				throw new IllegalArgumentException("Missing required argument");
			}
			BigDecimal lat = new BigDecimal(params.get("lat"));
			BigDecimal lng = new BigDecimal(params.get("lng"));
			String body = params.get("body");
			
			if (getServer().getDatabase().addComment(new Comment(lat, lng, body)).get(10, TimeUnit.SECONDS) == null) {
				// TODO Send success message
				Log.v("Successfully added comment");
				getServer().getCommentCache().rebuild(getServer().getDatabase()); // XXX Replace this with an append
			}
		} catch (SQLException | IllegalArgumentException | InterruptedException | ExecutionException e) {
			Log.e("Failed to add comment", e);
			// TODO Send error message
			// TODO Do not include SQL error details in response
		} catch (TimeoutException e) {
			Log.e("Timeout when adding comment", e);
			// TODO Send timeout message
		}
	}
}
