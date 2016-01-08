package org.openxsp.cdn.connector.net.vertx;

import org.openxsp.cdn.connector.CdnConnector;
import org.openxsp.cdn.connector.ConnectorCallback;
import org.openxsp.cdn.connector.ConnectorException;
import org.openxsp.cdn.connector.ConnectorFactory;
import org.openxsp.cdn.connector.handler.FileDownloadSessionHandler;
import org.openxsp.cdn.connector.handler.FileUploadSessionHandler;
import org.openxsp.cdn.connector.handler.SessionHandler;
import org.openxsp.cdn.connector.net.NetworkService;
import org.openxsp.cdn.connector.repo.CloudRepository;
import org.openxsp.cdn.connector.repo.RepositoryFactory;
import org.openxsp.cdn.connector.repo.UploadCallback;
import org.openxsp.cdn.connector.util.log.Logger;
import org.openxsp.cdn.connector.util.log.LoggerFactory;
import org.openxsp.cdn.connector.util.log.VertxLogger;
import org.openxsp.cdn.connector.util.mem.Storage;
import org.openxsp.cdn.connector.util.mem.VertxStorage;
import org.openxsp.java.Verticle;
import org.openxsp.java.session.SessionControlAcceptSession;
import org.openxsp.java.session.SessionControlCancelSession;
import org.openxsp.java.session.SessionControlConfirmSessionEstablishment;
import org.openxsp.java.session.SessionControlCreateSession;
import org.openxsp.java.session.SessionControlEndSession;
import org.openxsp.java.session.SessionControlFactory;
import org.openxsp.java.session.SessionControlMessage;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;



public class VertxHandler extends Verticle {

	public static final String 
			CONFIG_KURENTO_WS = "kurento_address", // websocket address of the  Kurento server
			EVENT_UPLOADFILE = "nubomedia.cdn.fileupload",
			EVENT_UPSTREAM = "nubomedia.cdn.upstream",
			EVENT_DOWNSTREAM = "nubomedia.cdn.downstream",
			EVENT_DOWNLOAD = "nubomedia.cdn.download",
			EVENT_GETVIDEOS = "nubomedia.cdn.getvideos",
			EVENT_DELETEVIDEO = "nubomedia.cdn.delete",
			PARAM_CDN_CONFIG = "cdn_config",
			PARAM_REPO_CONFIG = "repository_config",
			PARAM_SDP = "sdp";

	private String kurentoAddress, serviceregistryEvent;

	private static Logger log;

//	private HashMap<String, SessionHandler> sessionHandler;
//
//	private HashMap<String, String> remoteEvents;

	private SessionEventHandler fileUploadHandler, uploadStreamHandler, fileDownloadHandler, downStreamHandler;
	
	private NetworkService uploadNetworkService, upstreamNetworkService, downloadNetworkservice, downStreamNetworkService;

	private Storage<String, String> sessionStorage;
	private Storage<String, SessionHandler> handlerStorage;
	
	private CloudRepository repo;
	
	public void start() {

		log = new VertxLogger(openxsp, getClass().getSimpleName(), "CDN Connector");

		LoggerFactory.setLogger(log);

		log.i("Starting CDN connector module");

		// read config
		if (!readConfig(container.config())) {
			// if config could not be read properly end this verticle
			try {
				finalize();
				return;
			} catch (Throwable e) {
				log.e("Could not stop verticle", e);
			}
			return;
		}
		
		
		this.sessionStorage = new VertxStorage<>("org.sessions");
		this.handlerStorage = new VertxStorage<>("org.handler");
		
		registerEventHandler();
		
		uploadNetworkService = new VertxNetworkService(openxsp, container.config(), EVENT_UPLOADFILE, sessionStorage, handlerStorage);
		upstreamNetworkService = new VertxNetworkService(openxsp, container.config(), EVENT_UPSTREAM, sessionStorage, handlerStorage);
		downloadNetworkservice = new VertxNetworkService(openxsp, container.config(), EVENT_DOWNLOAD, sessionStorage, handlerStorage);
		downStreamNetworkService = new VertxNetworkService(openxsp, container.config(), EVENT_DOWNSTREAM, sessionStorage, handlerStorage);

		// register handler to receive service requests
		publishServiceRegistryInformation();

	}

