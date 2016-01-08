package org.openxsp.modules;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.openxsp.java.Verticle;
import org.openxsp.modules.event.EventListener;
import org.openxsp.modules.event.VertxEventEmitter;
import org.openxsp.modules.repository.Deployment;
import org.openxsp.modules.repository.DeploymentHandler;
import org.openxsp.modules.repository.Module;
import org.openxsp.modules.repository.ModuleRegistry;
import org.openxsp.modules.util.JsonFactory;
import org.openxsp.modules.util.Logger;
import org.openxsp.modules.util.LoggerFactory;
import org.openxsp.modules.web.ApiHandler;
import org.openxsp.modules.web.FileHandler;
import org.openxsp.modules.web.UploadHandler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;
import static org.openxsp.modules.event.EventListener.*;

public class DeploymentManager extends Verticle{
	private static Logger log;
	
	private static String host = "0.0.0.0", moduleFolder = "mods", uploadUrl;
	private static int port = 8080;
	
	private static FileHandler fileHandler = new FileHandler();
	private static ApiHandler apiHandler = new ApiHandler();
    private static UploadHandler uploadHandler;
	
	public static final String
		EVENT_BASE = DeploymentManager.class.getSimpleName().toLowerCase(),
		LOCAL_EVENT_BASE = EVENT_BASE+"."+UUID.randomUUID().toString().substring(0, 4),
		//EVENT_BASE = "deployment-manager.server.",
		EVENT_LOAD_MODULES = /*"deployment-manager.server.load-all",*/EVENT_BASE+".load-all",
		EVENT_GET_INSTALLED_MODULES = EVENT_BASE+".list-modules", 
		EVENT_GET_DEPLOYED_MODULES = EVENT_BASE+".list-deployments",
		EVENT_DEPLOY_MODULE = LOCAL_EVENT_BASE+".deploy-module",
		EVENT_UNDEPLOY_MODULE = LOCAL_EVENT_BASE+".undeploy-module",
		
		CONFIG_PARAM_HOST = "lokal_server_host",
		CONFIG_PARAM_PORT= "lokal_server_port",
		CONFIG_PARAM_MODULEFOLDER = "module_folder",
		
		PARAM_UPLOAD_URL = "upload_url",
		UPLOAD_PATH = "/upload";

	private ModuleRegistry moduleRegistry;
	
	
	@Override
	public void start() {
		super.start();
		
		log = LoggerFactory.getLogger(DeploymentManager.class);
		
		log.v("Local event base is "+LOCAL_EVENT_BASE);
		
		moduleRegistry = ModuleRegistry.createInstance(this, LOCAL_EVENT_BASE);
		moduleRegistry.addEventListener(new VertxEventEmitter(this));
		
		//read config
		host = getContainer().config().getString(CONFIG_PARAM_HOST, host);
		port = getContainer().config().getInteger(CONFIG_PARAM_PORT, port);
		moduleFolder = getContainer().config().getString(CONFIG_PARAM_MODULEFOLDER, moduleFolder);
		if(getContainer().config().containsField(PARAM_UPLOAD_URL)){
			uploadUrl = getContainer().config().getString(PARAM_UPLOAD_URL);
		}
		
		loadModules(new Notifier() {
			
			@Override
			public void done() {
				registerApiEvents();
				registerDeploymentEventHandler();
				
				log.d("Deployment Manager module started");
				
				String modules = "";
				for(Module m : moduleRegistry.getModules()){
					modules+=m.getName()+" ";
				}
				log.v("Installed modules: "+modules);
				
				String deployments = "";
				for(Deployment d : moduleRegistry.getDeploymedModules()){
					deployments+= d.getDeployedModule().getName()+" ";
				}
				log.v("Deployed modules: "+deployments);
				
				log.v("File upload url: "+getUploadUrl());
			}
		});
		
		startServer();
	}
	
	public interface Notifier{
		void done();
	}
	
