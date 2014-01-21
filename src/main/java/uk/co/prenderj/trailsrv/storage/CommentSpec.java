package uk.co.prenderj.trailsrv.storage;

import java.sql.Timestamp;

import uk.co.prenderj.trailsrv.model.Attachment;

/**
 * The specification for creating a comment.
 * @author Joshua Prendergast
 */
public class CommentSpec {
    public final double latitude;
    public final double longitude;
    public final String title;
    public final String body;
    public final Attachment attachment;
    public final Timestamp timestamp;
    
    /**
     * Creates a new comment without an attachment.
     * @param latitude the latitude
     * @param longitude the longitude
     * @param title the title
     * @param body the main text
     * @param timestamp the date of creation
     */
    public CommentSpec(double latitude, double longitude, String title, String body, Timestamp timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.body = body;
        this.attachment = null;
        this.timestamp = timestamp;
    }
    
    /**
     * Creates a new comment with an attachment.
     * @param latitude the latitude
     * @param longitude the longitude
     * @param title the title
     * @param body the main text
     * @param attachment the attachment comment attachment
     * @param timestamp the date of creation
     */
    public CommentSpec(double latitude, double longitude, String title, String body, Attachment attachment, Timestamp timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.body = body;
        this.attachment = attachment;
        this.timestamp = (Timestamp) timestamp.clone();
    }
}
