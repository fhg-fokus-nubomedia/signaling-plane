package org.openxsp.cdn.connector;

import java.io.File;
import java.io.Serializable;

import org.vertx.java.core.json.JsonObject;


public interface CdnConnector extends Serializable{

	
	/*
	 * Configures the CDN connector instance
	 */
	//CdnConnector configure(JsonObject config) throws ConnectorException;
	
	/*
	 * Uploads the video as a file to the CDN
	 */
	void uploadVideoAsFile(JsonObject connectorConfig, File fileToUpload, ConnectorCallback cb);
	
	/*
	 * Streams the video live to the CDN
	 */
	void uploadVideoAsStream(JsonObject connectorConfig, ConnectorCallback cb) throws ConnectorException;
	
	/*
	 * plays a video as stream
	 */
	void playVideoStream(JsonObject connectorConfig, String id, ConnectorCallback cb) throws ConnectorException;
	
	/*
	 * 
	 */
	void downloadVideo(JsonObject connectorConfig, String id, ConnectorCallback cb) throws ConnectorException;
	
	/*
	 * Discovers the existing videos
	 */
	void getUploadedVideos(JsonObject connectorConfig, ConnectorCallback cb) throws ConnectorException;
	
	/*
	 * Discovers the existing videos
	 */
	void deleteVideo(JsonObject connectorConfig, String id, ConnectorCallback cb) throws ConnectorException;
	
}
