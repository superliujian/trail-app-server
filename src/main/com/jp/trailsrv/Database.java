package com.jp.trailsrv;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.jp.trailsrv.model.Comment;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class Database {
	private ExecutorService executor;
	private ComboPooledDataSource source;
	
	/**
	 * Creates a new database connector instance.
	 * @param properties
	 * 		the connection properties
	 * @throws ClassNotFoundException if the MySQL JDBC driver is not found
	 */
	public Database(Map<String, String> properties) throws ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver"); // Load the driver
		
		// Disable C3P0 spamming
		Properties p = new Properties(System.getProperties());
		p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
		p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");
		System.setProperties(p);
		
		// Setup the pooled connector
		source = new ComboPooledDataSource();
		try {
			source.setUser(properties.get("user"));
			source.setPassword(properties.get("password"));
			source.setJdbcUrl(properties.get("jdbcUrl"));
			source.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}
		executor = Executors.newCachedThreadPool();
	}
	
	/**
	 * Adds a comment into the database.
	 * @param comment
	 * 		the comment
	 * @return a Future which returns null on success
	 */
	public Future<?> addComment(final Comment comment) {
		return executor.submit(new Callable<Object>() {
			@Override
			public Object call() throws SQLException {
				// Insert a new comment using prepared statements
				try (Connection conn = source.getConnection(); 
						PreparedStatement ps = conn.prepareStatement("INSERT INTO comment (lat, lng, body, timestamp) VALUES(?, ?, ?, ?)")) {
					ps.setBigDecimal(1, comment.latitude);
					ps.setBigDecimal(2, comment.longitude);
					ps.setString(3, comment.body);
					ps.setTimestamp(4, new Timestamp(comment.timestamp.getTime()));
					ps.executeUpdate();
					return null;
				}
			}
		});
	}
	
	/**
	 * Loads all comments from the database into a cache.
	 * @param cache
	 * 		the XML comment cache
	 * @return a Future which returns all comments stored in the database on success
	 */
	public Future<Collection<Comment>> loadComments() {
		return executor.submit(new Callable<Collection<Comment>>() {
			@Override
			public Collection<Comment> call() throws Exception {
				List<Comment> out = new ArrayList<>();
				try (Connection conn = source.getConnection();
						PreparedStatement ps = conn.prepareStatement("SELECT * FROM comment")) {
					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
						Comment comment = new Comment(rs.getBigDecimal("lat"), rs.getBigDecimal("lng"), rs.getString("body"), rs.getTimestamp("timestamp"));
						out.add(comment);
					}
				}
				return out;
			}
		});
	}
}
