package org.openxsp.cdn.connector.youtube;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openxsp.cdn.connector.ConnectorCallback;
import org.openxsp.cdn.connector.ConnectorCallback.ConnectorError;
import org.openxsp.cdn.connector.util.log.Logger;
import org.openxsp.cdn.connector.util.log.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

public class DeleteVideo {

	private static Logger log = LoggerFactory.getLogger(DeleteVideo.class);

	public static void delete(JsonObject connectorConfig, String videoId, ConnectorCallback cb) {
		
		
		log.v("Trying to delete video with id "+videoId);

		String applicationName = connectorConfig.getString(YoutubeConnector.PARAM_APPLICATION_NAME);

		if (applicationName == null) {
			log.w("Missing " + YoutubeConnector.PARAM_APPLICATION_NAME + " - parameter in config");
			cb.onError(ConnectorError.BadConfiguration.setMessage("Missing Application Name"));
			return;
		}

		// this.authConfig =
		JsonObject auth = connectorConfig.getObject(YoutubeConnector.PARAM_AUTH_CONFIG);

		Set<String> scopes = new HashSet<>();
		scopes.add("https://www.googleapis.com/auth/youtube");

		try {
			// Authorization.
			Credential credential = Auth.authorize(scopes, auth);

			// This object is used to make YouTube Data API requests.
			YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(applicationName).build();

			// Create the video list request
//			YouTube.Videos.List listVideosRequest = youtube.videos().list(videoId);
//
//			// Request is executed and video list response is returned
//			VideoListResponse listResponse = listVideosRequest.execute();
//
//			List<Video> videoList = listResponse.getItems();
//			if (videoList.isEmpty()) {
//				log.e("Can't find a video with video id: " + videoId);
//				cb.onError(ConnectorError.NotFound);
//				return;
//			}
//			log.v("Video exists");

			
			// Create the video delete request
			YouTube.Videos.Delete deleteVideoRequest = youtube.videos().delete(videoId);

			deleteVideoRequest.execute();

		} catch (GoogleJsonResponseException e) {
			log.e("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage(), e);
			cb.onError(ConnectorError.CdnError.setMessage(e.getDetails().getCode() + " : " + e.getDetails().getMessage()));
		} catch (IOException e) {
			log.e("", e);
			cb.onError(ConnectorError.InternalError.setMessage(e.getMessage()));
		} catch (GeneralSecurityException e) {
			log.e("Authorization error", e);
			cb.onError(ConnectorError.Unauthorized.setMessage(e.getMessage()));
		}
	}
}
