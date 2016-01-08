package org.openxsp.modules.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openxsp.modules.repository.Deployment;
import org.openxsp.modules.repository.Module;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class JsonFactory {

	public static JsonObject getAllModulesJson(Collection<Module> modules, Collection<Deployment> deployments){
		JsonObject allModules = new JsonObject();
		
		if(modules!=null){
			JsonArray modulesArray = new JsonArray();
			for (Module module : modules) {
				//modulesArray.addObject(new JsonObject().putString(Module.NAME_FIELD, module));
				modulesArray.addObject(module.toJson());
			}
			allModules.putArray("modules", modulesArray);
		}
		
		if(deployments!=null){
			JsonArray deploymentsArray = new JsonArray();
			for (Deployment deployment : deployments) {
				//deploymentsArray.addObject(new JsonObject().putString(Module.NAME_FIELD, deployment.getDeployedModule()).putString("deployment_id", deployment.getDeploymentID()));
				deploymentsArray.addObject(new JsonObject().putObject(Deployment.PARAM_MODULE, deployment.getDeployedModule().toJson()).putString("deployment_id", deployment.getDeploymentID()));
			}
			allModules.putArray("deployments", deploymentsArray);
		}
		
		
		return allModules;
	}
	
	public static Collection<Module> getModules(JsonObject allModuleJson){
		JsonArray installedModules = allModuleJson.getArray("modules");
		Iterator<Object> iter = installedModules.iterator();
		
		Set<Module> modules = new HashSet<>();
		while(iter.hasNext()){
			JsonObject mod = (JsonObject) iter.next();
			Module m = Module.fromJson(mod);
			modules.add(m);
		}
		return modules;
	}
	
	public static Collection<Deployment> getDeployedModules(JsonObject allModuleJson){
		JsonArray installedModules = allModuleJson.getArray("deployments");
		Iterator<Object> iter = installedModules.iterator();
		
		Set<Deployment> deployments = new HashSet<>();
		while(iter.hasNext()){
			JsonObject mod = (JsonObject) iter.next();
			Deployment m = Deployment.fromJson(mod);
			deployments.add(m);
		}
		return deployments;
	}
}
