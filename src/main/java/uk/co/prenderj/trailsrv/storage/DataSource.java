package uk.co.prenderj.trailsrv.storage;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.configuration.Configuration;

import uk.co.prenderj.trailsrv.model.Attachment;
import uk.co.prenderj.trailsrv.model.Comment;

import com.google.common.base.Function;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * The database controller.
 * @author Joshua Prendergast
 */
public class DataSource {
    private ExecutorService executor;
    private ComboPooledDataSource source;
    
    /**
     * Creates a new database connector instance.
     * @param properties the connection properties
     * @throws ClassNotFoundException if the MySQL JDBC driver is not found
     */
    public DataSource(Configuration config) throws ClassNotFoundException {
        setupConnector(config);
        executor = Executors.newCachedThreadPool();
    }
    
    protected void setupConnector(Configuration config) throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver"); // Load the driver
        
        // Disable C3P0 verbose logging
        Properties p = new Properties(System.getProperties());
        p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "WARNING");
        System.setProperties(p);
        
        // Setup the pooled connector
        source = new ComboPooledDataSource();
        try {
            source.setUser(config.getString("DbUser"));
            source.setPassword(config.getString("DbPassword"));
            source.setJdbcUrl(config.getString("DbUrl"));
            source.setDriverClass("com.mysql.jdbc.Driver");
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected <T> Future<T> queueTask(Callable<T> callable) {
        return executor.submit(callable);
    }
    
    /**
     * Adds a comment into the database.
     * @param spec the comment specification
     * @return a Future which contains the new comment
     */
    public Future<Comment> insertComment(final CommentSpec spec) {
        return queueTask(new Callable<Comment>() {
            @Override
            public Comment call() throws SQLException {
                // Insert a new comment using prepared statements
                try (Connection conn = source.getConnection();
                        PreparedStatement insert = conn.prepareStatement("INSERT INTO comment (lat, lng, title, body, attachment_id, timestamp) VALUES(?, ?, ?, ?, ?, ?)");
                        PreparedStatement select = conn.prepareStatement("SELECT LAST_INSERT_ID()")) {
                    insert.setDouble(1, spec.latitude);
                    insert.setDouble(2, spec.longitude);
                    insert.setString(3, spec.title);
                    insert.setString(4, spec.body);
                    insert.setTimestamp(6, spec.timestamp);
                    
                    if (spec.attachment == null) {
                        insert.setNull(5, Types.BIGINT);
                    } else {
                        insert.setLong(5, spec.attachment.id);
                    }
                    insert.executeUpdate();
                    
                    /**
                     * Read the last AUTO_INCREMENT value (comment_id) LAST_INSERT_ID() is updated on a per-connection basis
                     * Since it's one thread per connection at any one time, there shouldn't be any concurrency problems... right?
                     */
                    ResultSet rs = select.executeQuery();
                    rs.next();
                    return new Comment(rs.getInt(1), spec.latitude, spec.longitude, spec.title, spec.body, spec.timestamp);
                }
            }
        });
    }
    
    public Future<Attachment> insertAttachmentRecord() {
        return queueTask(new Callable<Attachment>() {
            @Override
            public Attachment call() throws Exception {
                try (Connection conn = source.getConnection();
                        PreparedStatement insert = conn.prepareStatement("INSERT INTO attachment () VALUES()");
                        PreparedStatement select = conn.prepareStatement("SELECT LAST_INSERT_ID()")) {
                    insert.executeUpdate();
                    
                    ResultSet rs = select.executeQuery();
                    rs.next();
                    return new Attachment(rs.getInt(1));
                }
            }
        });
    }
    
    public Future<Boolean> doesAttachmentExist(final long attachmentId) {
        return queueTask(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try (Connection conn = source.getConnection();
                        PreparedStatement select = conn.prepareStatement("SELECT attachment_id FROM attachment WHERE attachment_id = ?")) {
                    select.setLong(1, attachmentId);
                    
                    ResultSet rs = select.executeQuery();
                    return rs.next();
                }
            }
        });
    }
    
    /**
     * Loads nearby comments from the database for processing in a streaming fashion.
     * @param lat the origin latitude
     * @param lng the origin longitude
     * @param radius the search radius
     * @param proc the function to run on the ResultSet
     * @return a Future which returns null on success
     */
    public Future<?> selectNearbyComments(final double lat, final double lng, final double radius, final Function<ResultSet, Void> proc) {
        return queueTask(new Callable<Object>() {
            @Override
            public Collection<?> call() throws Exception {
                try (Connection conn = source.getConnection();
                        PreparedStatement ps = conn.prepareStatement("SELECT * FROM comment WHERE (lat - ?) * (lat - ?) + (lng - ?) * (lng - ?) < ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                    ps.setFetchSize(Integer.MIN_VALUE); // Retrieve row by row to avoid overloading heap
                    ps.setDouble(1, lat);
                    ps.setDouble(2, lat);
                    ps.setDouble(3, lng);
                    ps.setDouble(4, lng);
                    ps.setDouble(5, radius * radius);
                    
                    ResultSet rs = ps.executeQuery();
                    proc.apply(rs);
                    return null;
                }
            }
        });
    }
}
