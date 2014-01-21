package uk.co.prenderj.trailsrv;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.servlet.Servlet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle.Listener;

import uk.co.prenderj.trailsrv.storage.DataSource;
import uk.co.prenderj.trailsrv.storage.FileSource;
import uk.co.prenderj.trailsrv.util.Log;

/**
 * Serves HTTP requests.
 * @author Joshua Prendergast
 */
public class TrailServer {
    private Configuration config;
    private int port;
    private Server srv;
    private DataSource database;
    private FileSource fileSource;
    private ServletContextHandler ctx;
    private File tempDir;
    
    public TrailServer(Configuration config) throws IOException, ClassNotFoundException {
        this.config = config;
        
        this.port = config.getInt("Port");
        srv = new Server(port);
        
        Log.i("Connecting to database...");
        database = new DataSource(config);
        
        Log.i("Connecting file source...");
        fileSource = new FileSource(config);
        
        setupHandlers();
        
        tempDir = new File("./temp");
        if (!tempDir.exists() && !tempDir.mkdir()) {
            throw new IOException("Unable to create temp directory");
        }
    }
    
    /**
     * Creates a new instance which is not started and has no handlers.
     * @param port the port to listen on
     * @throws RuntimeException if the server failed to start
     * @throws IOException if the properties file cannot be accessed
     * @throws ConfigurationException if the properties file is invalid
     * @throws ClassNotFoundException if the JDBC driver cannot be found
     */
    public TrailServer(String configPath) throws ClassNotFoundException, IOException, ConfigurationException {
        this(new PropertiesConfiguration(configPath));
    }
    
    protected void setupHandlers() {
        // Setup servlet handler
        ctx = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ctx.setContextPath("/");
        
        // Setup request logger
        SimpleDateFormat format = new SimpleDateFormat("dd_MM_yyyy");
        NCSARequestLog requestLog = new NCSARequestLog(config.getString("LogFile", "./logs/" + format.format(new Date()) + ".request.log"));
        requestLog.setExtended(true);
        requestLog.setLogTimeZone("GMT");
        requestLog.setRetainDays(7);
        
        RequestLogHandler logHandler = new RequestLogHandler();
        logHandler.setRequestLog(requestLog);
        
        // Attach both handlers
        HandlerCollection handlers = new HandlerCollection();
        handlers.addHandler(ctx);
        handlers.addHandler(logHandler);
        srv.setHandler(handlers);
    }
    
    protected Executor createExecutor() {
        return Executors.newCachedThreadPool(); // TODO Tweak settings
    }
    
    /**
     * Starts the server.
     * @throws Exception if Jetty throws an exception
     */
    public void start() throws Exception {
        Log.i("Server listening on port " + port);
        srv.start();
    }
    
    public void stop() throws Exception {
        Log.i("Stopping server...");
        srv.stop();
    }
    
    public void addServlet(Servlet servlet, String pathSpec) {
        ctx.addServlet(new ServletHolder(servlet), pathSpec);
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
    
    public File getTempDirectory() {
        return tempDir;
    }
}
