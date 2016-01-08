package org.openxsp.cdn.test;

import java.util.UUID;

import org.junit.Assert;
import org.openxsp.cdn.connector.handler.SessionHandler;
import org.openxsp.cdn.connector.util.mem.HeapStorage;
import org.openxsp.cdn.connector.util.mem.Storage;
import org.openxsp.cdn.connector.util.mem.VertxStorage;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class StorageTestVerticle extends Verticle {
	
	public static final String 
		EVENT_STRING_GET = "org.openxsp.mem.string.get", 
		EVENT_STRING_PUT = "org.openxsp.mem.string.put",
		EVENT_STRING_REMOVE = "org.openxsp.mem.string.remove",
		EVENT_STRING_SIZE = "org.openxsp.mem.string.size",
		EVENT_STRING_KEYSET = "org.openxsp.mem.string.keyset",
		EVENT_HANDLER_GET = "org.openxsp.mem.handler.get",
		EVENT_HANDLER_PUT = "org.openxsp.mem.handler.put", 
		EVENT_HANDLER_REMOVE = "org.openxsp.mem.handler.remove",
		EVENT_HANDLER_SIZE = "org.openxsp.mem.handler.size",
		EVENT_HANDLER_KEYSET = "org.openxsp.mem.handler.keyset";

	private Storage<String, String> stringStorage = new VertxStorage<>(SharedMemoryTest.STRING_MAP);
	private Storage<String, SessionHandler> handlerStorage = new HeapStorage<>();

	@Override
	public void start() {
		super.start();
		
		String id = UUID.randomUUID().toString().substring(0,1);

		System.out.println("Starting storage test verticle "+id);

		
		vertx.eventBus().registerHandler(EVENT_STRING_GET, new Handler<Message<String>>() {
			@Override
			public void handle(Message<String> msg) {
				Assert.assertNotNull(msg.body());
				msg.reply(stringStorage.get(msg.body()));
			}
		});
		
		
		vertx.eventBus().registerHandler(EVENT_STRING_PUT, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> msg) {
				Assert.assertNotNull(msg.body());
				Assert.assertNotNull(msg.body().getString("key"));
				Assert.assertNotNull(msg.body().getString("value"));
				stringStorage.put(msg.body().getString("key"), msg.body().getString("value"));
				msg.reply();
			}
		});

		System.out.println(id+": second handler registered");
		
		vertx.eventBus().registerHandler(EVENT_STRING_REMOVE, new Handler<Message<String>>() {
			@Override
			public void handle(Message<String> msg) {
				Assert.assertNotNull(msg.body());
				String res = stringStorage.remove(msg.body());
				msg.reply(res);
			}
		});
		
		
		vertx.eventBus().registerHandler(EVENT_STRING_SIZE, new Handler<Message<String>>() {
			@Override
			public void handle(Message<String> msg) {
				Assert.assertNotNull(msg.body());
				msg.reply(stringStorage.size());
			}
		});
		
		
		vertx.eventBus().registerHandler(EVENT_STRING_KEYSET, new Handler<Message<String>>() {
			@Override
			public void handle(Message<String> msg) {
				Assert.assertNotNull(msg.body());
				msg.reply(stringStorage.keySet());
			}
		});

		vertx.eventBus().registerHandler(EVENT_HANDLER_GET, new Handler<Message<String>>() {
			@Override
			public void handle(Message<String> msg) {
				Assert.assertNotNull(msg.body());
				msg.reply(handlerStorage.get(msg.body()) == null);
			}
		});

		vertx.eventBus().registerHandler(EVENT_HANDLER_REMOVE, new Handler<Message<String>>() {
			@Override
			public void handle(Message<String> msg) {
				Assert.assertNotNull(msg.body());
				SessionHandler res = handlerStorage.remove(msg.body());
				msg.reply(res == null);
			}
		});
		
		vertx.eventBus().registerHandler(EVENT_HANDLER_KEYSET, new Handler<Message<String>>() {
			@Override
			public void handle(Message<String> msg) {
				Assert.assertNotNull(msg.body());
				msg.reply(handlerStorage.keySet());
			}
		});
		
		vertx.eventBus().registerHandler(EVENT_HANDLER_SIZE, new Handler<Message<String>>() {
			@Override
			public void handle(Message<String> msg) {
				Assert.assertNotNull(msg.body());
				msg.reply(handlerStorage.size());
			}
		});
		
		System.out.println(id+" done");
	}
}
