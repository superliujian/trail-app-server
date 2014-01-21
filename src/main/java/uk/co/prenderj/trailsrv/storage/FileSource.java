package uk.co.prenderj.trailsrv.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.time.DateUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;

/**
 * Data source for uploaded files.
 * @author Joshua Prendergast
 */
public class FileSource {
    private String bucketName;
    private AmazonS3 conn;
    
    public FileSource(Configuration config) {
        AWSCredentials credentials = new BasicAWSCredentials(config.getString("S3AccessKey"), config.getString("S3SecretKey"));
        
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTPS);

        conn = new AmazonS3Client(credentials, clientConfig);
        
        // Check for configuration end-point (not required)
        String endPoint = config.getString("S3Endpoint");
        if (endPoint != null) {
            conn.setEndpoint(endPoint);
        }
        
        bucketName = config.getString("S3BucketName");
    }
    
    /**
     * Stores the stream under the given file name and access rights.
     * @param filename the filename
     * @param is the input stream
     * @param md the metadata. Must contain content-length
     * @param access the access rights
     */
    public void storeFile(String filename, InputStream is, ObjectMetadata md, CannedAccessControlList access) {
        conn.putObject(bucketName, filename, is, md);
        conn.setObjectAcl(bucketName, filename, access);
    }
    
    public void storeFile(File file, ObjectMetadata md, CannedAccessControlList access) throws FileNotFoundException, IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            storeFile(file.getName(), is, md, access);
        }
    }
    
    public void deleteFile(String filename) {
        conn.deleteObject(bucketName, filename);
    }
    
    /**
     * Gets a timed URL which allows read access for the specified amount of time.
     * @param filename the file's name
     * @param expirationDelay the read time, in hours
     * @return the URL
     */
    public URL getSignedAccessUrl(String filename, int expirationDelay) {
        // Convert duration from hours into milliseconds
        Date expiration = DateUtils.addHours(new Date(), expirationDelay);
        return conn.generatePresignedUrl(bucketName, filename, expiration);
    }
}
