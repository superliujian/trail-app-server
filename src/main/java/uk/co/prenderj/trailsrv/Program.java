package uk.co.prenderj.trailsrv;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import com.amazonaws.AmazonClientException;

import uk.co.prenderj.trailsrv.handler.InsertServlet;
import uk.co.prenderj.trailsrv.handler.LoadNearbyServlet;
import uk.co.prenderj.trailsrv.handler.ViewAttachmentServlet;
import uk.co.prenderj.trailsrv.util.Log;

public class Program {
    private static TrailServer srv;
    
    /**
     * Main execution point.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            srv = new TrailServer("var/server.properties");
        } catch (ClassNotFoundException e) {
            Log.e("MySQL JDBC driver not found", e);
        } catch (ConfigurationException e) {
            Log.e("Invalid properties file", e);
        } catch (AmazonClientException e) {
            Log.e("Problem with Amazon integration", e);
        } catch (IOException e) {
            Log.e("Properties file not found", e);
        } 
        
        try {
            // Add servlets
            Configuration config = srv.getConfig();
            srv.addServlet(new InsertServlet(srv, config.getInt("MaxUploadFileSize")), "/comments");
            srv.addServlet(new LoadNearbyServlet(srv, config.getFloat("LoadRadiusKm")), "/nearby/*");
            srv.addServlet(new ViewAttachmentServlet(srv), "/attachments/*");
            srv.start();
        } catch (Exception e) {
            Log.e("Uncaught exception in runtime", e);
        }
    }
}
