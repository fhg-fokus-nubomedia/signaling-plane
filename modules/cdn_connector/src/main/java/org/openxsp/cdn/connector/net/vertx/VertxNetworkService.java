package org.openxsp.cdn.connector.net.vertx;

import org.openxsp.cdn.connector.handler.SessionHandler;
import org.openxsp.cdn.connector.net.NetworkService;
import org.openxsp.cdn.connector.util.log.Logger;
import org.openxsp.cdn.connector.util.log.LoggerFactory;
import org.openxsp.cdn.connector.util.mem.HeapStorage;
import org.openxsp.cdn.connector.util.mem.Storage;
import org.openxsp.java.OpenXSP;
import org.openxsp.java.RPCResult;
import org.openxsp.java.RPCResultHandler;
import org.openxsp.java.session.SessionControlAcceptSession;
import org.openxsp.java.session.SessionControlCancelSession;
import org.openxsp.java.session.SessionControlConfirmSessionEstablishment;
import org.openxsp.java.session.SessionControlCreateSession;
import org.openxsp.java.session.SessionControlEndSession;
import org.openxsp.java.session.SessionControlFactory;
import org.vertx.java.core.json.JsonObject;

public class VertxNetworkService implements NetworkService{

	private static Logger log = LoggerFactory.getLogger(VertxNetworkService.class);
	
	private String callbackAddress;
	
	private OpenXSP openxsp;
	
	private JsonObject config;
	
	private Storage<String, String> sessionStorage;
	private Storage<String, SessionHandler> handlerStorage;
	
	public VertxNetworkService(OpenXSP openxsp, JsonObject config, String callbackAddress, Storage<String, String> sessionStorage, Storage handlerStorage){
		this.openxsp = openxsp;
		this.config = config;
		this.callbackAddress = callbackAddress;
		this.sessionStorage = sessionStorage;
		this.handlerStorage = handlerStorage;
	}
	
	@Override
	public void createSession(final String action, final String sessionId, String from, String to, String content, String contentType, final SessionHandler handler) {
		log.i("Creating session from " + from + " to " + to);
		log.v("Content: \r\n" + content);

		SessionControlCreateSession s = SessionControlFactory.createInitSessionMessage(sessionId, from, to, content, contentType, callbackAddress);

		String remoteEvent = getUserEvent(to);

		if (remoteEvent == null) {
			log.w("Could not send create session request to remote user - user unknown");
			return;
		}

		openxsp.eventBus().invoke(remoteEvent, action, s.create(), new RPCResultHandler() {

			@Override
			public void handle(RPCResult res) {
				if (res.succeeded()) {
					registerHandler(sessionId, handler);
				} else if (res.failed()) {

					log.e("Create session error", res.cause());

					if (handler != null) {
						if (res.cause() != null)
							handler.onSessionError(action, sessionId, res.cause().getMessage());
						else
							handler.onSessionError(action, sessionId, "Create session error");
					}
				}
			}
		});
	}

	@Override
	public void acceptSession(final String action, final String sessionId, String content, String contentType, final SessionHandler handler) {

		log.i("Accepting session with id " + sessionId);
		log.v("Content: \r\n" + content);

		SessionControlAcceptSession a = SessionControlFactory.createAcceptSessionMessage(sessionId, content, contentType, callbackAddress);

		String remoteEvent = getSessionRemoteEvent(sessionId);

		if (remoteEvent == null) {
			log.w("Could not send accept session with id " + sessionId + " - remote event unknown");
			return;
		}

		openxsp.eventBus().invoke(remoteEvent, action, a.create(), new RPCResultHandler() {

			@Override
			public void handle(RPCResult res) {
				if (res.succeeded()) {
					registerHandler(sessionId, handler);
				} else if (res.failed()) {

					log.e("Accept session error", res.cause());

					if (handler != null) {
						if (res.cause() != null)
							handler.onSessionError(action, sessionId, res.cause().getMessage());
						else
							handler.onSessionError(action, sessionId, "Accept session error");
					}
				}
			}
		});
	}

