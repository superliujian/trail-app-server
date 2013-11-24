package com.jp.trailsrv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.SQLException;

public class CommentCache {
	private File file = new File("comments.xml");
	
	public void reconstruct(Database database) throws FileNotFoundException, SQLException {
		if (file.exists()) {
			file.delete();
		}
		database.loadComments(new FileOutputStream(file));
	}
}
