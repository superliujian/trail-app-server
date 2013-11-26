package uk.co.prenderj.trailsrv.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import uk.co.prenderj.trailsrv.util.Util;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.sun.net.httpserver.HttpExchange;

public class PostParams {
    private Map<String, String> params;

    /**
     * Converts the text stream of a HTTP POST from URL-encoded parameters to a
     * map of parameters.
     * @param in the HTTP POST request body. This is consumed and closed.
     * @throws IOException if an IO error occurs
     * @throws IllegalArgumentException if the parameters contain a duplicate value
     */
    public PostParams(InputStream in) throws IOException {
        parse(in);
    }
    
    public void parse(InputStream in) throws IOException {
        params = new HashMap<>();
        try {
            String body = Util.streamToString(in);
            Iterable<String> pairs = Splitter.on('&').trimResults().split(body);
            for (String str : pairs) {
                Iterable<String> kv = Splitter.on('=').trimResults().split(str);
                String key = URLDecoder.decode(Iterables.getFirst(kv, null), "UTF-8");
                if (params.containsKey(key)) {
                    throw new IllegalArgumentException("Duplicate parameter: " + key);
                } else {
                    params.put(key, URLDecoder.decode(Iterables.getLast(kv), "UTF-8"));
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e); // This can't happen
        }
    }

    public String get(String key) {
        return params.get(key);
    }

    public boolean containsKeys(String... keys) {
        for (String key : keys) {
            if (!params.containsKey(key)) {
                return false;
            }
        }
        return true;
    }
}
