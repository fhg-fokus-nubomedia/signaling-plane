package org.openxsp;
import org.openxsp.java.Verticle;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;


public class DefaultVerticle extends Verticle{

	
	@Override
	public void start() {
		super.start();
		
		System.out.println("Starting "+DefaultVerticle.class.getSimpleName());
		
		System.out.println("Configuration is: "+container.config());
		
		//TODO
//		registerEvents();
		
		//TODO
//		sendEvents();

		
		//TODO open connections
		
		//TODO implement business logic
		
		container.logger().debug("Verticle started");
	}
	
	private void registerEvents(){
		openxsp.eventBus().registerHandler("MyEventAddress", new Handler<Message<String>>() {

			@Override
			public void handle(Message<String> msg) {
				container.logger().debug("Received event");
				
				String message = msg.body();
				
				//TODO process the message
			}
		});
	}
	
	private void sendEvents(){
		openxsp.eventBus().send("RemoteAddress", "Hello World");
	}
}