	private boolean readConfig(JsonObject config) {
		log.v("Config: " + container.config());

		this.kurentoAddress = config.getString(CONFIG_KURENTO_WS);

		if (this.kurentoAddress == null) {
			log.w("Missing Kurento address in configuration ... stopping module");
			return false;
		}
		
		JsonObject repoConfig = config.getObject(PARAM_REPO_CONFIG);
		this.repo = RepositoryFactory.getRepository(repoConfig);

		return true;
	}

	/*
	 * Subscribes for events in order to receive service requests for session
	 * establishment
	 */
	private void registerEventHandler() {

		fileUploadHandler = new SessionEventHandler(EVENT_UPLOADFILE);
		fileDownloadHandler = new SessionEventHandler(EVENT_DOWNLOAD);
		uploadStreamHandler = new SessionEventHandler(EVENT_UPSTREAM);
		downStreamHandler = new SessionEventHandler(EVENT_DOWNSTREAM);

		log.v("Registering handler for " + fileUploadHandler.getEvent());
		log.v("Registering handler for " + fileDownloadHandler.getEvent());
		log.v("Registering handler for " + uploadStreamHandler.getEvent());
		log.v("Registering handler for " + downStreamHandler.getEvent());

		openxsp.eventBus().registerHandler(fileUploadHandler.getEvent(), fileUploadHandler);
		openxsp.eventBus().registerHandler(fileDownloadHandler.getEvent(), fileDownloadHandler);
		openxsp.eventBus().registerHandler(uploadStreamHandler.getEvent(), uploadStreamHandler);
		openxsp.eventBus().registerHandler(downStreamHandler.getEvent(), downStreamHandler);
		
		openxsp.eventBus().registerHandler(EVENT_DELETEVIDEO, new Handler<Message<String>>() {

			@Override
			public void handle(final Message<String> msg) {
				JsonObject json = new JsonObject(msg.body());
				
				JsonObject cdnConfig = json.getObject(PARAM_CDN_CONFIG);
				
				String videoId = json.getString("");
				
				CdnConnector connector;
				try {
					connector = ConnectorFactory.getConnector(cdnConfig);
					connector.deleteVideo(cdnConfig, videoId, new ConnectorCallback(){

						@Override
						public void onSuccess(String connectorId, JsonObject result) {
							msg.reply(new JsonObject().putString("status", "ok"));
						}

						@Override
						public void onError(ConnectorError e) {
							msg.reply(new JsonObject().putString("error", e.getError()).putString("message", e.getMessage()));
						}
						
					});
				} catch (ConnectorException e) {
					msg.reply(new JsonObject().putString("error", e.getMessage()));
					log.e("",e);
				}
				
			}
		});
		
		openxsp.eventBus().registerHandler(EVENT_GETVIDEOS, new Handler<Message<String>>() {

			@Override
			public void handle(final Message<String> msg) {
				JsonObject json = new JsonObject(msg.body());
				
				JsonObject cdnConfig = json.getObject(PARAM_CDN_CONFIG);
				
				CdnConnector connector;
				try {
					connector = ConnectorFactory.getConnector(cdnConfig);
					connector.getUploadedVideos(cdnConfig, new ConnectorCallback(){

						@Override
						public void onSuccess(String connectorId, JsonObject result) {
							msg.reply(result);
						}

						@Override
						public void onError(ConnectorError e) {
							msg.reply(new JsonObject().putString("error", e.getError()).putString("message", e.getMessage()));
						}
						
					});
				} catch (ConnectorException e) {
					msg.reply(new JsonObject().putString("error", e.getMessage()));
					log.e("",e);
				}
			}
		});
	}

