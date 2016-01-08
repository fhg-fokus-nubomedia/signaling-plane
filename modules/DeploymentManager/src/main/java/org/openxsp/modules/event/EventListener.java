package org.openxsp.modules.event;

import org.openxsp.modules.repository.Deployment;
import org.openxsp.modules.repository.Module;

public interface EventListener {

	public final static String EVENT_MODULE_INSTALLED = "deployment-manager.client.module-installed",
			EVENT_MODULE_DEPLOYED = "deployment-manager.client.module-deployed",
			EVENT_MODULE_UNDEPLOYED = "deployment-manager.client.module-undeployed",
			EVENT_MODULE_UNINSTALLED = "deployment-manager.client.module-uninstalled",
			EVENT_MODULE_MODIFIED = "deployment-manager.client.module-modified";

	void onModuleModified(Module m);

	void onModuleUninstalled(Module m);

	void onModuleInstalled(Module m);

	void onModuleDeployed(Deployment deployment);

	void onModuleUndeployed(Deployment deployment);
}
