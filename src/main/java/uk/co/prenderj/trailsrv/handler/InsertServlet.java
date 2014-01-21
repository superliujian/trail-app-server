package uk.co.prenderj.trailsrv.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import uk.co.prenderj.trailsrv.TrailServer;
import uk.co.prenderj.trailsrv.csv.CommentWriter;
import uk.co.prenderj.trailsrv.model.Attachment;
import uk.co.prenderj.trailsrv.model.Comment;
import uk.co.prenderj.trailsrv.storage.CommentSpec;
import uk.co.prenderj.trailsrv.util.Log;
import uk.co.prenderj.trailsrv.util.Util;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.common.base.Charsets;

@SuppressWarnings("restriction")
public class InsertServlet extends BaseServlet {
    private static final int MAX_PARAMS = 5;
    private ServletFileUpload fileUpload;
    
    public InsertServlet(TrailServer srv, long maxFileSize) {
        super(srv);
        
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(150 * 1024);
        factory.setRepository(srv.getTempDirectory());

        // Create a new file upload handler
        fileUpload = new ServletFileUpload(factory);
        fileUpload.setFileSizeMax(maxFileSize);
        // fileUpload.setSizeMax(maxFileSize + 1024);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Log.v("InsertComment called");
            
            if (!ServletFileUpload.isMultipartContent(req)) {
                throw new IllegalArgumentException();
            }
            
            // Parse the request
            int i = 0;
            List<FileItem> items = fileUpload.parseRequest(req);
            Map<String, String> params = new HashMap<>();
            Iterator<FileItem> iter = items.iterator();
            Attachment attachment = null;
            while (iter.hasNext()) {
                if (i++ > MAX_PARAMS) {
                    throw new IllegalArgumentException();
                }
                
                FileItem item = iter.next();
                
                if (item.isFormField()) {
                    params.put(item.getFieldName(), item.getString());
                } else if (item.getFieldName().equals("attachment")) {
                    Log.v(String.format("Attachment found: size = %d, Content-Type = %s, name = %s",
                            item.getSize(), item.getContentType(), item.getName()));
                    
                    // Upload and store file
                    try (InputStream is = item.getInputStream()) {
                        attachment = getServer().getDatabase().insertAttachmentRecord().get(5, TimeUnit.SECONDS);
                        
                        ObjectMetadata md = new ObjectMetadata();
                        md.setContentLength(item.getSize());
                        md.setContentType(item.getContentType()); // TODO Filter non-audio / image
                        md.setContentDisposition(item.getName());
                        getServer().getFileSource().storeFile(Long.toString(attachment.id), is, md, CannedAccessControlList.Private);
                    }
                }
            }
            
            Comment comment = getServer().getDatabase().insertComment(new CommentSpec(Double.parseDouble(params.get("lat")), Double.parseDouble(params.get("lng")),
                    params.get("title"), params.get("body"), attachment, new Timestamp(new Date().getTime()))).get(5, TimeUnit.SECONDS);
            Log.v(String.format("Successfully added comment: '%s'", Util.preview(comment.body, 25)));
            
            writeResponse(resp, comment);
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (FileSizeLimitExceededException e) {
            resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        } catch (InterruptedException | ExecutionException | IOException | FileUploadException e) {
            Log.e("Failed to add comment", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (TimeoutException e) {
            Log.e("Timeout when adding comment", e);
            resp.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT);
        }
    }

    private void writeResponse(HttpServletResponse resp, Comment comment) throws IOException {
        resp.setContentType("text/csv");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        try (CommentWriter writer = new CommentWriter(new OutputStreamWriter(resp.getOutputStream(), Charsets.UTF_8))) {
            writer.writeNextComment(comment);
        }
    }
}
