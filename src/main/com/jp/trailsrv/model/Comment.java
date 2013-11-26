package com.jp.trailsrv.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Comment {
	public final long id;
	public final BigDecimal latitude;
	public final BigDecimal longitude;
	public final String body;
	public final Timestamp timestamp;
	
	public Comment(long id, BigDecimal latitude, BigDecimal longitude, String body, Timestamp timestamp) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.body = body;
		this.timestamp = timestamp;
	}
}
