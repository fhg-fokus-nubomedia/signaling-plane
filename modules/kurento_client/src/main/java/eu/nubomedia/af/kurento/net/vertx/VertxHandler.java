package eu.nubomedia.af.kurento.net.vertx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.kurento.client.factory.KurentoClient;
import org.openxsp.java.EventBus;
import org.openxsp.java.RPCResult;
import org.openxsp.java.RPCResultHandler;
import org.openxsp.java.Verticle;
import org.openxsp.java.session.SessionControlAcceptSession;
import org.openxsp.java.session.SessionControlAcceptSessionImpl;
import org.openxsp.java.session.SessionControlCancelSession;
import org.openxsp.java.session.SessionControlCancelSessionImpl;
import org.openxsp.java.session.SessionControlConfirmSessionEstablishment;
import org.openxsp.java.session.SessionControlConfirmSessionEstablishmentImpl;
import org.openxsp.java.session.SessionControlCreateSession;
import org.openxsp.java.session.SessionControlCreateSessionImpl;
import org.openxsp.java.session.SessionControlEndSession;
import org.openxsp.java.session.SessionControlEndSessionImpl;
import org.openxsp.java.session.SessionControlMessage;
import org.openxsp.java.session.SessionControlMessgeImpl;
import org.openxsp.java.session.SessionControlUpdateSession;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import eu.nubomedia.af.kurento.SessionHandler;
import eu.nubomedia.af.kurento.call.CallSessionHandler;
import eu.nubomedia.af.kurento.net.NetworkService;
import eu.nubomedia.af.kurento.player.PlaySessionHandler;
import eu.nubomedia.af.kurento.util.log.Logger;
import eu.nubomedia.af.kurento.util.log.LoggerFactory;
import eu.nubomedia.af.kurento.util.log.VertxLogger;

/**
 * A connector for the OpenXSP platform
 * 
 * @author fsc
 *
 */
public class VertxHandler extends Verticle implements NetworkService{

	private static final String 
			CONFIG_CALLEVENT = "call_event", // own event address to receive call service requests
			CONFIG_PLAYEREVENT = "player_event", // own event address to receive player service requests
			CONFIG_SERVICEREGISTRY_EVENT = "service_registry_event", // event of the service registry where the module registers itself
			CONFIG_USEREGISTRY_EVENT = "user_registry_event", // event of the user registry to get information about user, e.g. event address
			CONFIG_KURENTO_WS = "kurento_address"; // websocket address of the Kurento server

	private String serviceregistryEvent, kurentoAddress, userRegistryEvent, callEvent, playerEvent;

	private static Logger log;

	private EventBus eb;
	
	private HashMap<String, SessionHandler> sessionHandler;
	
	private HashMap<String, String> remoteEvents;
	
	private KurentoClient kurentoClient;

	private SessionEventHandler callEventHandler, playEventHandler;
	
	public void start() {
		eb = openxsp.eventBus();
		
		log = new VertxLogger(openxsp, getClass().getSimpleName());

		LoggerFactory.setLogger(log);

		log.i("Starting Kurento AF module");

		// read config
		if(!readConfig(container.config())){
			//if config could not be read properly end this verticle
			try {
				finalize();
				return;
			} catch (Throwable e) {
				log.e("Could not stop verticle",e);
			}
			return;
		}

		registerEventHandler();
		
		// register handler to receive service requests
		publishServiceRegistryInformation();

	}
	
	
	@Override
	public void createSession(final String action, final String sessionId, String from, String to, String content, String contentType, final SessionHandler handler) {
		log.i("Creating session from "+from+" to "+to);
		log.v("Content: \r\n"+content);
		
		SessionControlCreateSessionImpl s = new SessionControlCreateSessionImpl();
		s.setContent(content);
		s.setContentType(contentType);
		s.setFrom(from);
		s.setTo(to);
		s.setAddress(this.callEvent);
		s.setSessionID(sessionId);

		String remoteEvent = getUserEvent(to);
		
		if(remoteEvent==null){
			log.w("Could not send create session request to remote user - user unknown");
			return;
		}
		
		eb.invoke(remoteEvent, action, s.create(), new RPCResultHandler() {
			
			@Override
			public void handle(RPCResult res) {
				if (res.succeeded()) {
					registerHandler(sessionId, handler);
				}
				else if (res.failed()) {
					
					log.e("Create session error", res.cause());
					
					if(handler!=null){
						if(res.cause()!=null) handler.onSessionError(action, sessionId, res.cause().getMessage());
						else handler.onSessionError(action, sessionId, "Create session error");
					}
				} 
			}
		});
	}
	

