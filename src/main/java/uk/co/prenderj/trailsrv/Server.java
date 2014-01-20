package uk.co.prenderj.trailsrv;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import uk.co.prenderj.trailsrv.handler.BaseHandler;
import uk.co.prenderj.trailsrv.storage.DataSource;
import uk.co.prenderj.trailsrv.storage.FileSource;
import uk.co.prenderj.trailsrv.util.Log;

import com.sun.net.httpserver.HttpServer;

/**
 * Serves HTTP requests.
 * @author Joshua Prendergast
 */
@SuppressWarnings("restriction")
public class Server {
    private static final int MAX_SHUTDOWN_DELAY = 10;
    private Configuration config;
    private int port;
    private HttpServer srv;
    private DataSource database;
    private FileSource fileSource;
    
    public Server(Configuration config) throws IOException, ClassNotFoundException {
        this.config = config;
        
        this.port = config.getInt("Port");
        srv = HttpServer.create(new InetSocketAddress(port), 0);
        srv.setExecutor(createExecutor());
        
        Log.i("Connecting to database...");
        database = new DataSource(config);
        
        Log.i("Connecting file source...");
        fileSource = new FileSource(config);
    }
    
    /**
     * Creates a new instance which is not started and has no handlers.
     * @param port the port to listen on
     * @throws RuntimeException if the server failed to start
     * @throws IOException if the properties file cannot be accessed
     * @throws ConfigurationException if the properties file is invalid
     * @throws ClassNotFoundException if the JDBC driver cannot be found
     */
    public Server(String configPath) throws ClassNotFoundException, IOException, ConfigurationException {
        this(new PropertiesConfiguration(configPath));
    }
    
    protected Executor createExecutor() {
        return Executors.newCachedThreadPool(); // TODO Tweak settings
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
    
    public FileSource getFileSource() {
        return fileSource;
    }
    
    public Configuration getConfig() {
        return config;
    }
}
