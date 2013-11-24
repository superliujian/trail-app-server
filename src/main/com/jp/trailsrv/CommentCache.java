package com.jp.trailsrv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.jp.trailsrv.model.Comment;
import com.jp.trailsrv.util.Log;

/**
 * Maintains a local XML file of all comments stored in the database.
 * @author Joshua Prendergast
 */
public class CommentCache {
	private File file;
	
	public CommentCache(String path) {
		this.file = new File(path);
	}
	
	/**
	 * Writes the cache into an output stream. The output stream is closed after writing.
	 * @param out
	 * 		the output stream
	 * @throws IOException if an I/O error occurs
	 */
	public synchronized void write(OutputStream out) throws IOException {
		try (FileInputStream in = new FileInputStream(file)) {
			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}
		} finally {
			out.close();
		}
	}
	
	/**
	 * Rebuilds the cache from scratch.
	 * @param database
	 * 		the source database
	 * @throws SQLException if a database error occurs
	 * @throws IOException if a read error occurs (database or stream related)
	 */
	public synchronized void rebuild(Database database) throws IOException, SQLException {
		XMLStreamWriter writer = null;
		try {
			// Load comments from database
			Collection<Comment> comments = database.loadComments().get();
			Log.i("Rebuilding '" + file.getName() + "' with " + comments.size() + " comments");
			
			try (OutputStream out = new FileOutputStream(file)){
				writer = startWrite(out);
				for (Comment comment : comments) {
					comment.writeAsXml(writer);
					// writer.flush();
				}
				endWrite(writer);
			} finally {
				if (writer != null)
					writer.close(); // This doesn't close the underlying output stream
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new SQLException(e);
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}
	
	public synchronized void append(Comment comment) throws IOException {
		throw new IllegalStateException("Not yet implemented");
	}
	
	protected XMLStreamWriter createStreamWriter(OutputStream out) throws RuntimeException {
		try {
			return XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8");
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new RuntimeException(e);
		}
	}
	
	protected XMLStreamWriter startWrite(OutputStream out) throws IOException, XMLStreamException {
		XMLStreamWriter writer = createStreamWriter(out);
		if (file.exists()) {
			file.delete();
			file.createNewFile();
		}
		
		writer.writeStartDocument("UTF-8", "1.0");
		writer.writeStartElement("response");
		return writer;
	}
	
	protected void endWrite(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeEndElement();
		writer.flush();
	}
}
