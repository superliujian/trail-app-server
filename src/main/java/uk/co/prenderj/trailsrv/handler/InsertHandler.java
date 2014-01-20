package uk.co.prenderj.trailsrv.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.io.IOUtil;

import uk.co.prenderj.trailsrv.csv.CommentWriter;
import uk.co.prenderj.trailsrv.model.Attachment;
import uk.co.prenderj.trailsrv.model.Comment;
import uk.co.prenderj.trailsrv.net.ExchangeRequestContext;
import uk.co.prenderj.trailsrv.storage.DataSource;
import uk.co.prenderj.trailsrv.storage.FileSource;
import uk.co.prenderj.trailsrv.util.Log;
import uk.co.prenderj.trailsrv.util.SimpleTimer;
import uk.co.prenderj.trailsrv.util.Util;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public class InsertHandler extends BaseHandler {
    private DataSource dataSource;
    private FileSource fileSource;
    
    public InsertHandler(DataSource dataSource, FileSource fileSource) {
        super("/comments", "POST");
        this.dataSource = dataSource;
        this.fileSource = fileSource;
    }
    
    @Override
    public void call(HttpExchange ex) throws Exception {
        try {
            RequestContext ctx = new ExchangeRequestContext(ex);
            if (!FileUploadBase.isMultipartContent(ctx)) {
                throw new IllegalArgumentException();
            }
            
            FileUpload upload = new FileUpload();
            List<FileItem> parts = upload.parseRequest(ctx);
            
            double lat = Double.parseDouble(parts.get(0).getString());
            double lng = Double.parseDouble(parts.get(1).getString());
            String title = parts.get(2).getString();
            String body = parts.get(3).getString(); // TODO Limit size
            Attachment attachment = null;
            
            // Check if an attachment is there
            if (parts.size() == 5) {
                attachment = dataSource.insertAttachmentRecord().get(getTimeoutRemaining(), TimeUnit.MILLISECONDS);
                fileSource.storeFile(Long.toString(attachment.id), parts.get(4).getInputStream(), CannedAccessControlList.Private);
            }
            
            // Store comment in database and cache
            Comment comment = dataSource.insertComment(lat, lng, title, body, attachment, new Timestamp(new Date().getTime())).get(getTimeoutRemaining(), TimeUnit.MILLISECONDS);
            Log.v(String.format("Successfully added comment: '%s'", Util.preview(body, 25)));
            
            writeResponse(ex, comment);
        } catch (IllegalArgumentException e) {
            ex.sendResponseHeaders(400, 0); // Bad request
        } catch (InterruptedException | ExecutionException | IOException e) {
            Log.e("Failed to add comment", e);
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            Log.e("Timeout when adding comment", e);
            ex.sendResponseHeaders(504, 0); // Gateway timeout
        }
    }

    private void writeResponse(HttpExchange ex, Comment comment) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "text/csv");
        ex.sendResponseHeaders(200, 0);
        try (CommentWriter writer = new CommentWriter(new OutputStreamWriter(ex.getResponseBody()))) {
            writer.writeNextComment(comment);
        }
    }
}
