package com.jp.trailsrv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import au.com.bytecode.opencsv.CSVWriter;

import com.jp.trailsrv.csv.CommentWriteProc;
import com.jp.trailsrv.model.Comment;
import com.jp.trailsrv.util.Log;
import com.jp.trailsrv.util.Processor;

/**
 * Maintains a local XML file of all comments stored in the database.
 * @author Joshua Prendergast
 */
public class CommentCache {
    private File file;

    public CommentCache(String path) {
        file = new File(path);
        if (!file.getParentFile().mkdirs()) { // Make folder structure
            throw new RuntimeException(); 
        }
    }

    /**
     * Writes the cache into an output stream.
     * @param out the output stream
     * @throws IOException if an I/O error occurs
     */
    public synchronized void write(OutputStream out) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            int c;
            while ((c = in.read()) != -1) {
                out.write(c);
            }
        }
    }

    /**
     * Rebuilds the cache from scratch.
     * @param database the source database
     * @throws SQLException if a database error occurs
     * @throws IOException if an stream IO error occurs
     */
    public synchronized void rebuild(DataSource database) throws IOException, SQLException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            // Load comments from database
            Log.i("Rebuilding '" + file.getName() + "'");
            database.loadComments(new Processor<ResultSet>() {
                @Override
                public void process(ResultSet rs) throws SQLException {
                    int i = 0;
                    CommentWriteProc proc = new CommentWriteProc();
                    while (rs.next()) {
                        Comment c = new Comment(rs.getLong("comment_id"), rs.getBigDecimal("lat"), rs.getBigDecimal("lng"), rs.getString("body"), rs
                                .getTimestamp("timestamp"));
                        proc.setComment(c);
                        writer.write(proc);
                        i++;
                    }
                    Log.i("Loaded " + i + " comments");
                }
            }).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new SQLException(e);
        }
    }

    /**
     * Adds a single comment to the cache.
     * @param comment the comment
     * @throws IOException if a write error occurs
     */
    public synchronized void append(Comment comment) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file, true))) {
            writer.write(new CommentWriteProc(comment));
        }
    }
}
