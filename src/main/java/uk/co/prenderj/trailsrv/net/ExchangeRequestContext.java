package uk.co.prenderj.trailsrv.net;

import java.io.IOException;
import java.io.InputStream;

import com.sun.net.httpserver.HttpExchange;

import org.apache.commons.fileupload.RequestContext;

@SuppressWarnings("restriction")
public class ExchangeRequestContext implements RequestContext {
    private HttpExchange ex;
    
    public ExchangeRequestContext(HttpExchange ex) {
        this.ex = ex;
    }
    
    @Override
    public String getCharacterEncoding() {
        return "UTF-8"; // FIXME
    }
    
    @Override
    public int getContentLength() {
        String contentLength = ex.getRequestHeaders().getFirst("Content-Length");
        return contentLength == null ? 0 : Integer.parseInt(contentLength);
    }
    
    @Override
    public String getContentType() {
        return ex.getRequestHeaders().getFirst("Content-Type");
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        return ex.getRequestBody();
    }
}
