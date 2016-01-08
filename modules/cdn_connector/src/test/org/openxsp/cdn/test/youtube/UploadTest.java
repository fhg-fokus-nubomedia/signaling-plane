package org.openxsp.cdn.test.youtube;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.openxsp.cdn.connector.ConnectorCallback;
import org.openxsp.cdn.connector.util.log.Logger;
import org.openxsp.cdn.connector.util.log.LoggerFactory;
import org.openxsp.cdn.connector.youtube.VideoList;
import org.openxsp.cdn.connector.youtube.YoutubeConnector;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

public class UploadTest extends TestVerticle{

	private static Logger log = LoggerFactory.getLogger(UploadTest.class);
	
	private void startServer(){
		int port = 12345;
		
		HttpServer server = vertx.createHttpServer();

		server.requestHandler(new Handler<HttpServerRequest>() {
		    public void handle(HttpServerRequest req) {
		    	
		    	String file = "src/main/resources/webapp/upload_video.html"; 
		    	
		    	if(req.path().endsWith("upload_video.js")){
		    		file = "src/main/resources/webapp/upload_video.js";
		    	}
		    	
		    	if(req.path().endsWith("cors_upload.js")){
		    		file = "src/main/resources/webapp/cors_upload.js";
		    	}
		    	
		    	if(req.path().endsWith("upload_video.css")){
		    		file = "src/main/resources/webapp/upload_video.css";
		    	}
		    	
		        //req.response().sendFile("/home/xsp/openxsp/modules/java_webrtc/src/main/resources/webapp/" + file);
                req.response().sendFile(file);
		      }
		  });
		
		JsonObject config = new JsonObject().putString("prefix", "/eventbus");

		JsonArray noPermitted = new JsonArray();
		noPermitted.add(new JsonObject());

		vertx.createSockJSServer(server).bridge(config, noPermitted, noPermitted);

		server.listen(port);
		
		log.d("Server started on port "+port);
	}
	
	
	@Test
	public void testUpload(){
		AuthTest.init();
		
		startServer();
	}
	
	/*
	 * run after the server was started and an access token was retrieved 
	 * (after successful log in the access token is printed on the console) and copy/paste 
	 * this token to AuthTest.ACCESS_TOKEN
	 */
	public static void main(String[] args) {
		
		AuthTest.init();
		
		JsonObject youtubeConfig = new JsonObject();
		youtubeConfig.putString(YoutubeConnector.PARAM_APPLICATION_NAME, "nubomedia-test");
		
		youtubeConfig.putObject(YoutubeConnector.PARAM_AUTH_CONFIG, AuthTest.config);
		
		YoutubeConnector youtubeConnector = new YoutubeConnector();
		
		deleteAllVideos(youtubeConfig);
		
		youtubeConnector.uploadVideoAsFile(youtubeConfig, new File("sample-video.mp4"), new ConnectorCallback(){

			@Override
			public void onSuccess(String connectorId, JsonObject result) {
				log.d("Upload successful");
				
			}

			@Override
			public void onError(ConnectorError e) {
				// TODO Auto-generated method stub
				log.w("Upload error: "+e.toString());
				org.junit.Assert.fail();
			}
			
		});
		
		
		youtubeConnector.getUploadedVideos(youtubeConfig, new ConnectorCallback(){

			@Override
			public void onSuccess(String connectorId, JsonObject result) {
				// TODO Auto-generated method stub
				
				JsonArray list = result.getArray(VideoList.PARAM_VIDEOLIST);
				
				org.junit.Assert.assertEquals(1, list.size());
			}

			@Override
			public void onError(ConnectorError e) {
				// TODO Auto-generated method stub
				org.junit.Assert.fail();
			}
			
		});
		
		
		deleteAllVideos(youtubeConfig);
	}
	
	private static void deleteAllVideos(final JsonObject config){
		
		final CountDownLatch latch = new CountDownLatch(1);
		
		log.d("deleting all videos");
		
		log.d("Getting list of all uploaded videos");
		
		new YoutubeConnector().getUploadedVideos(config, new ConnectorCallback(){

			@Override
			public void onSuccess(String connectorId, JsonObject result) {
				Set<String> videoIds = new HashSet<>();
				
				JsonArray list = result.getArray(VideoList.PARAM_VIDEOLIST);
				
				if(list!=null && list.size()>0){
					for(int i=0; i<list.size(); i++){
						JsonObject video = list.get(i);
						videoIds.add(video.getString(VideoList.PARAM_ID));
					}
				}
				
				
				log.v("Found "+videoIds.size()+" videos to delete");
				
				if(videoIds.size()==0) return;
				
				final CountDownLatch deleteLatch = new CountDownLatch(videoIds.size());
				
				for(String id : videoIds){
					log.d("Deleting video "+id);
					new YoutubeConnector().deleteVideo(config, id, new ConnectorCallback(){

						@Override
						public void onSuccess(String connectorId, JsonObject result) {
							deleteLatch.countDown();
						}

						@Override
						public void onError(ConnectorError e) {
							deleteLatch.countDown();
							log.w("Could not delete video: "+e.toString());
						}
						
					});
				}
				
				//wait for delete operations to finish
				try {
					deleteLatch.await(15, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				latch.countDown();
			}

			@Override
			public void onError(ConnectorError e) {
				log.w("Could not get video list");
				log.w(e.toString());
				latch.countDown();
			}
			
		});
		
		try {
			latch.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		log.v("Deletion done");
	}
}
