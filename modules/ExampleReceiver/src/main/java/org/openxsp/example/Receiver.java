package org.openxsp.example;

import org.openxsp.java.EventBus;
import org.openxsp.java.Verticle;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class Receiver extends Verticle {

	public static final String EVENT = "org.openxsp.test";

	@Override
	public void start() {
		
		System.out.println("Starting Example Receiver module");
		
		System.out.println("Registering event handler for " + EVENT);
		EventBus eb = openxsp.eventBus();
		// Register a handler for my event
		eb.registerHandler(EVENT, new Handler<Message>() {
			@Override
			public void handle(Message msg) {
				System.out.println("Received event: "+msg.body());
				JsonObject res = new JsonObject();
				res.putString("status", "ok");
				msg.reply(res);
				Object data = msg.body();
				// TODO do something with the data …
			}
		});
		
		System.out.println("Example Receiver Module started");
	}

}
