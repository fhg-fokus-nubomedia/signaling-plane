package org.openxsp.modules.repository;

import org.vertx.java.core.json.JsonObject;

public interface RepositoryMonitor {

	void start(JsonObject config, ModuleDeploymentListener listener);
	
	void stop();
}
