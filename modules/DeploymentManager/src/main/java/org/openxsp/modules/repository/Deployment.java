package org.openxsp.modules.repository;

import org.openxsp.modules.util.Logger;
import org.openxsp.modules.util.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

public class Deployment {
	
	public static final String PARAM_DEPLOYMENT_ID = "deployment_id",
			PARAM_MODULE = "module";
	private static Logger log = LoggerFactory.getLogger(Deployment.class.getSimpleName());
	
	private String deploymentID;
	private Module deployedModule;

	/* Constructor */
	public Deployment(String deploymentID, Module deployedModule) {
		this.deploymentID = deploymentID;
		this.deployedModule = deployedModule;
	}

	/* Accessors */
	public String getDeploymentID() {
		return deploymentID;
	}

	public void setDeploymentID(String deploymentID) {
		this.deploymentID = deploymentID;
	}

	public Module getDeployedModule() {
		return deployedModule;
	}

	public static Deployment fromJson(JsonObject body) {
		
		log.v("parsing "+body);
		
		String id = body.getString(PARAM_DEPLOYMENT_ID);
		JsonObject moduleJson = body.getObject(PARAM_MODULE);
		
		if(moduleJson==null){
			System.out.println("Missing module description");
			return null;
		}
		
		Module m = Module.fromJson(moduleJson);
		
		if(id!=null && moduleJson!=null) return new Deployment(id, m);
		
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Deployment)) return false;
		
		Deployment d = (Deployment)obj;
		
		if(!deploymentID.equals(d.getDeploymentID())) return false;
		
		if(!deployedModule.equals(d.getDeployedModule())) return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return deploymentID.hashCode();
	}

//	public void setDeployedModule(Module deployedModule) {
//		this.deployedModule = deployedModule;
//	}

}
