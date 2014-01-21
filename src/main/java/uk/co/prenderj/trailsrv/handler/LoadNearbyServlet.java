package uk.co.prenderj.trailsrv.handler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Charsets;
import com.google.common.base.Function;

import uk.co.prenderj.trailsrv.TrailServer;
import uk.co.prenderj.trailsrv.csv.CommentWriter;
import uk.co.prenderj.trailsrv.model.Attachment;
import uk.co.prenderj.trailsrv.model.Comment;
import uk.co.prenderj.trailsrv.storage.DataSource;
import uk.co.prenderj.trailsrv.util.Log;

import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public class LoadNearbyServlet extends BaseServlet {
    private float radius;
    
    public LoadNearbyServlet(TrailServer srv, float radius) {
        super(srv);
        this.radius = radius; // TODO Convert to KM
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Log.v("LoadNearby called: " + req.getRequestURI());
            
            // Grab the last two segments of the URI as longitude and latitude
            // e.g. www.example.com/nearby/25/-2.5
            String uri = req.getRequestURI();
            String[] segments = uri.split("/");
            writeResponse(resp, Double.parseDouble(segments[2]), Double.parseDouble(segments[3]));
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (TimeoutException e) {
            Log.e("Timeout when loading comments", e);
            resp.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
        } catch (InterruptedException | ExecutionException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void writeResponse(final HttpServletResponse resp, double lat, double lng) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        resp.setContentType("text/csv");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        
        getServer().getDatabase().selectNearbyComments(lat, lng, radius, new Function<ResultSet, Void>() {
            @Override
            public Void apply(ResultSet rs) {
                try (CommentWriter writer = new CommentWriter(new OutputStreamWriter(resp.getOutputStream(), Charsets.UTF_8))) {
                    while (rs.next()) {
                        writer.writeNextComment(new Comment(rs.getInt("comment_id"), rs.getDouble("lat"), rs.getDouble("lng"), rs.getString("title"), rs.getString("body"), new Attachment(rs.getLong("attachment_id")), rs.getTimestamp("timestamp")));
                    }
                    return null;
                } catch (IOException | SQLException e) {
                    throw new RuntimeException(e);
                } 
            }
        }).get(5, TimeUnit.SECONDS);
    }
}
