package org.openxsp.modules.repository;

public interface DeploymentHandler {
	
	void onSuccess(Deployment deployment);
	
	void onError(String message, String moduleId);

}
