package org.openxsp.cdn.test;

import java.util.UUID;

import org.junit.Test;
import org.openxsp.cdn.connector.net.vertx.VertxHandler;
import org.openxsp.cdn.connector.util.log.Logger;
import org.openxsp.cdn.connector.util.log.LoggerFactory;
import org.openxsp.java.session.SessionControlCreateSession;
import org.openxsp.java.session.SessionControlFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

public class VertxHandlerTest extends TestVerticle{
	
	private static final Logger log = LoggerFactory.getLogger(VertxHandlerTest.class);
	
	@Test
	public void vertxHandlerTest(){
		
		//deploy verticle
		
		JsonObject config = new JsonObject();
		config.putString(VertxHandler.CONFIG_KURENTO_WS, "ws://kurento:8888/kurento");
		
		
		container.deployVerticle(VertxHandler.class.getCanonicalName(), config, new Handler<AsyncResult<String>>() {
			
			@Override
			public void handle(AsyncResult<String> arg0) {
				log.i("CDN connector verticle deployed");
				
				uploadTest();
			}
		});
	}
	
	
	//end-to-end test
	public void uploadTest(){
		
		log.d("Running upload test");
		
		//register callback
		String callBack = "org.openxsp.test.cdn.upload.callback";
		
		vertx.eventBus().registerHandler(callBack, new Handler<Message<String>>() {

			@Override
			public void handle(Message<String> msg) {
				log.d("Received answer: msg.body()");
				
			}
		
			
		});
		
		JsonObject youtubeConfig = new JsonObject();
		//TODO create connector config
		
		JsonObject content = new JsonObject();
		content.putString(VertxHandler.PARAM_SDP, ""); //FIXME
		content.putObject(VertxHandler.PARAM_CDN_CONFIG, youtubeConfig);
		
		String sessionId = UUID.randomUUID().toString().substring(0, 5);
		
		SessionControlCreateSession create = SessionControlFactory.createInitSessionMessage(sessionId, "TestAlice", "TestBob", content.toString(), "application/json", callBack);
	
	
		vertx.eventBus().send(VertxHandler.EVENT_UPLOADFILE, create.create());
	}
	
	
}
