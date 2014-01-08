package uk.co.prenderj.trailsrv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import uk.co.prenderj.trailsrv.handler.BaseHandler;
import uk.co.prenderj.trailsrv.util.Log;

import com.sun.net.httpserver.HttpServer;

/**
 * Serves HTTP requests using threads.
 * @author Joshua Prendergast
 */
public class Server {
    private static final int MAX_SHUTDOWN_DELAY = 10;
    private Properties properties;
    private int port;
    private HttpServer srv;
    private DataSource database;

    public Server(Properties properties) throws RuntimeException {
    	initialize(properties);
    }
    
    /**
     * Creates a new instance which is not started and has no handlers.
     * @param port the port to listen on
     * @throws RuntimeException if the server failed to start
     * @throws IOException if the properties file cannot be accessed
     */
    public Server(String propertiesPath) throws RuntimeException, IOException {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(new File(propertiesPath))) {
        	properties.load(in);
        }
        initialize(properties);
    }
    
    private void initialize(Properties properties) {
    	try {
            this.port = Integer.valueOf(properties.getProperty("Port"));
            srv = HttpServer.create(new InetSocketAddress(port), 0);
            srv.setExecutor(createExecutor());

            Log.i("Connecting to database...");
            database = new DataSource(loadProperties());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected Executor createExecutor() {
        return Executors.newCachedThreadPool(); // TODO Tweak settings
    }

    protected Map<String, String> loadProperties() {
        // TODO Load from file
        Map<String, String> properties = new HashMap<>();
        properties.put("user", "root");
        properties.put("password", "");
        properties.put("jdbcUrl", "jdbc:mysql://localhost/trail");
        return properties;
    }

    /**
     * Starts the server.
     */
    public void start() {
        Log.i("Server listening on port " + port);
        srv.start();
    }

    public void stop() {
        Log.i("Stopping server...");
        srv.stop(MAX_SHUTDOWN_DELAY);
    }

    public void createContext(BaseHandler handler) {
        srv.createContext(handler.getContextPath(), handler);
    }

    public DataSource getDatabase() {
        return database;
    }
}
