package org.openxsp.cdn.test;

import java.io.Serializable;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kurento.client.MediaElement;
import org.openxsp.cdn.connector.handler.SessionHandler;
import org.openxsp.cdn.connector.util.mem.Storage;
import org.openxsp.cdn.connector.util.mem.VertxStorage;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

public class SharedMemoryTest extends TestVerticle {

	protected final static String 
		STRING_MAP = "Stringmap",
		HANDLER_MAP = "Handlermap",
		STRINGKEY = "event",
		STRINGVALUE = "Stringvalue",
		HANDLERKEY = "handler";

	private Storage<String, String> stringStorage = null;
	private Storage<String, SessionHandler> handlerStorage = null;
	
	@Before
	public void init(){
		
		stringStorage = new VertxStorage<>(STRING_MAP);
		
		handlerStorage = new VertxStorage<>(HANDLER_MAP);
	}
	
	
	@Test
	public void sharedMemoryTest() {
		
		init();
		
		testStringStorage();
		
		testHandlerStorage();
		
		
		//prepare test verticles
		final int numberOfVerticles = 3;
		final CountDownLatch latch = new CountDownLatch(numberOfVerticles);
		final HashSet<String> deployments = new HashSet<>();
		Handler<AsyncResult<String>> handler = new Handler<AsyncResult<String>>() {

			@Override
			public void handle(AsyncResult<String> event) {
				System.out.println("Verticle deployed");
				deployments.add(event.result());
				//latch.countDown();
				
				testSharedStringStorage();
			}
		};
		
		container.deployWorkerVerticle(StorageTestVerticle.class.getCanonicalName(), new JsonObject(), numberOfVerticles, true, handler);
		
//		try {
//			latch.await(20, TimeUnit.SECONDS);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		Assert.assertEquals(numberOfVerticles, deployments.size());
//		
//		System.out.println("Test verticles deployed");
//		
//		testSharedStringStorage();
	}
	
	
	
	@Test
	@Ignore
	public void testStringStorage(){
		
		Assert.assertNotNull(stringStorage);
		
		Assert.assertNull(stringStorage.get(STRINGKEY));
		
		stringStorage.put(STRINGKEY, STRINGVALUE);

		Assert.assertEquals(STRINGVALUE, stringStorage.get( STRINGKEY));
		
		Assert.assertEquals(STRINGVALUE, stringStorage.remove( STRINGKEY));
		
		Assert.assertNull(stringStorage.get(STRINGKEY));
	}
	
	@Test
	@Ignore
	public void testHandlerStorage(){
		Assert.assertNotNull(handlerStorage);
		
		Assert.assertNull(handlerStorage.get(HANDLERKEY));
		
		XYZ xyz = new XYZ("hi");
		
		Assert.assertNull(handlerStorage.get(HANDLERKEY));
		
		handlerStorage.put(HANDLERKEY, xyz);

		Assert.assertEquals(xyz, handlerStorage.get(HANDLERKEY));
		
		Assert.assertEquals(xyz, handlerStorage.remove(HANDLERKEY));
		
		Assert.assertNull(handlerStorage.get(HANDLERKEY));
	}
	
	@Test
	@Ignore
	public void testSharedStringStorage(){
		System.out.println("Testing shared String storage");
		
		String testKey1="abc", testValue1="xyz";
		JsonObject json = new JsonObject().putString("key", testKey1).putString("value", testValue1);
		
		Assert.assertEquals(0, stringStorage.size());
		
		vertx.eventBus().send(StorageTestVerticle.EVENT_STRING_PUT, json, new Handler<Message<String>>(){

			@Override
			public void handle(Message<String> event) {
				Assert.assertEquals(1, stringStorage.size());
			}
			
		});
		
	}
	
	@Test
	@Ignore
	public void testSharedHandlerStorage(){
		System.out.println("Testing shared Handler storage");
	}
	
	
	
	
	
	private static class XYZ implements SessionHandler, Serializable{
		
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8875589103645176597L;
		
		private String a = "hello world";
		
		private MediaElement me;
		
		public void abc(){
			
		}
		
		public XYZ(String s) {
			// TODO Auto-generated constructor stub
			this.a=s;
		}
		
		@Override
		public void onSessionCreate(String action, String sessionId, String sessionDescription, String sessionDescriptionType) {}

		@Override
		public void onSessionAccepted(String action, String sessionId, String sessionDescription, String sessionDescriptionType) {}

		@Override
		public void onSessionComfirmed(String action, String sessionId, String sessionDescription, String sessionDescriptionType) {}

		@Override
		public void onSessionError(String action, String sessionId, String description) {}

		@Override
		public void onSessionCanceled(String action, String sessionId) {}

		@Override
		public void onSessionEnded(String action, String sessionId) {}
	}
	
	
}