	@Override
	public void acceptSession(final String action, final String sessionId, String content, String contentType, final SessionHandler handler) {
		
		log.i("Accepting session with id "+sessionId);
		log.v("Content: \r\n"+content);
		
		SessionControlAcceptSessionImpl a = new SessionControlAcceptSessionImpl();
		a.setContent(content);
		a.setContentType(contentType);
		a.setSessionID(sessionId);
		a.setAddress(this.callEvent);
		
		String remoteEvent = getSessionRemoteEvent(sessionId);
		
		if(remoteEvent==null){
			log.w("Could not send accept session with id "+sessionId+" - remote event unknown");
			return;
		}
		
		eb.invoke(remoteEvent, action, a.create(), new RPCResultHandler() {
			
			@Override
			public void handle(RPCResult res) {
				if (res.succeeded()) {
					registerHandler(sessionId, handler);
				}
				else if (res.failed()) {
					
					log.e("Accept session error", res.cause());
					
					if(handler!=null){
						if(res.cause()!=null) handler.onSessionError(action, sessionId, res.cause().getMessage());
						else handler.onSessionError(action, sessionId, "Accept session error");
					}
				} 
			}
		});
	}

	@Override
	public void confirmSession(final String action, final String sessionId, String content, String contentType, final SessionHandler handler) {
		
		log.i("Confirming session with id "+sessionId);
		log.v("Content: \r\n"+content);
		
		SessionControlConfirmSessionEstablishmentImpl c = new SessionControlConfirmSessionEstablishmentImpl();
		c.setContent(content);
		c.setContentType(contentType);
		c.setSessionID(sessionId);
		c.setAddress(this.callEvent);
		
		String remoteEvent = getSessionRemoteEvent(sessionId);
		
		if(remoteEvent==null){
			log.w("Could not send confirm session with id "+sessionId+" - remote event unknown");
			handler.onSessionError("", sessionId, "Could not send confirm session with id "+sessionId+" - remote event unknown");
			return;
		}
		
		eb.invoke(remoteEvent, action, c.create(), new RPCResultHandler() {
			
			@Override
			public void handle(RPCResult res) {
				
				if (res.failed()) {
					
					log.w("Accept session error", res.cause());
					
					if(handler!=null){
						if(res.cause()!=null) handler.onSessionError(action, sessionId, res.cause().getMessage());
						else handler.onSessionError(action, sessionId, "Confirm session error");
					}
				}
				if(res.succeeded()){
					// message receivement acknowledged  by remote - nothing to do
				}
			}
		});
		
	}


	@Override
	public void cancelSession(final String action, final String sessionId, String content, String contentType, final SessionHandler handler) {
		
		log.i("Canceling session with id "+sessionId);
		
		SessionControlCancelSessionImpl c = new SessionControlCancelSessionImpl();
		c.setSessionID(sessionId);
		c.setAddress(this.callEvent);
		
		String remoteEvent = getSessionRemoteEvent(sessionId);
		
		if(remoteEvent==null){
			log.w("Could not send cancel session with id "+sessionId+" - remote event unknown");
			return;
		}
		
		eb.invoke(remoteEvent, action, c.create(), new RPCResultHandler() {
			
			@Override
			public void handle(RPCResult res) {
				
				if (res.failed()) {
					
					log.e("Cancel session error", res.cause());
					
					if(handler!=null){
						if(res.cause()!=null) handler.onSessionError(action, sessionId, res.cause().getMessage());
						else handler.onSessionError(action, sessionId, "Cancel session error");
					}
				}
				if(res.succeeded()){
					// message receivement acknowledged  by remote - nothing to do
				}
			}
		});
		
	}


	@Override
	public void endSession(final String action, final String sessionId, final SessionHandler handler) {
		
		log.i("Ending session with id "+sessionId);
		
		SessionControlEndSessionImpl c = new SessionControlEndSessionImpl();
		c.setSessionID(sessionId);
		c.setAddress(this.callEvent);
		
		String remoteEvent = getSessionRemoteEvent(sessionId);
		
		if(remoteEvent==null){
			log.w("Could not send end session with id "+sessionId+" - remote event unknown");
			return;
		}
		
		eb.invoke(remoteEvent, action, c.create(), new RPCResultHandler() {
			
			@Override
			public void handle(RPCResult res) {
				
				if (res.failed()) {
					
					log.e("Cancel session error", res.cause());
					
					if(handler!=null){
						if(res.cause()!=null) handler.onSessionError(action, sessionId, res.cause().getMessage());
						else handler.onSessionError(action, sessionId, "End session error");
					}
				}
				else{
					handler.onSessionEnded("", sessionId);
					
					unregisterHandler(sessionId);
				}
			}
		});
	}
	


