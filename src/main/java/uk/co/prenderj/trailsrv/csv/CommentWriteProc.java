package uk.co.prenderj.trailsrv.csv;

import uk.co.prenderj.trailsrv.model.Comment;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Writes a comment to a CSV writer.
 * @author Joshua Prendergast
 */
public class CommentWriteProc {
    private Comment comment;

    public CommentWriteProc() {
    }

    public CommentWriteProc(Comment comment) {
        this.comment = comment;
    }

    public void process(CSVWriter writer) throws NullPointerException {
    	String[] out = new String[] { String.valueOf(comment.id), comment.latitude.toString(), comment.longitude.toString(), comment.body,
                comment.timestamp.toString() };
        writer.writeNext(out);
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }
}
