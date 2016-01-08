package org.openxsp.example;

import java.util.Scanner;

import org.openxsp.java.RPCResult;
import org.openxsp.java.RPCResultHandler;
import org.openxsp.java.Verticle;
import org.vertx.java.core.json.JsonObject;

public class Sender extends Verticle {
	public static final String EVENT = "org.openxsp.test";

	@Override
	public void start() {
		System.out.println("Starting Sender Example module");

		System.out.println("Type a message to send:");
		Scanner reader = new Scanner(System.in);
		String message = reader.next();
		reader.close();
		sendEvent(message);
	}

	private void sendEvent(String message){
		JsonObject data = new JsonObject();
		data.putString("message", message);
		System.out.println("Sending message "+data+" to "+EVENT);
		openxsp.eventBus().invoke(EVENT, "OptionalRpcAction", data, new RPCResultHandler() {
			@Override
			public void handle(RPCResult res) {
				if (res.succeeded()) {
					System.out.println("Successful reply received");
				}
				if (res.failed()) {
					System.out.println("Unsuccessful reply received");
				}
			}
		});
	}
}
