package org.openxsp.cdn.connector.youtube;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openxsp.cdn.connector.ConnectorCallback;
import org.openxsp.cdn.connector.ConnectorCallback.ConnectorError;
import org.openxsp.cdn.connector.util.log.Logger;
import org.openxsp.cdn.connector.util.log.LoggerFactory;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

public class VideoList {
	
	public static final String 
		PARAM_ID = "id",
		PARAM_TITLE = "title",
		PARAM_UPLOADED_AT = "upload_date",
		PARAM_METADATA = "meta_data",
		PARAM_VIDEOLIST = "videos";

	private static final Logger log = LoggerFactory.getLogger(VideoList.class);

	public static void getVideos(JsonObject connectorConfig, ConnectorCallback cb) {

		String applicationName = connectorConfig.getString(YoutubeConnector.PARAM_APPLICATION_NAME);

		if (applicationName == null) {
			log.w("Missing " + YoutubeConnector.PARAM_APPLICATION_NAME + " - parameter in config");
			cb.onError(ConnectorError.BadConfiguration.setMessage("Missing Application Name"));
			return;
		}

		// this.authConfig =
		JsonObject auth = connectorConfig.getObject(YoutubeConnector.PARAM_AUTH_CONFIG);

		Set<String> scopes = new HashSet<>();
		scopes.add("https://www.googleapis.com/auth/youtube.readonly");

		try {
			// Authorize the request.
			Credential credential = Auth.authorize(scopes, auth/* "uploadvideo" */);

			// This object is used to make YouTube Data API requests.
			YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
					applicationName).build();

			log.d("Getting uploaded videos");

			// Call the API's channels.list method to retrieve the
			// resource that represents the authenticated user's channel.
			// In the API response, only include channel information needed for
			// this use case. The channel's contentDetails part contains
			// playlist IDs relevant to the channel, including the ID for the
			// list that contains videos uploaded to the channel.
			YouTube.Channels.List channelRequest = youtube.channels().list("contentDetails");
			channelRequest.setMine(true);
			channelRequest.setFields("items/contentDetails,nextPageToken,pageInfo");
			ChannelListResponse channelResult = channelRequest.execute();

			List<Channel> channelsList = channelResult.getItems();

			if (channelsList != null) {
				// The user's default channel is the first item in the list.
				// Extract the playlist ID for the channel's videos from the
				// API response.
				String uploadPlaylistId = channelsList.get(0).getContentDetails().getRelatedPlaylists().getUploads();

				// Define a list to store items in the list of uploaded videos.
				List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

				// Retrieve the playlist of the channel's uploaded videos.
				YouTube.PlaylistItems.List playlistItemRequest = youtube.playlistItems().list("id,contentDetails,snippet");
				playlistItemRequest.setPlaylistId(uploadPlaylistId);

				// Only retrieve data used in this application, thereby making
				// the application more efficient. See:
				// https://developers.google.com/youtube/v3/getting-started#partial
				//playlistItemRequest.setFields("items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");

				String nextToken = "";

				// Call the API one or more times to retrieve all items in the
				// list. As long as the API response returns a nextPageToken,
				// there are still more items to retrieve.
				while (nextToken != null) {
					playlistItemRequest.setPageToken(nextToken);
					PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

					playlistItemList.addAll(playlistItemResult.getItems());

					nextToken = playlistItemResult.getNextPageToken();
				} 

				
				JsonObject response = new JsonObject().putArray(PARAM_VIDEOLIST, createJsonList(playlistItemList));
				
				if(cb!=null){
					cb.onSuccess(null, response);
				}
			}

		} catch (IOException e) {
			log.e("",e);
			cb.onError(ConnectorError.InternalError.setMessage(e.getMessage()));
		} catch (GeneralSecurityException e) {
			log.e("Authentication error", e);
			cb.onError(ConnectorError.Unauthorized.setMessage(e.getMessage()));
		}
	}

	private static  JsonArray createJsonList(List<PlaylistItem> playlistItemList) throws IOException {
		JsonArray videos = new JsonArray();
		
		for(PlaylistItem item : playlistItemList){
			
			JsonObject videoInfo = new JsonObject();
			videoInfo
				.putString(PARAM_ID, item.getSnippet().getResourceId().getVideoId())
				.putString(PARAM_TITLE, item.getSnippet().getTitle())
				.putString(PARAM_UPLOADED_AT, item.getSnippet().getPublishedAt().toStringRfc3339())
				.putObject(PARAM_METADATA, new JsonObject(item.getSnippet().toPrettyString()));
			
			videos.addObject(videoInfo);
			log.d("Found Video: "+videoInfo);
		}
		
		
		
		return videos;
	}

}
