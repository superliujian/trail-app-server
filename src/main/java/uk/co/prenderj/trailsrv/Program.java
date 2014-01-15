package uk.co.prenderj.trailsrv;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;

import uk.co.prenderj.trailsrv.handler.CommentAdder;
import uk.co.prenderj.trailsrv.handler.CommentLoader;
import uk.co.prenderj.trailsrv.util.Log;

public class Program {
    private static Server srv;
    
    /**
     * Main execution point.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            srv = new Server(args.length > 0 ? args[0] : "cfg/server.properties"); // Override the properties path by providing an argument
        } catch (ClassNotFoundException e) {
            Log.e("MySQL JDBC driver not found", e);
        } catch (ConfigurationException e) {
            Log.e("Invalid properties file", e);
        } catch (IOException e) {
            Log.e("Properties file not found", e);
        } 
        
        try {
            srv.createContext(new CommentAdder(srv.getDatabase()));
            srv.createContext(new CommentLoader(srv.getDatabase(), srv.getConfig().getFloat("LoadRadiusKm")));
            srv.start();
        } catch (Exception e) {
            Log.e("Uncaught exception in runtime", e);
        }
    }
    
    /**
     * Stops the running server. Blocks until terminated.
     */
    public static void stop() {
        if (srv != null) {
            srv.stop();
            srv = null;
        }
    }
}