	/*
	 * Discovers the modules that are already installed
	 */
	private void loadModules(final Notifier n){
		openxsp.eventBus().send(EVENT_LOAD_MODULES, "", new Handler<AsyncResult<Message<JsonObject>>>() {

			@Override
			public void handle(AsyncResult<Message<JsonObject>> msg) {
				
				if(msg.succeeded()){
					JsonObject moduleInfo = msg.result().body();
					
					log.v("received existing modules description: "+moduleInfo);
					
					for(Deployment d : JsonFactory.getDeployedModules(moduleInfo)){
						moduleRegistry.moduleDeployed(d);
					}

					for (Module m : JsonFactory.getModules(moduleInfo)){
						moduleRegistry.moduleInstalled(m);
					}
					
//					if(moduleInfo.containsField(PARAM_UPLOAD_URL)){
//						uploadUrl = moduleInfo.getString(PARAM_UPLOAD_URL);
//					}
//					else{
//						uploadUrl = getUploadUrl();
//					}
				}
				else{
					log.d("could not load modules");
				}
				
				n.done();
			}
		});
	}
	
	protected String getUploadUrl() {
		
		if(uploadUrl==null) uploadUrl = "http://"+host+":"+port+UPLOAD_PATH+"/";
		
		return uploadUrl;
	}

	private void startServer(){

        uploadHandler = new UploadHandler(openxsp, moduleFolder);

		HttpServer server = openxsp.createHttpServer();
		server.requestHandler(new Handler<HttpServerRequest>() {

			@Override
			public void handle(HttpServerRequest request) {
				Path path = Paths.get(request.uri());

                log.v("HTTP request path is: "+path);

                //handle api requests
				if (path.startsWith("/api")) {
					apiHandler.handle(request);
					return;
				}

				//handle upload requests
                if (path.startsWith(UPLOAD_PATH)) {
                	log.i("handling upload");
                    uploadHandler.handle(request);
                    return;
                }
                
                log.w("handling file request");
                //handle file requests
				fileHandler.handle(request);
			}
		});

		SockJSServer sockjsServer = openxsp.createSockJSServer(server);

		JsonObject config = new JsonObject().putString("prefix", "/eventbus");
		JsonArray inbound = new JsonArray();
		inbound.addObject(new JsonObject().putString("address_re", DeploymentManager.EVENT_BASE+"\\..+"));
		JsonArray outbound = new JsonArray();
		outbound.addObject(new JsonObject().putString("address_re", "deployment-manager\\.client\\..+"));

		sockjsServer.bridge(config, inbound, outbound);

		server.listen(port, host);
		
		log.i("Started server on "+host+":"+port);
	}
	