	/*
	 * Registers the module in the service registry
	 */
	private void publishServiceRegistryInformation() {

		if (this.serviceregistryEvent == null) {
			log.w("Missing service registry event");
			return;
		}

		// TODO publish event to service registry with service information of
		// this module

		log.i("Publish to service registry not yet implemented");

	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		if (fileUploadHandler != null)
			openxsp.eventBus().unregisterHandler(fileUploadHandler.getEvent(), fileUploadHandler);

	}

	private class SessionEventHandler implements Handler<Message<JsonObject>> {

		public String event;
		
		private NetworkService networkService;

		public SessionEventHandler(String event) {
			this.event = event;
		}

		public String getEvent() {
			return event;
		}
		
		private NetworkService getNetworkService(){
			if(this.networkService==null){
				this.networkService = new VertxNetworkService(openxsp, container.config(), event, sessionStorage, handlerStorage);
			}
			
			return networkService;
		}

		@Override
		public void handle(Message<JsonObject> msg) {
			log.v("Received event: " + msg.body());
			// dispatch message
			JsonObject msgJsonBody = msg.body();
			
			final SessionControlMessage scm = SessionControlFactory.parse(msgJsonBody);

			// dispatch session operation
			switch (scm.getMethod()) {
			case INIT: {
				log.i("Received creation of a new session");
				SessionControlCreateSession create = (SessionControlCreateSession) scm;

				JsonObject response = new JsonObject();
				response.putString("status", "ok");
				msg.reply(response);

				// map session id with event address
				registerRemoteEvent(create.getSessionID(), create.getAddress());
				
				
				JsonObject content = new JsonObject(create.getContent());
				  
				if (event.equals(EVENT_UPLOADFILE)) {
					String sdp = content.getString(PARAM_SDP);
					if(sdp==null){
						log.w("Missing sdp");
						getNetworkService().cancelSession("", create.getSessionID(), "Missing SDP content", "plain/text", null);
						return;
					}
					try{
						FileUploadSessionHandler handler = new FileUploadSessionHandler(uploadNetworkService, content.getObject(PARAM_CDN_CONFIG), repo);
						registerHandler(create.getSessionID(), handler);
						handler.onSessionCreate("", create.getSessionID(), sdp, create.getContentType());
					} catch (ConnectorException e) {
						log.w("could not create connector. Maybe the connector parameters are not valid?",e);
						
						getNetworkService().cancelSession("", create.getSessionID(), "Could not create connector", "plain/text", null);
						return;
					}
					
				}
				else if (event.equals(EVENT_DOWNLOAD)) {
					final String sdp = content.getString(PARAM_SDP);
					if(sdp==null){
						log.w("Missing sdp");
						getNetworkService().cancelSession("", create.getSessionID(), "Missing SDP content", "plain/text", null);
						return;
					}
					
					try {
						JsonObject cdnConfig = content.getObject(PARAM_CDN_CONFIG);
						CdnConnector connector = ConnectorFactory.getConnector(cdnConfig);
						connector.downloadVideo(cdnConfig,cdnConfig.getString("id"), new ConnectorCallback(){

							@Override
							public void onSuccess(String connectorId, JsonObject result) {
								//store the file into repo
								repo.uploadFile(result.getString("fileName"), new UploadCallback() {
									
									@Override
									public void onUploadError(String path, String error) {
										log.w("File Upload Error "+ error);
										// send cancel request
										downloadNetworkservice.cancelSession("error", scm.getSessionID(), error, "plain/text", null);
									}
									
									@Override
									public void onFileUploaded(String id) {
										log.v("file uploaded to repository");
										
										FileDownloadSessionHandler downloadSessionHandler = new FileDownloadSessionHandler(kurentoAddress, id, downloadNetworkservice);
										registerHandler(scm.getSessionID(), downloadSessionHandler);
										downloadSessionHandler.onSessionCreate("", scm.getSessionID(), sdp, "application/sdp");
									}
								});
								
							}

							@Override
							public void onError(ConnectorError e) {
								log.w("ConnectorError: "+e.toString());
								downloadNetworkservice.cancelSession("error", scm.getSessionID(), e.toString(), "plain/text", null);
							}
							
						});
					} catch (ConnectorException e1) {
						log.e("",e1);
						downloadNetworkservice.cancelSession("error", scm.getSessionID(), e1.getMessage(), "plain/text", null);
					}					
				}
				
				// TODO HANDLE UPDSTREAM and DOWNSTREAM events
				
				else{
					log.w("Cannot handle event " + event);
					
					downloadNetworkservice.cancelSession("error", scm.getSessionID(), "Service not implemented", "plain/text", null);
				}
					
				

			}
				break;

			case ACCEPT: {
				log.i("Received accept session");
				SessionControlAcceptSession accept = (SessionControlAcceptSession) scm;
				SessionHandler handler = getSessionHandler(accept.getSessionID());
				if (handler == null) {
					log.i("Could not find handler for session with id " + accept.getSessionID());

					JsonObject response = new JsonObject();
					response.putString("status", "error");
					response.putString("message", "Session not found");
					msg.reply(response);
					break;
				}

				JsonObject response = new JsonObject();
				response.putString("status", "ok");
				msg.reply(response);

				// map session id with event address
				registerRemoteEvent(accept.getSessionID(), accept.getAddress());

				handler.onSessionAccepted("action", accept.getSessionID(), accept.getContent(), accept.getContentType());
			}
				break;

			case CONFIRM: {
				log.i("Received confirmation of session");
				SessionControlConfirmSessionEstablishment confirm = (SessionControlConfirmSessionEstablishment) scm;
				SessionHandler handler = getSessionHandler(confirm.getSessionID());
				if (handler == null) {
					log.i("Could not find handler for session with id " + confirm.getSessionID());

					JsonObject response = new JsonObject();
					response.putString("status", "error");
					response.putString("message", "Session not found");
					msg.reply(response);
					break;
				}

				JsonObject response = new JsonObject();
				response.putString("status", "ok");
				msg.reply(response);

				// map session id with event address
				registerRemoteEvent(confirm.getSessionID(), confirm.getAddress());

				handler.onSessionComfirmed("action", confirm.getSessionID(), confirm.getContent(), confirm.getContentType());
			}
				break;

			case CANCEL: {
				log.i("Received cancel session");

				SessionControlCancelSession cancel = (SessionControlCancelSession) scm;
				SessionHandler handler = getSessionHandler(cancel.getSessionID());
				if (handler == null) {
					log.i("Could not find handler for session with id " + cancel.getSessionID());

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

			case END: {
				log.i("Received end session");
				SessionControlEndSession end = (SessionControlEndSession) scm;
				SessionHandler handler = getSessionHandler(end.getSessionID());
				if (handler == null) {
					log.i("Could not find handler for session with id " + end.getSessionID());

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

			case UPDATE: {
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
				response.putString("message", "Unsupported method \"" + scm.getMethod() + "\"");
				msg.reply(response);
			}
		}
	};
	
	
	private class MessageEventHandler implements Handler<Message<JsonObject>> {

		public String event;
		
		public MessageEventHandler(String event) {
			this.event = event;
		}

		public String getEvent() {
			return event;
		}

		@Override
		public void handle(Message<JsonObject> msg) {
			log.v("Received event: " + msg.body());
			// dispatch message
			JsonObject msgJsonBody = msg.body();
			
			SessionControlMessage scm = SessionControlFactory.parse(msgJsonBody);

			// dispatch session operation
			switch (scm.getMethod()) {
				case MESSAGE:
				default:
					log.w("Received request contains unsupported method field " + scm.getMethod());
					JsonObject response = new JsonObject();
					response.putString("status", "error");
					response.putString("message", "Unsupported method \"" + scm.getMethod() + "\"");
					msg.reply(response);
			}
		}
	};

	private String getUserEvent(String to) {
		// TODO get remote event address from user registry
		return to;
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

	private SessionHandler getSessionHandler(String sessionId) {
		return handlerStorage.get(sessionId);
	}

	private void registerRemoteEvent(String sessionId, String remoteEvent) {
		sessionStorage.put(sessionId, remoteEvent);
	}
	
}
