package org.openxsp.modules.event;

import org.openxsp.java.Verticle;
import org.openxsp.modules.repository.Deployment;
import org.openxsp.modules.repository.Module;
import org.openxsp.modules.util.Logger;
import org.openxsp.modules.util.LoggerFactory;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

public class VertxEventEmitter implements EventListener {

	private static Logger log;
	
	private final EventBus eb;

	public VertxEventEmitter(Verticle verticle) {
		this.eb = verticle.getVertx().eventBus();
		
		log = LoggerFactory.getLogger(VertxEventEmitter.class.getSimpleName());
		
	}
	

	
	/* Module Events */
	public void onModuleInstalled(Module m) {
		log.v("Sending Module Installed Notification "+m.getName());
		this.eb.publish(EVENT_MODULE_INSTALLED, m.toJson());
	}

	public void onModuleUninstalled(Module m) {
		log.v("Sending Module Uninstalled Notification "+ m.getName());
		this.eb.publish(EVENT_MODULE_UNINSTALLED, m.toJson());
	}

	public void onModuleModified(Module m) {
		log.v("Sending Module Modified Notification "+ m.getName());
		this.eb.publish(EVENT_MODULE_MODIFIED, m.toJson());
	}

	/* Deployment Events */
	public void onModuleDeployed(Deployment deployment) {
		log.v("Sending New Deployment Notificiation {"+deployment.getDeployedModule()+": "+ deployment.getDeploymentID()+"}");

		this.eb.publish(
				EVENT_MODULE_DEPLOYED,
				new JsonObject().putObject(Deployment.PARAM_MODULE, deployment.getDeployedModule().toJson()).putString("deployment_id",
						deployment.getDeploymentID()));
	}

	public void onModuleUndeployed(Deployment deployment) {
		log.v("Sending Undeployment Notificiation {"+deployment.getDeployedModule()+": "+deployment.getDeploymentID()+"}");

		this.eb.publish(
				EVENT_MODULE_UNDEPLOYED,
				new JsonObject().putObject(Deployment.PARAM_MODULE, deployment.getDeployedModule().toJson()).putString("deployment_id",
						deployment.getDeploymentID()));
	}
}
