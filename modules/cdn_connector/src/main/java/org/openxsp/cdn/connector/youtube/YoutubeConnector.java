package org.openxsp.cdn.connector.youtube;

import java.io.File;

import org.openxsp.cdn.connector.CdnConnector;
import org.openxsp.cdn.connector.ConnectorCallback;
import org.openxsp.cdn.connector.ConnectorCallback.ConnectorError;
import org.openxsp.cdn.connector.util.log.Logger;
import org.openxsp.cdn.connector.util.log.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

public class YoutubeConnector implements CdnConnector{

	public static final String 
		PARAM_APPLICATION_NAME = "application_name",
		PARAM_AUTH_CONFIG = "auth";
	
	private static Logger log = LoggerFactory.getLogger(YoutubeConnector.class);
	
	
	public void uploadVideoAsFile(JsonObject connectorConfig, File file, ConnectorCallback cb) {
		UploadVideo.upload(connectorConfig, file, cb);
	}

	
	public void uploadVideoAsStream(JsonObject connectorConfig, ConnectorCallback cb) {
		log.w("Upload stream not yet implemented");
		
		if(cb!=null){
			cb.onError(ConnectorError.OperationNotSupported);
		}
	}

	
	public void playVideoStream(JsonObject connectorConfig, String id, ConnectorCallback cb) {
		log.w("Play stream not yet implemented");
		
		if(cb!=null){
			cb.onError(ConnectorError.OperationNotSupported);
		}
	}
	
	
	public void downloadVideo(JsonObject connectorConfig, String id, ConnectorCallback cb){
		log.w("Download video not yet implemented");
		if(cb!=null){
			cb.onError(ConnectorError.OperationNotSupported);
		}
	}

	
	@Override
	public void getUploadedVideos(JsonObject connectorConfig, ConnectorCallback cb) {
		VideoList.getVideos(connectorConfig, cb);
	}



	@Override
	public void deleteVideo(JsonObject connectorConfig, String id, ConnectorCallback cb) {
		DeleteVideo.delete(connectorConfig, id, cb);
	}

}
