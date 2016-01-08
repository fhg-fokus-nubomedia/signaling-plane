package org.openxsp.cdn.connector.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openxsp.cdn.connector.youtube.util.PrivateKeyConverter;
import org.vertx.java.core.json.JsonObject;

/**
 * Shared class used by every sample. Contains methods for authorizing a user
 * and caching credentials.
 */
public class Auth {

	public static final String 
		PARAM_PRIVATE_KEY_ID = "private_key_id", 
		PARAM_PRIVATE_KEY = "private_key",
		PARAM_CLIENT_EMAIL = "client_email", 
		
		PARAM_TYPE = "type",
		PARAM_ACCESS_TOKEN = "access_token",
		PARAM_CLIENT_ID = "client_id", 
		PARAM_CODE ="code",
		PARAM_CLIENT_SECRET="client_secret",
		PARAM_REDIRECT_URI="redirect_uri",
		PARAM_GRANT_TYPE="grant_type"
		;

	 /**
	 * Define a global instance of the HTTP transport.
	 */
	 public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	
	 /**
	 * Define a global instance of the JSON factory.
	 */
	 public static final JsonFactory JSON_FACTORY = new JacksonFactory();

	/**
	 * This is the directory that will be used under the user's home directory
	 * where OAuth tokens will be stored.
	 */
	private static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";

	/**
	 * Authorizes the installed application to access user's protected data.
	 *
	 */
	public static Credential authorize(Set<String> scopes, JsonObject jsonCredentials) throws GeneralSecurityException, IOException {
		/*
		 * // Load client secrets. Reader clientSecretReader = new
		 * InputStreamReader
		 * (Auth.class.getResourceAsStream("/client_secrets.json"));
		 * GoogleClientSecrets clientSecrets =
		 * GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);
		 * 
		 * // Checks that the defaults have been replaced (Default = //
		 * "Enter X here"). if
		 * (clientSecrets.getDetails().getClientId().startsWith("Enter") ||
		 * clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
		 * System.out.println(
		 * "Enter Client ID and Secret from https://code.google.com/apis/console/?api=youtube"
		 * + "into src/main/resources/client_secrets.json"); System.exit(1); }
		 * 
		 * // This creates the credentials datastore at //
		 * ~/.oauth-credentials/${credentialDatastore} FileDataStoreFactory
		 * fileDataStoreFactory = new FileDataStoreFactory(new
		 * File(System.getProperty("user.home") + "/" + CREDENTIALS_DIRECTORY));
		 * DataStore<StoredCredential> datastore =
		 * fileDataStoreFactory.getDataStore(credentialDatastore);
		 * 
		 * GoogleAuthorizationCodeFlow flow = new
		 * GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
		 * clientSecrets, scopes).setCredentialDataStore(datastore).build();
		 * 
		 * // Build the local server and bind it to port 8080
		 * LocalServerReceiver localReceiver = new
		 * LocalServerReceiver.Builder().setPort(8080).build();
		 * 
		 * // Authorize. return new AuthorizationCodeInstalledApp(flow,
		 * localReceiver).authorize("user");
		 */

		String 
			privateKeyId = jsonCredentials.getString(PARAM_PRIVATE_KEY_ID),
//			privateKeyString = jsonCredentials.getString(PARAM_PRIVATE_KEY),
//			clientMail = jsonCredentials.getString(PARAM_CLIENT_EMAIL),
			cientId = jsonCredentials.getString(PARAM_CLIENT_ID),
			redirectUri = jsonCredentials.getString(PARAM_REDIRECT_URI),
			grantType = jsonCredentials.getString(PARAM_GRANT_TYPE),
			code = jsonCredentials.getString(PARAM_CODE),
			clientSecret = jsonCredentials.getString(PARAM_CLIENT_SECRET),
			accessToken = jsonCredentials.getString(PARAM_ACCESS_TOKEN);
		

		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

//		PrivateKey privateKey = PrivateKeyConverter.fromBase64StringToPrivateKey(privateKeyString);
//		
//		JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
//		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//		InputStream is = java.io.Reader.class.getResourceAsStream("nubomedia-test-8a6c122a7517.json");
//		
//		File f = new File("nubomedia-test-8a6c122a7517.json");
//		FileInputStream fi= new FileInputStream("nubomedia-test-8a6c122a7517.json");
		
//		if( is==null) System.out.println("Null");
		
		GoogleCredential credential = new GoogleCredential.Builder()
		    .setTransport(httpTransport)
		    .setJsonFactory(JSON_FACTORY)
//		    .setServiceAccountId(clientMail)
//		    .setServiceAccountPrivateKeyFromP12File(new File("nubomedia-test-678edf93d07d.p12"))
//		    .setClientSecrets(cientId, clientSecret)
//		    .setServiceAccountScopes(scopes)
		    .build();
		
		credential.setAccessToken(accessToken);
		
		

		// Build service account credential.
//		GoogleCredential credential = new GoogleCredential.Builder()
//			.setTransport(HTTP_TRANSPORT)
//			.setJsonFactory(JSON_FACTORY)
//			.setServiceAccountId(clientMail)
//			.setServiceAccountScopes(scopes)
//			.setServiceAccountPrivateKeyId(privateKeyId)
//			.setServiceAccountPrivateKey(privateKey)
//			.build();
		

		return credential;
	}
}