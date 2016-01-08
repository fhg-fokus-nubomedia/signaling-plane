package org.openxsp.java.session;

import org.openxsp.java.EventBus;
import org.openxsp.java.OpenXSP;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class SessionControlFactory {
	
	
	public static SessionControlMessage parse(JsonObject json){
		
		
		String method = json.getString("method");

        if (method == null){
        	System.out.println(SessionControlFactory.class.getSimpleName()+": Cannot parse session control message - No method-parameter provided");
        	return null;
        }
            

        switch (SessionControlMethodEnum.valueOf(method.toUpperCase())) {
            case INIT:
                SessionControlCreateSession createSession = new SessionControlCreateSessionImpl();
                return createSession.parse(json);
            case ACCEPT:
                SessionControlAcceptSession acceptSession = new SessionControlAcceptSessionImpl();
                return acceptSession.parse(json);
            case CANCEL:
                SessionControlCancelSession cancelSession = new SessionControlCancelSessionImpl();
                return cancelSession.parse(json);
            case CONFIRM:
                SessionControlConfirmSessionEstablishment confirmSessionEstablishment = new SessionControlConfirmSessionEstablishmentImpl();
                return confirmSessionEstablishment.parse(json);
            case UPDATE:
                SessionControlUpdateSession controlUpdateSession = new SessionControlUpdateSessionImpl();
                return controlUpdateSession.parse(json);
            case END:
                SessionControlEndSession endSession = new SessionControlEndSessionImpl();
                return endSession.parse(json);
            case INFO:
                SessionControlInfoSession info = new SessionControlInfoSessionImpl();
                return info.parse(json);
            case MESSAGE:
                InstantMessage instantMessage = new InstantMessageImpl();
                return instantMessage.parse(json);
            default:
                System.out.println("Unknown method "+method);
                return null;
        }
        
	}
	
	public static SessionControlAcceptSession createAcceptSessionMessage(String sessionId, String content, String contentType, String callBackAddress ){
		SessionControlAcceptSessionImpl a = new SessionControlAcceptSessionImpl();
		a.setContent(content);
		a.setContentType(contentType);
		a.setSessionID(sessionId);
		a.setAddress(callBackAddress);
		
		return a;
	}

	public static SessionControlConfirmSessionEstablishment createConfirmSessionMessage(String sessionId, String content, String contentType, String callBackAddress ){
		
		SessionControlConfirmSessionEstablishmentImpl c = new SessionControlConfirmSessionEstablishmentImpl(content, contentType);
		c.setSessionID(sessionId);
		c.setAddress(callBackAddress);
		
		return c;
	}
	
	public static SessionControlCancelSession createCancelSessionMessage(String sessionId, String callbackAddress){
		SessionControlCancelSessionImpl impl = new SessionControlCancelSessionImpl();
		impl.setSessionID(sessionId);
		impl.setAddress(callbackAddress);
		
		return impl;
	}
	
	public static SessionControlEndSession createEndSessionMessage(String sessionId, String callbackAddress){
		SessionControlEndSessionImpl impl = new SessionControlEndSessionImpl();
		impl.setSessionID(sessionId);
		impl.setAddress(callbackAddress);
		
		return impl;
	}
	
	public static SessionControlCreateSession createInitSessionMessage(String sessionId, String from, String to, String content, String contentType, String callbackAddress){
		SessionControlCreateSessionImpl impl = new SessionControlCreateSessionImpl();
		impl.setContent(content);
		impl.setContentType(contentType);
		impl.setFrom(from);
		impl.setTo(to);
		impl.setAddress(callbackAddress);
		impl.setSessionID(sessionId);
		
		return impl;
	}

    public static SessionControlUpdateSession createUpdateSessionMessage(String sessionId, String content, String contentType, String callBackAddress ){
        SessionControlUpdateSessionImpl u = new SessionControlUpdateSessionImpl();
        u.setContent(content);
        u.setContentType(contentType);
        u.setSessionID(sessionId);
        u.setAddress(callBackAddress);

        return u;
    }
	
	public static InstantMessage createInstantMessage(String from, String to, String content, String contentType, String callBackAdress){
		InstantMessageImpl impl = new InstantMessageImpl();
		impl.setAddress(callBackAdress);
		impl.setContent(content);
		impl.setContentType(contentType);
		impl.setFrom(from);
		impl.setTo(to);
		
		return impl;
	}
	
	public static SessionControlInfoSession createInfoSessionMessage(String sessionId, String from, String to, String content, String contentType, String callBackAdress){
		SessionControlInfoSessionImpl impl = new SessionControlInfoSessionImpl();
		impl.setAddress(callBackAdress);
		impl.setContent(content);
		impl.setContentType(contentType);
		impl.setFrom(from);
		impl.setTo(to);
		impl.setSessionID(sessionId);
		
		return impl;
	}
	
}
