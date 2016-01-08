package org.openxsp.cdn.connector.net;

import org.openxsp.cdn.connector.handler.SessionHandler;
import org.vertx.java.core.json.JsonObject;


public interface NetworkService {
	
	void createSession(String action, String sessionId, String from, String to, String content, String contentType, SessionHandler handler);
	
	void acceptSession(String action, String sessionId, String content, String contentType, SessionHandler handler);
	
	void confirmSession(String action, String sessionId, String content, String contentType, SessionHandler handler);
	
	void cancelSession(String action, String sessionId, String content, String contentType, SessionHandler handler);
	
	void endSession(String action, String sessionId, SessionHandler handler);
	
	JsonObject getConfig();

}
