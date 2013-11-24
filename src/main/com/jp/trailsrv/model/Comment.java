package com.jp.trailsrv.model;

import java.math.BigDecimal;

public class Comment implements XML {
	public final BigDecimal latitude;
	public final BigDecimal longitude;
	public final String body;
	
	public Comment(BigDecimal latitude, BigDecimal longitude, String body) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.body = body;
	}

	@Override
	public String asXml() {
		// TODO Auto-generated method stub
		return null;
	}
}
