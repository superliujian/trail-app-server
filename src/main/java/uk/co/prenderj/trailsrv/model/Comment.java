package uk.co.prenderj.trailsrv.model;

import java.sql.Timestamp;

import uk.co.prenderj.trailsrv.storage.CommentSpec;

/**
 * Represents a user's comment.
 * @author Joshua Prendergast
 */
public class Comment extends CommentSpec {
    public final long id;
    
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
        super(latitude, longitude, title, body, timestamp);
        this.id = id;
    }
    
    /**
     * Creates a new comment with an attachment.
     * @param id the comment's unique ID in the database
     * @param latitude the latitude
     * @param longitude the longitude
     * @param title the title
     * @param body the main text
     * @param attachmentId the comment attachment
     * @param timestamp the date of creation
     */
    public Comment(long id, double latitude, double longitude, String title, String body, Attachment attachment, Timestamp timestamp) {
        super(latitude, longitude, title, body, attachment, timestamp);
        this.id = id;
    }
}
