package org.openxsp.modules.web;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.openxsp.modules.repository.ModuleRegistry;
import org.openxsp.modules.util.JsonFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class ApiHandler implements Handler<HttpServerRequest> {

	private static final String API_PATH = "/api",
			GET_ALL_MODULES = API_PATH+"/getmodules",
			GET_DEPLOYED_MODULES = API_PATH+"/getdeployments",
			GET_INSTALLED_MODULES = API_PATH+"/installedmodules";
	
	private static final Path MODULES_PATH = Paths.get("api/getinstalledmodules");

	//private ModuleRegistry registry;
	
	
	@Override
	public void handle(HttpServerRequest request) {
		String path = Paths.get(request.uri()).toString();

		System.out.println("handling request for "+path);
		
		System.out.println(path+".equals("+GET_ALL_MODULES+")? "+path.equals(GET_ALL_MODULES));
		
		if (path.equals(GET_ALL_MODULES)) {
			System.out.println("Getting all modules");
			request.response().setStatusCode(200);
			JsonObject json = JsonFactory.getAllModulesJson(ModuleRegistry.getInstance().getModules(), ModuleRegistry.getInstance().getDeploymedModules());
			request.response().end(json.toString());
			
			return;
		}

		request.response().setStatusCode(404);
		request.response().end("");
	}

	private void listModules(HttpServerRequest request) {
		request.response().end("Modules");
	}
}
