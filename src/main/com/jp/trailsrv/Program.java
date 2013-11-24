package com.jp.trailsrv;

import com.jp.trailsrv.handler.CommentAdder;
import com.jp.trailsrv.handler.CommentLoader;

public class Program {
	/**
	 * Main execution point.
	 * @param args
	 * 		command line arguments
	 * @throws Exception if any exception is uncaught
	 */
	public static void main(String[] args) throws Exception {
		Server srv = new Server(8080);
		srv.createContext(new CommentAdder(srv));
		srv.createContext(new CommentLoader(srv));
		srv.start();
	}
}
