package org.openxsp.example;


import org.openxsp.java.Verticle;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class BridgeServer extends Verticle {

	static final int PORT = 8081;
	
	public void start() {
		System.out.println("Starting Websocket server module");

		HttpServer server = vertx.createHttpServer();

		server.requestHandler(new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				
//				String file = "";
//		        if (req.path().equals("/")) {
//		          file = "index.html";
//		        } else if (!req.path().contains("..")) {
//		          file = req.path();
//		        }
		       
		        String file = req.path().equals("/") ? "index.html" : req.path().replace("/", "");
                
		        req.response().sendFile(file);
                
		        System.out.println("returning file "+file);
		        
				//req.response().sendFile(file);
			}
		});

		JsonObject config = new JsonObject().putString("prefix", "/eventbus");
		// create access rights filter – in this case empty = no restrictions
		JsonArray noPermitted = new JsonArray();
		noPermitted.add(new JsonObject());

		openxsp.createSockJSServer(server).bridge(config, noPermitted, noPermitted);
		
		server.listen(PORT);
	}

}
