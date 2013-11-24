package com.jp.trailsrv.test;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jp.trailsrv.Program;

public class AddCommentTest {
	private static final String USER_AGENT = "Mozilla/5.0";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Program.main(new String[0]);
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		Program.stop();
	}

	@Test
	public void test() throws Exception {
		URL url = new URL("http://localhost:8080/comment/load");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		
		int code = conn.getResponseCode();
		assertEquals(code, 200);
	}
}
