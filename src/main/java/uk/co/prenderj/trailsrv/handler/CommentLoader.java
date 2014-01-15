package uk.co.prenderj.trailsrv.handler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;

import au.com.bytecode.opencsv.CSVWriter;
import uk.co.prenderj.trailsrv.DataSource;
import uk.co.prenderj.trailsrv.Server;
import uk.co.prenderj.trailsrv.csv.CommentWriter;
import uk.co.prenderj.trailsrv.model.Comment;
import uk.co.prenderj.trailsrv.net.HttpExchangeWrapper;
import uk.co.prenderj.trailsrv.util.Processor;

public class CommentLoader extends BaseHandler {
    private DataSource dataSource;
    private float radius;
    
    public CommentLoader(DataSource dataSource, float radius) {
        super("/nearby", "GET");
        this.dataSource = dataSource;
        this.radius = radius; // TODO Convert to KM
    }
    
    @Override
    public void call(final HttpExchangeWrapper ex) throws Exception {
        try {
            // Grab the last two segments of the URI as longitude and latitude
            // e.g. www.example.com/nearby/25/-2.5
            URI uri = ex.getRequestURI();
            String[] segments = uri.getPath().split("/");
            double lat = Double.parseDouble(segments[2]);
            double lng = Double.parseDouble(segments[3]);
            
            // Response
            try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(ex.getResponseBody()))) {
                ex.setContentType("text/csv");
                ex.sendResponseHeaders(200);
                dataSource.findNearbyComments(lat, lng, radius, new Processor<ResultSet>() {
                    @Override
                    public void call(ResultSet rs) throws RuntimeException {
                        try (CommentWriter writer = new CommentWriter(new OutputStreamWriter(ex.getResponseBody()))) {
                            writer.writeNextComment(new Comment(rs.getInt("comment_id"), rs.getDouble("lat"), rs.getDouble("lng"), rs.getString("title"), rs.getString("body"), rs.getTimestamp("timestamp")));
                        } catch (SQLException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            ex.sendResponseHeaders(400); // Bad request
        }
    }
}