	@Override
	public void confirmSession(final String action, final String sessionId, String content, String contentType, final SessionHandler handler) {

		log.i("Confirming session with id " + sessionId);
		log.v("Content: \r\n" + content);

		SessionControlConfirmSessionEstablishment c = SessionControlFactory.createConfirmSessionMessage(sessionId, content, contentType, callbackAddress);

		String remoteEvent = getSessionRemoteEvent(sessionId);

		if (remoteEvent == null) {
			log.w("Could not send confirm session with id " + sessionId + " - remote event unknown");
			handler.onSessionError("", sessionId, "Could not send confirm session with id " + sessionId
					+ " - remote event unknown");
			return;
		}

		openxsp.eventBus().invoke(remoteEvent, action, c.create(), new RPCResultHandler() {

			@Override
			public void handle(RPCResult res) {

				if (res.failed()) {

					log.w("Accept session error", res.cause());

					if (handler != null) {
						if (res.cause() != null)
							handler.onSessionError(action, sessionId, res.cause().getMessage());
						else
							handler.onSessionError(action, sessionId, "Confirm session error");
					}
				}
				if (res.succeeded()) {
					// message receivement acknowledged by remote - nothing to
					// do
				}
			}
		});

	}

	@Override
	public void cancelSession(final String action, final String sessionId, String content, String contentType,
			final SessionHandler handler) {

		log.i("Canceling session with id " + sessionId);

		SessionControlCancelSession c = SessionControlFactory.createCancelSessionMessage(sessionId, callbackAddress);

		String remoteEvent = getSessionRemoteEvent(sessionId);

		if (remoteEvent == null) {
			log.w("Could not send cancel session with id " + sessionId + " - remote event unknown");
			return;
		}

		openxsp.eventBus().invoke(remoteEvent, action, c.create(), new RPCResultHandler() {

			@Override
			public void handle(RPCResult res) {

				if (res.failed()) {

					log.e("Cancel session error", res.cause());

					if (handler != null) {
						if (res.cause() != null)
							handler.onSessionError(action, sessionId, res.cause().getMessage());
						else
							handler.onSessionError(action, sessionId, "Cancel session error");
					}
				}
				if (res.succeeded()) {
					// message receivement acknowledged by remote - nothing to
					// do
				}
			}
		});

	}

	@Override
	public void endSession(final String action, final String sessionId, final SessionHandler handler) {

		log.i("Ending session with id " + sessionId);

		SessionControlEndSession c = SessionControlFactory.createEndSessionMessage(sessionId, callbackAddress);

		String remoteEvent = getSessionRemoteEvent(sessionId);

		if (remoteEvent == null) {
			log.w("Could not send end session with id " + sessionId + " - remote event unknown");
			return;
		}

		openxsp.eventBus().invoke(remoteEvent, action, c.create(), new RPCResultHandler() {

			@Override
			public void handle(RPCResult res) {

				if (res.failed()) {

					log.e("Cancel session error", res.cause());

					if (handler != null) {
						if (res.cause() != null)
							handler.onSessionError(action, sessionId, res.cause().getMessage());
						else
							handler.onSessionError(action, sessionId, "End session error");
					}
				} else {
					handler.onSessionEnded("", sessionId);

					unregisterHandler(sessionId);
				}
			}
		});
	}

	private String getUserEvent(String to) {
		// TODO get remote event address from user registry
		return to;
	}

	@Override
	public JsonObject getConfig() {
		return this.config;
	}
	

	private String getSessionRemoteEvent(String sessionId) {
		
		return sessionStorage.get(sessionId);
	}

	private void registerHandler(String sessionId, SessionHandler handler) {
		
		handlerStorage.put(sessionId, handler);
	}

	private void unregisterHandler(String sessionId) {
		
		handlerStorage.remove(sessionId);
	}

//	private SessionHandler getSessionHandler(String sessionId) {
//		
//		return VertxStorage.getInstance().get(VertxStorage.MAP_SESSION_HANDLER, sessionId);
//	}
//
//	private void registerRemoteEvent(String sessionId, String remoteEvent) {
//		
//		VertxStorage.getInstance().put(VertxStorage.MAP_EVENT_SESSIONS, sessionId, remoteEvent);
//	}
}
