package com.jp.trailsrv.model;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public interface XML {
	public void writeAsXml(XMLStreamWriter writer) throws XMLStreamException;
}
