package org.openxsp.modules.repository;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openxsp.java.Verticle;
import org.openxsp.modules.event.EventListener;
import org.openxsp.modules.util.Logger;
import org.openxsp.modules.util.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

public class ModuleRegistry implements ModuleDeploymentListener {

	private static Logger log = LoggerFactory.getLogger(ModuleRegistry.class);
	
	private Set<EventListener> listener = new HashSet<EventListener>();

	private  LocalRepositoryMonitor fileSystemMonitor = null;

	private Set<Module> modules = new HashSet<Module>();
	private Map<Module, List<Deployment>> deployments = new HashMap<>();

	private Verticle verticle;
	
	private String address;
	
	private static ModuleRegistry instance;
	
	/* Constructors */
	private ModuleRegistry(Verticle verticle, String address) {
		
		this.verticle = verticle;
		
		this.address = address;
		
		init();
	}
	
	public static ModuleRegistry createInstance(Verticle verticle, String address){
		instance = new ModuleRegistry(verticle, address);
		return instance;
	}
	
	public static ModuleRegistry getInstance(){
		return instance;
	}
	
	private void init(){
		
		log.v("Initializing ...");
		
		try {
			this.fileSystemMonitor = new LocalRepositoryMonitor(verticle.getOpenXSP(), address);
			this.fileSystemMonitor.start(verticle.getContainer().config(), this);
		} catch (Exception e) {
			log.e(e.getMessage(),e);
		}
		
		/* FIXME check if this is needed
		try{
			UploadMonitor uploadMonitor = new UploadMonitor(this.verticle.getVertx(), "mods2"); //FIXME
			uploadMonitor.addEventHandler(FileSystemEvent.CREATED, new FileSystemEventHandler(){

				@Override
				public void onNewEvent(FileSystemEvent event, final String path) {
					if (event.equals(FileSystemEvent.CREATED)) {
						// try to deploy module
						try {
							
							log.i("New file uploaded "+path);
							
							Module zipModule = new Module(path, address, new JsonObject());
							
							moduleInstalled(zipModule);
							
						} catch (Exception e) {
							log.e("Deployment failed", e);
						}
					}
					else{
						//container.logger().info(event+ " event not supported");
					}
				}
			});
			
			uploadMonitor.start();
		}
		catch(Exception e){
			log.e(e.getMessage(),e);
		}
		*/
	}
	

//	public ModuleRegistry(final Verticle verticle, final String modulesDir, final EventListener eventEmitter) throws Exception {
//		this(verticle, Paths.get(modulesDir), eventEmitter);
//	}

	/* Accessors */
	public Set<Module> getModules() {
		
		return modules;
		// XXX return this.modules.values().toArray(new
		// Module[this.modules.size()]);

	}

	public Collection<Deployment> getDeploymedModules() {

		if (deployments != null) {
			List<Deployment> result = new LinkedList<>();

			for (List<Deployment> deploymentList : this.deployments.values()) {
				result.addAll(deploymentList);
			}
			return result;
		}

		return null;

		/*
		 * XXX List<Deployment> result = new LinkedList<>();
		 * 
		 * for (List<Deployment> deploymentList : this.deployments.values()) {
		 * result.addAll(deploymentList); }
		 * 
		 * return result.toArray(new Deployment[result.size()]);
		 */
	}

//	public Module getModuleWithName(String name) throws Exception {
//		if (!this.hasModuleWithName(name)) {
//			// TODO: Custom Exception
//			throw new Exception("Module Not Found");
//		}
//
//		return this.modules.get(name);
//	}

//	public boolean hasModuleWithName(String name) {
//		return this.modules.contains(name);
//	}

	/* Deployment Methods */
	public void deployModule(final Module m, final DeploymentHandler handler) throws Exception {
		
		log.i("Deploying module "+m.getName());
		log.v(m.toJson().toString());


		if(!m.getRepositoryAddress().equals(this.address)){
			log.i("The module belongs to another instance - forwarding the reqeust to the correct repository");
			this.verticle.getOpenXSP().eventBus().send(m.getRepositoryAddress()+".deploy-module", m.toJson(), new Handler<AsyncResult<Message<JsonObject>>>() {

				@Override
				public void handle(AsyncResult<Message<JsonObject>> msg) {
					if(msg.succeeded()){
						handler.onSuccess(Deployment.fromJson(msg.result().body()));
					}
					else{
						handler.onError(msg.cause().getMessage(), m.getName());
						log.e("Could not deploy modules", msg.cause());
					}
				}
			});
		}
		else{
			if(!(m.getName().endsWith(".zip") || m.getName().endsWith(".jar"))){
				this.verticle.getContainer().deployModule(m.getName(), m.getConfig(), 1, new Handler<AsyncResult<String>>() {

					@Override
					public void handle(AsyncResult<String> res) {
						
						if(res.failed()){
							log.w("Could not deploy module");
							if(res.cause()!=null)	handler.onError(res.cause().getMessage(), m.getName());
							else handler.onError("Error deploying module", m.getName());
						}
						else{
							log.i("Deployment done : " + res.result());
							
							List<Deployment> d = deployments.get(m);
							if(d==null){
								d = new ArrayList<>();
								deployments.put(m, d);
							}
							
							Deployment deployment = new Deployment(res.result(), m);
							d.add(deployment);
							handler.onSuccess(deployment);
							
							for(EventListener l : ModuleRegistry.this.listener) l.onModuleDeployed(deployment);
						}
					}
				});
			}
			else{
				deployZipModule(m, handler);
			}
			
		}
	}
	