	private void registerApiEvents(){
		
		log.v("Registering API events: ");
		log.v("Load modules event: "+EVENT_LOAD_MODULES);
		log.v("Deploy module event: "+EVENT_DEPLOY_MODULE);
		log.v("Undeploy module event: "+EVENT_UNDEPLOY_MODULE);
		log.v("Get deployed modules event: "+EVENT_GET_DEPLOYED_MODULES);
		log.v("Get installed modules event: "+EVENT_GET_INSTALLED_MODULES);
		/*
		 * API events
		 */
		
		this.registerHandler(EVENT_LOAD_MODULES, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				log.i("EventBus Received Message for \""+EVENT_LOAD_MODULES+"\": Load All Data");

				JsonObject replyMessage = JsonFactory.getAllModulesJson(moduleRegistry.getModules(), moduleRegistry.getDeploymedModules());

				replyMessage.putString(PARAM_UPLOAD_URL, getUploadUrl());
				
				log.v("Replying: "+replyMessage);
				
				message.reply(replyMessage);
			}
		});
		
		this.registerHandler(EVENT_GET_INSTALLED_MODULES, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				log.i("EventBus Received Message: List Modules");
				JsonObject replyMessage = JsonFactory.getAllModulesJson(moduleRegistry.getModules(), null);;

				log.v("Replying: "+replyMessage);
					
				message.reply(replyMessage);
			}
		});

		this.registerHandler(EVENT_GET_DEPLOYED_MODULES, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> message) {
				log.i("EventBus Received Message: List Deployments");
				JsonObject replyMessage = JsonFactory.getAllModulesJson(null, moduleRegistry.getDeploymedModules());

				log.v("Replying: "+replyMessage);
				
				message.reply(replyMessage);
			}
		});

		this.registerHandler(EVENT_DEPLOY_MODULE, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(final Message<JsonObject> message) {
				log.i("EventBus Received Message: Deploy Module");
				
				Module m = Module.fromJson( message.body());
				
				//m.setRepositoryAddress(LOCAL_EVENT_BASE);
				
				try {
					DeploymentManager.this.moduleRegistry.deployModule(m, new DeploymentHandler() {
						
						@Override
						public void onSuccess(Deployment deployment) {
							log.d("Successful deployment "+deployment.getDeploymentID()+" of module "+deployment.getDeployedModule().getName());
							message.reply(new JsonObject().putString("success", "1"));
						}
						
						@Override
						public void onError(String msg, String moduleId) {
							log.w("Could not deploy module "+moduleId+": "+msg);
							
							message.reply(new JsonObject().putString("success", "0"));
						}
					});
				} catch (Exception e) {
					log.e("Error while deploying module",e);
					message.reply(new JsonObject().putString("success", "0"));
				}
			}
		});

		this.registerHandler(EVENT_UNDEPLOY_MODULE, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(final Message<JsonObject> message) {
				log.i("EventBus Received Message: Undeploy Module");
				
				Module m = Module.fromJson(message.body());
				
				if(m==null){
					log.w("Could not parse module information");
					return;
				}
				
				try {
					DeploymentManager.this.moduleRegistry.undeployModule(m, new DeploymentHandler() {
						
						@Override
						public void onSuccess(Deployment deployment) {
							log.d("Successfully undeployed deployment "+deployment.getDeploymentID()+" of module "+deployment.getDeployedModule().getName());
							message.reply(new JsonObject().putString("success", "1"));
						}
						
						@Override
						public void onError(String msg, String moduleId) {
							log.w("Could not undeploy module "+moduleId+": "+msg);
							message.reply(new JsonObject().putString("success", "0"));
						}
					});
					
				} catch (Exception e) {
					log.e("Could not undeploy module",e);
					message.reply(new JsonObject().putString("success", "0"));
				}
			}
		});
	}
	
	/*
	 * Events for deployment notifications of other Deployment Manager module instances
	 */
	private void registerDeploymentEventHandler(){

		log.v("Registering deployment events");
		
		this.registerHandler(EVENT_MODULE_INSTALLED, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(final Message<JsonObject> message) {
				Module m = Module.fromJson(message.body());
				
				if(!LOCAL_EVENT_BASE.equals(m.getRepositoryAddress())){
					log.d("EventBus Received Message: module installed by "+m.getRepositoryAddress());

					DeploymentManager.this.moduleRegistry.moduleInstalled(m);
				}
				else log.v("Received own module installed event");
			}
		});
		
		this.registerHandler(EVENT_MODULE_DEPLOYED, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(final Message<JsonObject> message) {
				Deployment d = Deployment.fromJson(message.body());
				
				if(d.getDeployedModule()==null){
					log.w("Could not get information about deployed module");
					return;
				}
				if(!LOCAL_EVENT_BASE.equals(d.getDeployedModule().getRepositoryAddress())){
					log.d("EventBus Received Message: module deployed by "+d.getDeployedModule().getRepositoryAddress());

					DeploymentManager.this.moduleRegistry.moduleDeployed(d);
				}
				else log.v("Received own module deployed event");
			}
		});
		
		this.registerHandler(EVENT_MODULE_UNDEPLOYED, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(final Message<JsonObject> message) {
				Deployment d = Deployment.fromJson(message.body());
				
				if(d.getDeployedModule()==null){
					log.w("Could not get information about undeployed module");
					return;
				}
				if(!LOCAL_EVENT_BASE.equals(d.getDeployedModule().getRepositoryAddress())){
					log.d("EventBus Received Message: module undeployed by "+d.getDeployedModule().getRepositoryAddress());

					DeploymentManager.this.moduleRegistry.moduleUndeployed(d);
				}
				else log.v("Received own module undeployed event");
			}
		});
		
		this.registerHandler(EVENT_MODULE_UNINSTALLED, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(final Message<JsonObject> message) {
				Module m = Module.fromJson(message.body());
				
				if(!LOCAL_EVENT_BASE.equals(m.getRepositoryAddress())){
					log.d("EventBus Received Message: module uninstalled by "+m.getRepositoryAddress());

					DeploymentManager.this.moduleRegistry.moduleUninstalled(m);
				}
				else log.v("Received own module uninstalled event");
			}
		});
	}
	
	private void registerHandler(final String address, final Handler<Message<JsonObject>> handler) {
		openxsp.eventBus().registerHandler(address, handler);
	}
	
}
