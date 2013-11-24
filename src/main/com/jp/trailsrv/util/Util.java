package com.jp.trailsrv.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Contains associated static utility methods.
 * @author Joshua Prendergast
 */
public class Util {
	/**
	 * Converts an input stream into its String equivalent. This method may block.
	 * The underlying stream is fully exhausted and closed by this method.
	 * @param in
	 * 		the input stream
	 * @param charset
	 * 		the charset
	 * @return the string representation of the stream
	 * @throws IOException if a stream error occurs
	 */
	public static String streamToString(InputStream in, Charset charset) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset))) {
			StringBuilder rv = new StringBuilder();
			String l;
			while ((l = reader.readLine()) != null) {
				rv.append(l);
			}
			return rv.toString();
		}
	}
	
	/**
	 * Converts an input stream into its String equivalent using UTF-8 encoding. This method may block.
	 * The underlying stream is closed by this method.
	 * @param in
	 * 		the input stream
	 * @return the string representation of the stream
	 * @throws IOException if a stream error occurs
	 */
	public static String streamToString(InputStream in) throws IOException {
		return streamToString(in, Charset.forName("UTF-8"));
	}
}
