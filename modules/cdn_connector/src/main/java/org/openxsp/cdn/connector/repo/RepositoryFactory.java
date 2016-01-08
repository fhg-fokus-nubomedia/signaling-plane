package org.openxsp.cdn.connector.repo;

import org.vertx.java.core.json.JsonObject;

public class RepositoryFactory {
	
	public static CloudRepository getRepository(JsonObject repositoryConfig){
		
		if(repositoryConfig == null) return null;
		
		//TODO parse the config file and return the appropriate repository instance (if there are different repository implementations)
		
		return new MongoSyncCloudRepository(repositoryConfig);
		
	}

}
