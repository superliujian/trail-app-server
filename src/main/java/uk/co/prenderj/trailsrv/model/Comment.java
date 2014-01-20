package uk.co.prenderj.trailsrv.model;

import java.sql.Timestamp;

/**
 * Represents a user's comment.
 * @author Joshua Prendergast
 */
public class Comment {
    public final long id;
    public final double latitude;
    public final double longitude;
    public final String title;
    public final String body;
    public final long attachmentId;
    public final Timestamp timestamp;
    
    /**
     * Creates a new comment without an attachment.
     * @param id the comment's unique ID in the database
     * @param latitude the latitude
     * @param longitude the longitude
     * @param title the title
     * @param body the main text
     * @param timestamp the date of creation
     */
    public Comment(long id, double latitude, double longitude, String title, String body, Timestamp timestamp) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.body = body;
        this.attachmentId = -1;
        this.timestamp = timestamp;
    }
    
    /**
     * Creates a new comment with an attachment.
     * @param id the comment's unique ID in the database
     * @param latitude the latitude
     * @param longitude the longitude
     * @param title the title
     * @param body the main text
     * @param attachmentId the attachment's unique ID in the database
     * @param timestamp the date of creation
     */
    public Comment(long id, double latitude, double longitude, String title, String body, long attachmentId, Timestamp timestamp) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.body = body;
        this.attachmentId = attachmentId;
        this.timestamp = (Timestamp) timestamp.clone();
    }
}
