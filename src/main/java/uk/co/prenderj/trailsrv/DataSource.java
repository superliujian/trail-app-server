package uk.co.prenderj.trailsrv;

import java.beans.PropertyVetoException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import uk.co.prenderj.trailshared.function.Processor;
import uk.co.prenderj.trailshared.function.Transformer;
import uk.co.prenderj.trailsrv.model.Comment;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DataSource {
    private ExecutorService executor;
    private ComboPooledDataSource source;

    /**
     * Creates a new database connector instance.
     * @param properties the connection properties
     * @throws ClassNotFoundException if the MySQL JDBC driver is not found
     */
    public DataSource(Map<String, String> properties) throws ClassNotFoundException {
        setupConnector(properties);
        executor = Executors.newCachedThreadPool();
    }

    protected void setupConnector(Map<String, String> properties) throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver"); // Load the driver

        // Disable C3P0 verbose logging
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
     * @param latitude the comment latitude
     * @param longitude the comment longitude
     * @param body the comment text
     * @param timestamp the comment date of receipt
     * @return a Future which contains the new comment
     */
    public Future<Comment> addComment(final BigDecimal latitude, final BigDecimal longitude, final String body, final Timestamp timestamp) {
        return executor.submit(new Callable<Comment>() {
            @Override
            public Comment call() throws SQLException {
                // Insert a new comment using prepared statements
                try (Connection conn = source.getConnection();
                        PreparedStatement insert = conn.prepareStatement("INSERT INTO comment (lat, lng, body, timestamp) VALUES(?, ?, ?, ?)");
                        PreparedStatement select = conn.prepareStatement("SELECT LAST_INSERT_ID()")) {
                    insert.setBigDecimal(1, latitude);
                    insert.setBigDecimal(2, longitude);
                    insert.setString(3, body);
                    insert.setTimestamp(4, timestamp);
                    insert.executeUpdate();

                    /**
                     * Read the last AUTO_INCREMENT value (comment_id)
                     * LAST_INSERT_ID() is updated on a per-connection basis
                     * Since it's one thread per connection at any one time,
                     * there shouldn't be any concurrency problems... right?
                     */
                    ResultSet rs = select.executeQuery();
                    rs.next();
                    return new Comment(rs.getInt(1), latitude, longitude, body, timestamp);
                }
            }
        });
    }

    /**
     * Loads nearby comments from the database for processing in a streaming
     * fashion.
     * @param lat the origin latitude
     * @param lng the origin longitude
     * @param radius the search radius
     * @param proc the processor to run on the ResultSet
     * @return a Future which returns null on success
     */
    public Future<?> findNearbyComments(final BigDecimal lat, final BigDecimal lng, final BigDecimal radius, final Processor<ResultSet> proc) {
        return executor.submit(new Callable<Object>() {
            @Override
            public Collection<?> call() throws Exception {
                try (Connection conn = source.getConnection();
                        PreparedStatement ps = conn.prepareStatement("SELECT * FROM comment WHERE (lat - ?) * (lat - ?) + (lng - ?) * (lng - ?) < ? * ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                    ps.setFetchSize(Integer.MIN_VALUE); // Retrieve row by row to avoid overloading heap
                    ps.setBigDecimal(1, lat);
                    ps.setBigDecimal(2, lat);
                    ps.setBigDecimal(3, lng);
                    ps.setBigDecimal(4, lng);
                    ps.setBigDecimal(5, radius);
                    ps.setBigDecimal(6, radius);

                    ResultSet rs = ps.executeQuery();
                    proc.call(rs);
                    return null;
                }
            }
        });
    }
}
