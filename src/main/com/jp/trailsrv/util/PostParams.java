package com.jp.trailsrv.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.sun.net.httpserver.HttpExchange;

public class PostParams {
    private Map<String, String> params;

    /**
     * Converts the text stream of a HTTP POST from URL-encoded parameters to a
     * map of parameters.
     * @param ex the HTTP POST
     * @throws IOException if an IO error occurs
     * @throws IllegalArgumentException if the parameters contain a duplicate value
     */
    public PostParams(HttpExchange ex) throws IOException, IllegalArgumentException {
        params = new HashMap<>();
        try {
            if (!ex.getRequestMethod().equalsIgnoreCase("POST")) {
                throw new IllegalArgumentException("Expected RequestMethod = POST");
            }
            /*
             * if (!ex.getRequestHeaders().get("Content-Type").equals(
             * "application/x-www-form-urlencoded")) { throw new
             * IllegalArgumentException
             * ("Expected Context-Type = application/x-www-form-urlencoded"); }
             */
            String body = Util.streamToString(ex.getRequestBody());
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