	private void deployZipModule(final Module m, final DeploymentHandler handler){
		log.d("Deploying module from zip file");
		
		String path = verticle.getContainer().config().getString("module_folder")+FileSystems.getDefault().getSeparator()+m.getName();
		PlatformManager pMan = PlatformLocator.factory.createPlatformManager();

		log.v("Path of zip file is "+path );
		
		pMan.deployModuleFromZip(path, m.getConfig(), 1, new Handler<AsyncResult<String>>() {

			@Override
			public void handle(AsyncResult<String> res) {
				if (res.failed()) {
					log.w("Deployment failed ",res.cause());
					return;
				}

				log.d("Deployment of module " + m.getName() + " successful");
				
				List<Deployment> d = deployments.get(m);
				if(d==null){
					d = new ArrayList<>();
					deployments.put(m, d);
				}
				
				Deployment deployment = new Deployment(res.result(), m);
				d.add(deployment);
				handler.onSuccess(deployment);
				
				for(EventListener l : ModuleRegistry.this.listener) l.onModuleDeployed(deployment);
			}
		});
	}

	public void undeployModule(final Module m, final DeploymentHandler handler) throws Exception {

//		final Module module = this.getModuleWithName(moduleName);
		final List<Deployment> deployments = this.deployments.get(m);
		
		if(deployments==null || deployments.size()==0){
			log.d("Could not find deployments of module "+m.getName());
			handler.onError("Not found", m.getName());
			return;
		}

		if(m.getRepositoryAddress()!=null && !m.getRepositoryAddress().equals(this.address)){
			log.i("The module belongs to another instance - forwarding the request to the corresponding repository");
			this.verticle.getOpenXSP().eventBus().send(m.getRepositoryAddress()+".undeploy-module", m.toJson(), new Handler<AsyncResult<Message<JsonObject>>>() {

				@Override
				public void handle(AsyncResult<Message<JsonObject>> msg) {
					if(msg.succeeded()){
						handler.onSuccess(Deployment.fromJson(msg.result().body()));
					}
					else{
						handler.onError(msg.cause().getMessage(), m.getName());
						log.e("Could not undeploy modules", msg.cause());
					}
				}
			});
		}
		else{
			
			Deployment deployment = null;
			
			//try to find a deployment in this container
			for(Deployment d : deployments){
				if(d.getDeployedModule().getRepositoryAddress()!=null && d.getDeployedModule().getRepositoryAddress().equals(address)){
					deployment = d;
					break;
				}
			}
			
			if(deployment == null){
				log.d("Could not find local deployment of module "+m.getName());
				handler.onError("Could not find deployed module", m.getName());
				return;
			}
			
			final Deployment d = deployment;
			
			this.verticle.getContainer().undeployModule(deployment.getDeploymentID(), new Handler<AsyncResult<Void>>() {
				@Override
				public void handle(AsyncResult<Void> res) {
					
					if(res.succeeded()){
						
						log.d("Undeployment Done : " + d.getDeploymentID());
						
						handler.onSuccess(d);
						
						for(EventListener l : ModuleRegistry.this.listener) l.onModuleUndeployed(d);
					}
					else{
						if(res.cause()!=null)	handler.onError(res.cause().getMessage(), m.getName());
						else handler.onError("Error undeploying module", m.getName());
					}
				}
			});
		}
	}


	/* Module Loader Delegate methods */
	@Override
	public void moduleInstalled(Module m) {
		
		log.d("Adding installed module "+m.getName());
		
		this.modules.add(m);
		this.deployments.put(m, new LinkedList<Deployment>());

		if(this.address.equals(m.getRepositoryAddress())){
			for(EventListener l : this.listener) l.onModuleInstalled(m);
		}
		
	}

	@Override
	public void moduleModified(Module m) {
		/* Ignore modification events on deleted modules */
		if (!this.modules.contains(m)) {
			return;
		}
		if(this.address.equals(m.getRepositoryAddress())){
			for(EventListener l : this.listener) l.onModuleModified(m);
		}
		
	}

	@Override
	public void moduleUninstalled(Module m) {
		
		log.d("Removing uninstalled module "+m.getName());
		
		this.modules.remove(m);
		this.deployments.remove(m);

		if(this.address.equals(m.getRepositoryAddress())){
			for(EventListener l : this.listener) l.onModuleUninstalled(m);
		}
	}

	public boolean addEventListener(EventListener listener){
		if(this.listener == null) this.listener = new HashSet<EventListener>();
		
		return this.listener.add(listener);
	}
	
	public boolean removeListener(EventListener listener){
		if(this.listener!=null) return this.listener.remove(listener);
		
		return false;
	}

	@Override
	public void onError(String error) {
		log.e("Module repository error: "+error);
	}

	@Override
	public void moduleDeployed(Deployment d) {
		
		log.d("Adding deployed module "+d.getDeployedModule().getName()+" with deployment id "+d.getDeploymentID());
		
		List<Deployment> deployments = this.deployments.get(d.getDeployedModule());
		
		if(deployments==null){
			deployments = new ArrayList<Deployment>();
			this.deployments.put(d.getDeployedModule(), deployments);
		}
		
		deployments.add(d);

		if(this.address.equals(d.getDeployedModule().getRepositoryAddress())){
			for(EventListener l : this.listener) l.onModuleDeployed(d); 
		}
	}

	@Override
	public void moduleUndeployed(Deployment m) {
		
		log.d("Removing undeployed module "+m.getDeployedModule().getName()+" with deployment id "+m.getDeploymentID());
		
		List<Deployment> deployments = this.deployments.get(m.getDeployedModule());
		
		if(deployments==null){
			log.d("No deployments stored");
			return;
		}
		
		if(deployments.remove(m)){
			if(this.address.equals(m.getDeployedModule().getRepositoryAddress())){
				for(EventListener l : this.listener) l.onModuleUndeployed(m); 
			}
		}
	}
}
