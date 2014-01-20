package uk.co.prenderj.trailsrv.handler;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import uk.co.prenderj.trailsrv.storage.DataSource;
import uk.co.prenderj.trailsrv.storage.FileSource;
import uk.co.prenderj.trailsrv.util.Log;

import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public class ViewAttachmentHandler extends BaseHandler {
    private DataSource dataSource;
    private FileSource fileSource;
    
    public ViewAttachmentHandler(DataSource dataSource, FileSource fileSource) {
        super("attachment", "GET");
        this.dataSource = dataSource;
        this.fileSource = fileSource;
    }

    @Override
    public void call(HttpExchange ex) throws Exception {
        try {
            // Grab the last segment as attachment ID
            // e.g. www.example.com/attachment/205
            URI uri = ex.getRequestURI();
            String[] segments = uri.getPath().split("/");
            writeResponse(ex, Long.parseLong(segments[2]));
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            ex.sendResponseHeaders(400, 0); // Bad request
        } catch (TimeoutException e) {
            Log.e("Timeout when loading attachment", e);
            ex.sendResponseHeaders(504, 0); // Gateway timeout
        }
    }

    private void writeResponse(HttpExchange ex, long attachmentId) throws IOException, TimeoutException, InterruptedException, ExecutionException {
        if (dataSource.doesAttachmentExist(attachmentId).get(getTimeoutRemaining(), TimeUnit.MILLISECONDS)) {
            // Redirect to access URL
            URL url = fileSource.getSignedAccessUrl(Long.toString(attachmentId), 1);
            ex.getResponseHeaders().set("Location", url.toString());
            ex.sendResponseHeaders(307, 0);
        } else {
            ex.sendResponseHeaders(404, 0); // Not found
        }
    }
}
