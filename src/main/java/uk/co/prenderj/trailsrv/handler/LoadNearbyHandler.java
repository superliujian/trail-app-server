package uk.co.prenderj.trailsrv.handler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Function;

import uk.co.prenderj.trailsrv.csv.CommentWriter;
import uk.co.prenderj.trailsrv.model.Comment;
import uk.co.prenderj.trailsrv.storage.DataSource;
import uk.co.prenderj.trailsrv.util.Log;
import uk.co.prenderj.trailsrv.util.SimpleTimer;

import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public class LoadNearbyHandler extends BaseHandler {
    private DataSource dataSource;
    private float radius;
    
    public LoadNearbyHandler(DataSource dataSource, float radius) {
        super("/nearby", "GET");
        this.dataSource = dataSource;
        this.radius = radius; // TODO Convert to KM
    }
    
    @Override
    public void call(final HttpExchange ex) throws Exception {
        try {
            // Grab the last two segments of the URI as longitude and latitude
            // e.g. www.example.com/nearby/25/-2.5
            URI uri = ex.getRequestURI();
            String[] segments = uri.getPath().split("/");
            writeResponse(ex, Double.parseDouble(segments[2]), Double.parseDouble(segments[3]));
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            ex.sendResponseHeaders(400, 0); // Bad request
        } catch (TimeoutException e) {
            Log.e("Timeout when loading comments", e);
            ex.sendResponseHeaders(504, 0); // Gateway timeout
        }
    }

    private void writeResponse(final HttpExchange ex, double lat, double lng) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        ex.getResponseHeaders().set("Content-Type", "text/csv");
        ex.sendResponseHeaders(200, 0);
        
        dataSource.selectNearbyComments(lat, lng, radius, new Function<ResultSet, Void>() {
            @Override
            public Void apply(ResultSet rs) {
                try (CommentWriter writer = new CommentWriter(new OutputStreamWriter(ex.getResponseBody()))) {
                    while (rs.next()) {
                        writer.writeNextComment(new Comment(rs.getInt("comment_id"), rs.getDouble("lat"), rs.getDouble("lng"), rs.getString("title"), rs.getString("body"), rs.getLong("attachment_id"), rs.getTimestamp("timestamp")));
                    }
                    return null;
                } catch (IOException | SQLException e) {
                    throw new RuntimeException(e);
                } 
            }
        }).get(getTimeoutRemaining(), TimeUnit.MILLISECONDS);
    }
}
