package uk.co.prenderj.trailsrv.handler;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.co.prenderj.trailsrv.TrailServer;
import uk.co.prenderj.trailsrv.storage.DataSource;
import uk.co.prenderj.trailsrv.storage.FileSource;
import uk.co.prenderj.trailsrv.util.Log;

import com.sun.net.httpserver.HttpExchange;

@SuppressWarnings("restriction")
public class ViewAttachmentServlet extends BaseServlet {
    public ViewAttachmentServlet(TrailServer srv) {
        super(srv);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Log.v("ViewAttachment called: " + req.getRequestURI());
            
            // Grab the last segment as attachment ID
            // e.g. www.example.com/attachment/205
            String uri = req.getRequestURI();
            String[] segments = uri.split("/");
            long attachmentId = Long.parseLong(segments[2]);
            
            if (attachmentId > 0) {
                writeResponse(resp, attachmentId);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (TimeoutException e) {
            Log.e("Timeout when loading attachment", e);
            resp.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT);
        } catch (InterruptedException | ExecutionException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void writeResponse(HttpServletResponse resp, long attachmentId) throws IOException, TimeoutException, InterruptedException, ExecutionException {
        if (getServer().getDatabase().doesAttachmentExist(attachmentId).get(5, TimeUnit.SECONDS)) {
            // Redirect to access URL
            URL url = getServer().getFileSource().getSignedAccessUrl(Long.toString(attachmentId), 1);
            resp.sendRedirect(resp.encodeRedirectURL(url.toString()));
        } else {
            resp.sendError(404); // Not found
        }
    }
}
