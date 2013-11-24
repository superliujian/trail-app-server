package com.jp.trailsrv;

import java.beans.PropertyVetoException;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import com.jp.trailsrv.model.Comment;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class Database {
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
	}
	
	/**
	 * Adds a comment into the database.
	 * @param comment
	 * 		the comment
	 * @throws SQLException if a database error occurs
	 */
	public void addComment(Comment comment) throws SQLException {
		// Insert a new comment using prepared statements
		try (Connection conn = source.getConnection(); 
				PreparedStatement ps = conn.prepareStatement("INSERT INTO comment (lat, lng, body) VALUES(?, ?, ?)")) {
			ps.setBigDecimal(1, comment.latitude);
			ps.setBigDecimal(2, comment.longitude);
			ps.setString(3, comment.body);
			ps.executeUpdate();
		}
	}
	
	/**
	 * Loads all comments from the database into a stream.
	 * @return all comments stored in the database
	 * @throws SQLException if a database error occurs
	 */
	public void loadComments(OutputStream out) throws SQLException {
		try (Connection conn = source.getConnection();
				PreparedStatement ps = conn.prepareStatement("SELECT * FROM comment")) {
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				Comment comment = new Comment(rs.getBigDecimal("lat"), rs.getBigDecimal("lng"), rs.getString("body"));
				// writer.append(comment.asXml());
			}
		}
	}
}
