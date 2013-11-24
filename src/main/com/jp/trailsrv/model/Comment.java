package com.jp.trailsrv.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class Comment implements XML {
	public final BigDecimal latitude;
	public final BigDecimal longitude;
	public final String body;
	public final Date timestamp;
	
	public Comment(BigDecimal latitude, BigDecimal longitude, String body, Date timestamp) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.body = body;
		this.timestamp = timestamp;
	}
	
	public Comment(BigDecimal latitude, BigDecimal longitude, String body) {
		this(latitude, longitude, body, new Date());
	}

	@Override
	public void writeAsXml(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("comment");
		writer.writeAttribute("lat", latitude.toString());
		writer.writeAttribute("lng", longitude.toString());
		writer.writeAttribute("timestamp", timestamp.toString());
		writer.writeStartElement("body");
		writer.writeCharacters(body);
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
