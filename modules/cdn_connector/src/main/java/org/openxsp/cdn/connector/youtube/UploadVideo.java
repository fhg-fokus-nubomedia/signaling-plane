/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.openxsp.cdn.connector.youtube;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;

/**
 * Upload a video to the authenticated user's channel. Use OAuth 2.0 to
 * authorize the request. Note that you must add your video files to the project
 * folder to upload them with this application.
 *
 */
public class UploadVideo {

	private static Logger log = LoggerFactory.getLogger(UploadVideo.class);

	/**
	 * Define a global variable that specifies the MIME type of the video being
	 * uploaded.
	 */
	private static final String VIDEO_FILE_FORMAT = "video/*";

	// private static final String SAMPLE_VIDEO_FILENAME = "sample-video.mp4";

	/**
	 * Upload the user-selected video to the user's YouTube channel. The code
	 * looks for the video in the application's project folder and uses OAuth
	 * 2.0 to authorize the API request.
	 *
	 */
	public static void upload(JsonObject config, File file, final ConnectorCallback cb) {

		String applicationName = config.getString(YoutubeConnector.PARAM_APPLICATION_NAME);

		JsonObject authConfig = config.getObject(YoutubeConnector.PARAM_AUTH_CONFIG);

		if (applicationName == null) {
			log.w("Missing " + YoutubeConnector.PARAM_APPLICATION_NAME + " - parameter in config");
			cb.onError(ConnectorError.BadConfiguration.setMessage("Missing Application Name"));
			return;
		}

		if (!file.exists()) {
			log.w("Cannot upload file " + file + " - does not exist");
			if (cb != null) {
				cb.onError(ConnectorError.InternalError.setMessage("File not found"));
			}
			return;
		}
		// This OAuth 2.0 access scope allows an application to upload files
		// to the authenticated user's YouTube channel, but doesn't allow
		// other types of access.
		Set<String> scopes = new HashSet<>();
		scopes.add("https://www.googleapis.com/auth/youtube.upload");

		try {
			// Authorize the request.
			Credential credential = Auth.authorize(scopes, authConfig/* "uploadvideo" */);

			// This object is used to make YouTube Data API requests.
			YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(applicationName).build();

			log.d("Uploading: " + file);

			// Add extra information to the video before uploading.
			Video videoObjectDefiningMetadata = new Video();

			// Set the video to be publicly visible. This is the default
			// setting. Other supporting settings are "unlisted" and "private."
			VideoStatus status = new VideoStatus();
			status.setPrivacyStatus("public");
			videoObjectDefiningMetadata.setStatus(status);

			// Most of the video's metadata is set on the VideoSnippet object.
			VideoSnippet snippet = new VideoSnippet();

			// This code uses a Calendar instance to create a unique name and
			// description for test purposes so that you can easily upload
			// multiple files. You should remove this code from your project
			// and use your own standard names instead.
			Calendar cal = Calendar.getInstance();
			snippet.setTitle("Test Upload via Java on " + cal.getTime());
			snippet.setDescription("Video uploaded via YouTube Data API V3 using the Java library " + "on " + cal.getTime());

			// Set the keyword tags that you want to associate with the video.
			List<String> tags = new ArrayList<String>();
			tags.add("test");
			tags.add("example");
			tags.add("java");
			tags.add("YouTube Data API V3");
			tags.add("erase me");
			snippet.setTags(tags);

			// Add the completed snippet object to the video resource.
			videoObjectDefiningMetadata.setSnippet(snippet);

			InputStream is = UploadVideo.class.getResourceAsStream("/sample-video.mp4");
			InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT, is);

			// Insert the video. The command sends three arguments. The first
			// specifies which information the API request is setting and which
			// information the API response should return. The second argument
			// is the video resource that contains metadata about the new video.
			// The third argument is the actual video content.
			YouTube.Videos.Insert videoInsert = youtube.videos().insert("snippet,statistics,status", videoObjectDefiningMetadata,
					mediaContent);

			// Set the upload type and add an event listener.
			MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();

			// Indicate whether direct media upload is enabled. A value of
			// "True" indicates that direct media upload is enabled and that
			// the entire media content will be uploaded in a single request.
			// A value of "False," which is the default, indicates that the
			// request will use the resumable media upload protocol, which
			// supports the ability to resume an upload operation after a
			// network interruption or other transmission failure, saving
			// time and bandwidth in the event of network failures.
			uploader.setDirectUploadEnabled(false);

			MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
				public void progressChanged(MediaHttpUploader uploader) throws IOException {

					switch (uploader.getUploadState()) {
					case INITIATION_STARTED:
						log.i("Initiation Started");
						break;
					case INITIATION_COMPLETE:
						log.i("Initiation Completed:");
						break;
					case MEDIA_IN_PROGRESS:
						log.d("Upload in progress");
						log.v("Upload percentage: " + uploader.getProgress());
						break;
					case MEDIA_COMPLETE:
						log.i("Upload Completed!");
						break;
					case NOT_STARTED:
						log.w("Upload Not Started!");
						break;
					default:
						System.out.println("Event: " + uploader.getUploadState());
						break;
					}
				}
			};
			uploader.setProgressListener(progressListener);

			// Call the API and upload the video.
			Video returnedVideo = videoInsert.execute();

			// Print data about the newly inserted video from the API response.
			log.v("\n================== Returned Video ==================\n");
			log.v("  - Id: " + returnedVideo.getId());
			log.v("  - Title: " + returnedVideo.getSnippet().getTitle());
			log.v("  - Tags: " + returnedVideo.getSnippet().getTags());
			log.v("  - Privacy Status: " + returnedVideo.getStatus().getPrivacyStatus());
			log.v("  - Video Count: " + returnedVideo.getStatistics().getViewCount());

			if (cb != null) {
				JsonObject result = new JsonObject();
				result.putString("id", returnedVideo.getId());
				JsonObject meta = new JsonObject();
				for (String s : returnedVideo.keySet()) {
					meta.putValue(s, returnedVideo.get(s));
				}
				result.putObject("meta_info", meta);
				cb.onSuccess(null, result);
			}

		} catch (GoogleJsonResponseException e) {

			log.e("GoogleJsonResponseException", e);

			if (cb != null) {
				cb.onError(ConnectorError.CdnError.setMessage(e.getMessage()));
				;
			}

		} catch (Throwable t) {
			if (cb != null) {
				cb.onError(ConnectorError.InternalError.setMessage(t.getMessage()));
				;
			}
			log.e("Throwable: " + t.getMessage(), t);
		}
	}
}
