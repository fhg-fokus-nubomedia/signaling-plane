package org.openxsp.modules.web;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

public class FileHandler implements Handler<HttpServerRequest> {
	@Override
	public void handle(HttpServerRequest request) {
		Path path = Paths.get(request.uri());

		if (path.equals(Paths.get("/"))) {
			path = path.resolve("index.html");
		}
		path = Paths.get("webroot", path.toString());

		System.out.println("File Call: " + path);

		
		
//		if (!Files.exists(path)) {
//			System.out.println("File does not exist");
//			request.response().setStatusCode(404);
//			request.response().end();
//			return;
//		}
//		
//		System.out.println("Returning file "+path.toString());
//
//		request.response().sendFile(path.toString());
		
		
		
		 String file = request.path().equals("/") ? "index.html" : request.path();
		 request.response().sendFile("webroot/" + file);
	}
}
