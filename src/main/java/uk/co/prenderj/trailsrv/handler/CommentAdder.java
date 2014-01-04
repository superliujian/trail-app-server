package uk.co.prenderj.trailsrv.handler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import uk.co.prenderj.trailsrv.Server;
import uk.co.prenderj.trailsrv.csv.CommentWriteProc;
import uk.co.prenderj.trailsrv.model.Comment;
import uk.co.prenderj.trailsrv.net.HttpExchangeWrapper;
import uk.co.prenderj.trailsrv.net.PostParams;
import uk.co.prenderj.trailsrv.util.Log;

import au.com.bytecode.opencsv.CSVWriter;


public class CommentAdder extends BaseHandler {
    public CommentAdder(Server srv) {
        super(srv, "/comments");
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
            
            // Response
            try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(ex.getResponseBody()))) {
                ex.setContentType("text/plain");
                ex.sendResponseHeaders(200, 0);
                new CommentWriteProc(comment).process(writer); // Respond with created resource
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
