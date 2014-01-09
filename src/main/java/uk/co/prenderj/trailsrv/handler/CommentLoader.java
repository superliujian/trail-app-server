package uk.co.prenderj.trailsrv.handler;

import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;

import au.com.bytecode.opencsv.CSVWriter;
import uk.co.prenderj.trailshared.function.Processor;
import uk.co.prenderj.trailsrv.Server;
import uk.co.prenderj.trailsrv.csv.CommentWriteProc;
import uk.co.prenderj.trailsrv.model.Comment;
import uk.co.prenderj.trailsrv.net.HttpExchangeWrapper;

public class CommentLoader extends BaseHandler {
	private static final BigDecimal RADIUS = new BigDecimal("0.50");

	public CommentLoader(Server srv) {
		super(srv, "/comments/nearby", "GET");
	}

	@Override
	public void call(HttpExchangeWrapper ex) throws Exception {
		try {
			// Grab the last two segments of the URI as longitude and latitude
			// e.g. www.example.com/comments/nearby/25/-2.5
			URI uri = ex.getRequestURI();
			String[] segments = uri.getPath().split("/");
			BigDecimal lat = new BigDecimal(segments[3]);
			BigDecimal lng = new BigDecimal(segments[4]);

			// Response
			try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(ex.getResponseBody()))) {
				ex.setContentType("text/csv");
				ex.sendResponseHeaders(200);
				getServer().getDatabase().findNearbyComments(lat, lng, RADIUS, new Processor<ResultSet>() {
					@Override
					public void call(ResultSet rs) throws RuntimeException {
						CommentWriteProc proc = new CommentWriteProc();
						try {
							while (rs.next()) {
								proc.setComment(new Comment(rs.getInt("comment_id"), rs.getBigDecimal("lat"), rs.getBigDecimal("lng"), rs.getString("body"), rs.getTimestamp("timestamp")));
							}
						} catch (SQLException e) {
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
