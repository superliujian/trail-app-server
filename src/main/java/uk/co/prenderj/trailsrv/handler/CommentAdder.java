package uk.co.prenderj.trailsrv.handler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import uk.co.prenderj.trailsrv.DataSource;
import uk.co.prenderj.trailsrv.csv.CommentWriter;
import uk.co.prenderj.trailsrv.model.Comment;
import uk.co.prenderj.trailsrv.net.HttpExchangeWrapper;
import uk.co.prenderj.trailsrv.net.PostParams;
import uk.co.prenderj.trailsrv.util.Log;
import uk.co.prenderj.trailsrv.util.Util;

public class CommentAdder extends BaseHandler {
    private DataSource dataSource;
    
    public CommentAdder(DataSource dataSource) {
        super("/comments", "POST");
        this.dataSource = dataSource;
    }
    
    @Override
    public void call(HttpExchangeWrapper ex) throws Exception {
        try {
            PostParams params = new PostParams(ex.getRequestBody());
            if (!params.containsKeys("lat", "lng", "title", "body")) {
                throw new IllegalArgumentException("Missing required argument");
            }
            double lat = Double.parseDouble(params.get("lat"));
            double lng = Double.parseDouble(params.get("lng"));
            String title = params.get("title");
            String body = params.get("body"); // TODO Limit size
            
            // Store comment in database and cache
            Comment comment = dataSource.addComment(lat, lng, title, body, new Timestamp(new Date().getTime())).get(5, TimeUnit.SECONDS);
            Log.v(String.format("Successfully added comment: '%s'", Util.preview(body, 25)));
            
            // Response
            ex.setContentType("text/csv");
            ex.sendResponseHeaders(200);
            try (CommentWriter writer = new CommentWriter(new OutputStreamWriter(ex.getResponseBody()))) {
                writer.writeNextComment(comment);
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
