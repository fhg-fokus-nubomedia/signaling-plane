package org.openxsp.cdn.test.youtube;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;
import org.openxsp.cdn.connector.youtube.Auth;
import org.vertx.java.core.json.JsonObject;

import com.google.api.client.auth.oauth2.Credential;

public class AuthTest extends TestCase{

	private static String 
		
		CLIENT_ID = "799789750817-icv8uakocgjeisju85nq5tok8jqvdppi.apps.googleusercontent.com",
		ACCESS_TOKEN = "ya29.fAE9WsjPkDmynAscEukG6xhv5VcC0qFW0ikOLkRD6CFy38DNe149yq4MELzWk52GIGWQtks1Uws63w";
	
	public static JsonObject config;
	private static Set<String> scopes;
	
	
	public static void init(){
		System.out.println("Initializing config ...");
		
		config = new JsonObject();
		
		config.putString(Auth.PARAM_CLIENT_ID, CLIENT_ID);
		
		config.putString(Auth.PARAM_ACCESS_TOKEN, ACCESS_TOKEN);
		
		scopes = new HashSet<>();
		scopes.add("https://www.googleapis.com/auth/youtube.upload");
	}
	
	@Test
	public void testConverter() {
		
		/*
		if(config==null) init();
		
		PrivateKey privateKey = null;
		
		try {
			privateKey = PrivateKeyConverter.fromBase64StringToPrivateKey(PRIVATE_KEY);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		assertNotNull(privateKey);
		
		String base64Key = null;
		try {
			base64Key = PrivateKeyConverter.fromPrivateKeyToBase64String(privateKey);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		
		assertNotNull(base64Key);
		
		assertEquals(PRIVATE_KEY, base64Key);
		*/
	}
	
	@Test
	public void testConfig(){
		
		if(config==null) init();
		
//		assertEquals(CLIENT_EMAIL, config.getString(Auth.PARAM_CLIENT_EMAIL));
		assertEquals(CLIENT_ID, config.getString(Auth.PARAM_CLIENT_ID));
//		assertEquals(PRIVATE_KEY, config.getString(Auth.PARAM_PRIVATE_KEY));
//		assertEquals(PRIVATE_KEY_ID, config.getString(Auth.PARAM_PRIVATE_KEY_ID));
//		assertEquals(TYPE, config.getString(Auth.PARAM_TYPE));
	}
	
	@Test
	public void testAuth(){
		
		if(config==null) init();
		
		Credential credential = null;
		try {
			credential = Auth.authorize(scopes, config);
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
		}
		
		assertNotNull(credential);
	}

}
