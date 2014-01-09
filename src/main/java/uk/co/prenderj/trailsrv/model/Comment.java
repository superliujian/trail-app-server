package uk.co.prenderj.trailsrv.model;

import java.sql.Timestamp;

public class Comment {
    public final long id;
    public final double latitude;
    public final double longitude;
    public final String title;
    public final String body;
    public final Timestamp timestamp;

    public Comment(long id, double latitude, double longitude, String title, String body, Timestamp timestamp) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.body = body;
        this.timestamp = (Timestamp) timestamp.clone();
    }
}
