package com.jp.trailsrv;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.jp.trailsrv.handler.BaseHandler;
import com.jp.trailsrv.util.Log;
import com.sun.net.httpserver.HttpServer;

/**
 * Serves HTTP requests using threads.
 * @author Joshua Prendergast
 */
public class Server {
	private static final int MAX_SHUTDOWN_DELAY = 10;
	private int port;
	private HttpServer srv;
	private Database database;
	private CommentCache cache;
	
	/**
	 * Creates a new instance which is not started and has no handlers.
	 * @param port
	 * 		the port to listen on
	 * @throws RuntimeException if the server failed to start
	 */
	public Server(int port) throws RuntimeException {
		try {
			this.port = port;
			srv = HttpServer.create(new InetSocketAddress(port), 0);
			srv.setExecutor(createExecutor());

			Log.i("Connecting to database...");
			database = new Database(loadProperties());
			
			cache = new CommentCache("cache/comment_cache.xml");
			cache.rebuild(database);
		} catch (IOException | SQLException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected Executor createExecutor() {
		return Executors.newCachedThreadPool(); // TODO Tweak settings
	}
	
	protected Map<String, String> loadProperties() {
		// TODO Load from file
		Map<String, String> properties = new HashMap<>();
		properties.put("user", "root");
		properties.put("password", "");
		properties.put("jdbcUrl", "jdbc:mysql://localhost/trail");
		return properties;
	}
	
	/**
	 * Starts the server.
	 */
	public void start() {
		Log.i("Server listening on port " + port);
		srv.start();
	}
	
	public void stop() {
		Log.i("Stopping server...");
		srv.stop(MAX_SHUTDOWN_DELAY);
	}
	
	public void createContext(BaseHandler handler) {
		srv.createContext(handler.getContextPath(), handler);
	}
	
	public Database getDatabase() {
		return database;
	}
	
	public CommentCache getCommentCache() {
		return cache;
	}
}
