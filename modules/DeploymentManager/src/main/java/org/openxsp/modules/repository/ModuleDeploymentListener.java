package org.openxsp.modules.repository;

interface ModuleDeploymentListener {
	void moduleDeployed(Deployment d);
	
	void moduleUndeployed(Deployment d);
	
	void moduleInstalled(Module m);

	void moduleModified(Module m);

	void moduleUninstalled(Module m);
	
	void onError(String error);
}