	private boolean readConfig(JsonObject config){
		log.v("Config: " + container.config());

		this.kurentoAddress = config.getString(CONFIG_KURENTO_WS);

		if (this.kurentoAddress == null) {
			log.w("Missing Kurento address in configuration ... stopping module");
			return false;
		}
		
		kurentoClient = KurentoClient.create(kurentoAddress);

		this.serviceregistryEvent = config.getString(CONFIG_SERVICEREGISTRY_EVENT);
		
		this.userRegistryEvent = config.getString(CONFIG_USEREGISTRY_EVENT);
		
		this.playerEvent  = config.getString(CONFIG_PLAYEREVENT);
		
		this.callEvent  = config.getString(CONFIG_CALLEVENT);
		
		return true;
	}
	
	/*
	 * Subscribes for events in order to receive service requests for session establishment
	 */
	private void registerEventHandler(){
		
		playEventHandler = new SessionEventHandler(playerEvent);
		callEventHandler = new SessionEventHandler(callEvent);
		
		log.v("Registering handler for "+playEventHandler.getEvent());
		log.v("Registering handler for "+callEventHandler.getEvent());
		
		eb.registerHandler(playEventHandler.getEvent(), playEventHandler);
		eb.registerHandler(callEventHandler.getEvent(), callEventHandler);
	}
	
	
	/*
	 * Registers the module in the service registry
	 */
	private void publishServiceRegistryInformation() {

		if (this.serviceregistryEvent == null) {
			log.w("Missing service registry event");
			return;
		}

		// TODO publish event to service registry with service infornamtion of
		// this module

		log.i("Publish to service registry not yet implemented");

	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		if(playEventHandler!=null) eb.unregisterHandler(playEventHandler.getEvent(), playEventHandler);
		if(callEventHandler!=null) eb.unregisterHandler(callEventHandler.getEvent(), callEventHandler);
		
				
		if (serviceregistryEvent != null) {
			// TODO unregister this module at service registry
			log.w("Unregistering of this module not yet implemented");
		}
	}

	private class SessionEventHandler implements Handler<Message> {

		public String event;
		
		public SessionEventHandler(String event){
			this.event = event;
		}
		
		public String getEvent(){
			return event;
		}
		
