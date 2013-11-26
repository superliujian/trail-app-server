package com.jp.trailsrv.handler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import au.com.bytecode.opencsv.CSVWriter;

import com.jp.trailsrv.Server;
import com.jp.trailsrv.csv.CommentWriteProc;
import com.jp.trailsrv.model.Comment;
import com.jp.trailsrv.net.HttpExchangeWrapper;
import com.jp.trailsrv.net.PostParams;
import com.jp.trailsrv.util.Log;

public class CommentAdder extends BaseHandler {
    public CommentAdder(Server srv) {
        super(srv, "/comment/add");
    }

    @Override
    public void call(HttpExchangeWrapper ex) throws IOException {
        try {
            PostParams params = new PostParams(ex.getRequestBody());
            if (!params.containsKeys("lat", "lng", "body")) {
                throw new IllegalArgumentException("Missing required argument");
            }
            BigDecimal lat = new BigDecimal(params.get("lat"));
            BigDecimal lng = new BigDecimal(params.get("lng"));
            String body = params.get("body");
            
            // Store comment in database and cache
            Comment comment = getServer().getDatabase().addComment(lat, lng, body, new Timestamp(new Date().getTime())).get(10, TimeUnit.SECONDS);

            Log.v("Successfully added comment");
            getServer().getCommentCache().append(comment);
            
            // Response
            try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(ex.getResponseBody()))) {
                ex.setContentType("text/plain");
                ex.sendResponseHeaders(200, 0);
                writer.write(new CommentWriteProc(comment)); // Respond with created resource
            }
        } catch (IllegalArgumentException e) {
            ex.sendResponseHeaders(400); // Bad request
        } catch (InterruptedException | ExecutionException | IOException e) {
            Log.e("Failed to add comment", e);
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            Log.e("Timeout when adding comment", e);
            ex.sendResponseHeaders(504); // Gateway timeout
        }
    }
}
