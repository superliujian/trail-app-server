package uk.co.prenderj.trailsrv.csv;

import java.io.Writer;

import uk.co.prenderj.trailsrv.model.Comment;
import au.com.bytecode.opencsv.CSVWriter;

public class CommentWriter extends CSVWriter {
    public CommentWriter(Writer writer, char separator, char quotechar, char escapechar, String lineEnd) {
        super(writer, separator, quotechar, escapechar, lineEnd);
    }
    
    public CommentWriter(Writer writer, char separator, char quotechar, char escapechar) {
        super(writer, separator, quotechar, escapechar);
    }
    
    public CommentWriter(Writer writer, char separator, char quotechar, String lineEnd) {
        super(writer, separator, quotechar, lineEnd);
    }
    
    public CommentWriter(Writer writer, char separator, char quotechar) {
        super(writer, separator, quotechar);
    }
    
    public CommentWriter(Writer writer, char separator) {
        super(writer, separator);
    }
    
    public CommentWriter(Writer writer) {
        super(writer);
    }
    
    public void writeNextComment(Comment comment) {
        writeNext(new String[] { String.valueOf(comment.id), String.valueOf(comment.longitude), String.valueOf(comment.latitude), comment.title, comment.body, comment.timestamp.toString() });
    }
}