		@Override
		public void handle(Message msg) {
			log.v("Received event: " + msg.body());
			// dispatch message
			JsonObject msgJsonBody = (JsonObject) msg.body();

			SessionControlMessgeImpl impl = new SessionControlMessgeImpl();
			SessionControlMessage scm = impl.parse(msgJsonBody);

			// dispatch session operation
			switch (scm.getMethod()) {
				case INIT:{
						log.i("Received creation of a new session");
						SessionControlCreateSession create = (SessionControlCreateSession) scm;
						
						JsonObject response = new JsonObject();
						response.putString("status", "ok");
						msg.reply(response);
						
						//map session id with event address
						registerRemoteEvent(create.getSessionID(), create.getAddress());
						
						if(event.equals(callEvent)){
							CallSessionHandler callHandler = new CallSessionHandler(kurentoClient, create.getFrom() , create.getTo(), VertxHandler.this);
							registerHandler(create.getSessionID(), callHandler);
							callHandler.onSessionCreate("", create.getSessionID(), create.getContent(), create.getContentType());
						}
						else if(event.equals(playerEvent)){
							PlaySessionHandler playHandler = new PlaySessionHandler(kurentoClient, create.getTo(), VertxHandler.this);
							registerHandler(create.getSessionID(), playHandler);
							playHandler.onSessionCreate("action", create.getSessionID(), create.getContent(), create.getContentType());
						}
						else log.w("Cannot handle event "+event);
						
					}
					break;
					
				case ACCEPT:{
						log.i("Received accept session");
						SessionControlAcceptSession accept = (SessionControlAcceptSession) scm;
						SessionHandler handler = getSessionHandler(accept.getSessionID());
						if(handler==null){
							log.i("Could not find handler for session with id "+accept.getSessionID());
							
							JsonObject response = new JsonObject();
							response.putString("status", "error");
							response.putString("message", "Session not found");
							msg.reply(response);
							break;
						}
						
						JsonObject response = new JsonObject();
						response.putString("status", "ok");
						msg.reply(response);
						
						//map session id with event address
						registerRemoteEvent(accept.getSessionID(), accept.getAddress());
						
						handler.onSessionAccepted("action", accept.getSessionID(), accept.getContent(), accept.getContentType());
					}
					break;
					
				case CONFIRM:{
						log.i("Received confirmation of session");
						SessionControlConfirmSessionEstablishment confirm = (SessionControlConfirmSessionEstablishment) scm;
						SessionHandler handler = getSessionHandler(confirm.getSessionID());
						if(handler==null){
							log.i("Could not find handler for session with id "+confirm.getSessionID());
							
							JsonObject response = new JsonObject();
							response.putString("status", "error");
							response.putString("message", "Session not found");
							msg.reply(response);
							break;
						}
						
						JsonObject response = new JsonObject();
						response.putString("status", "ok");
						msg.reply(response);
						
						//map session id with event address
						registerRemoteEvent(confirm.getSessionID(), confirm.getAddress());
						
						handler.onSessionComfirmed("action", confirm.getSessionID(), confirm.getContent(), confirm.getContentType());
					}
					break;
					
				case CANCEL:{
						log.i("Received cancel session");
						
						SessionControlCancelSession cancel = (SessionControlCancelSession) scm;
						SessionHandler handler = getSessionHandler(cancel.getSessionID());
						if(handler==null){
							log.i("Could not find handler for session with id "+cancel.getSessionID());
							
							JsonObject response = new JsonObject();
							response.putString("status", "error");
							response.putString("message", "Session not found");
							msg.reply(response);
							break;
						}
						
						JsonObject response = new JsonObject();
						response.putString("status", "ok");
						msg.reply(response);
						
						handler.onSessionCanceled("action", cancel.getSessionID());
					}
					break;
					
				case END:{
						log.i("Received end session");
						SessionControlEndSession end = (SessionControlEndSession) scm;
						SessionHandler handler = getSessionHandler(end.getSessionID());
						if(handler==null){
							log.i("Could not find handler for session with id "+end.getSessionID());
							
							JsonObject response = new JsonObject();
							response.putString("status", "error");
							response.putString("message", "Session not found");
							msg.reply(response);
							break;
						}
						
						JsonObject response = new JsonObject();
						response.putString("status", "ok");
						msg.reply(response);
						
						registerRemoteEvent(end.getSessionID(), end.getAddress());
						
						handler.onSessionEnded("action", end.getSessionID());
					}
					break;
					
				case UPDATE:{
						log.w("Handling of session update not yet implemented");
						JsonObject response = new JsonObject();
						response.putString("status", "error");
						response.putString("message", "Session not found");
						msg.reply(response);
						break;
					}
					
				default:
					log.w("Received request contains unsupported method field " + scm.getMethod());
					JsonObject response = new JsonObject();
					response.putString("status", "error");
					response.putString("message", "Unsupported method \""+scm.getMethod()+"\"");
					msg.reply(response);
			}
		}
	};

	
	private String getUserEvent(String to) {
		// TODO get remote event address from user registry
		return to;
	}
	

	private String getSessionRemoteEvent(String sessionId) {
		if(remoteEvents!=null){
			return remoteEvents.get(sessionId);
		}
		return null;
	}
	
	
	private void registerHandler(String sessionId, SessionHandler handler){
		if(this.sessionHandler==null) this.sessionHandler = new HashMap<>();
		
		sessionHandler.put(sessionId, handler);
	}
	
	
	private void unregisterHandler(String sessionId){
		if(this.sessionHandler==null) return;
		
		sessionHandler.remove(sessionId);
	}
	

	private SessionHandler getSessionHandler(String sessionId){
		if(this.sessionHandler==null) return null;
		
		return sessionHandler.get(sessionId);
	}
	
	private void registerRemoteEvent(String sessionId, String remoteEvent){
		if(this.remoteEvents==null) this.remoteEvents = new HashMap<>();
		
		remoteEvents.put(sessionId, remoteEvent);
	}


	@Override
	public JsonObject getConfig() {
		// TODO Auto-generated method stub
		return container.config();
	}
	
	
}
