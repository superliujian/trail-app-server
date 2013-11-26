package com.jp.trailsrv.csv;

import com.jp.trailsrv.model.Comment;

import au.com.bytecode.opencsv.CSVWriteProc;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Writes a comment to a CSV writer.
 * @author Joshua Prendergast
 */
public class CommentWriteProc implements CSVWriteProc {
    private Comment comment;

    public CommentWriteProc() {
    }

    public CommentWriteProc(Comment comment) {
        this.comment = comment;
    }

    @Override
    public void process(CSVWriter writer) throws NullPointerException {
        writer.writeNext(String.valueOf(comment.id), comment.latitude.toString(), comment.longitude.toString(), comment.body,
                comment.timestamp.toString());
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }
}
