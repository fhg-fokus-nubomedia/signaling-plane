package org.openxsp.modules.repository;

import org.openxsp.modules.util.Logger;
import org.openxsp.modules.util.LoggerFactory;
import org.vertx.java.core.json.JsonObject;


public class Module {
	
	private static Logger log = LoggerFactory.getLogger(Module.class.getSimpleName()); 
	
	public static final String 
		
		ADDRESS_FIELD = "repository_address",
		CONFIG_FIELD = "module_config";
	
	private static final String NAME_FIELD = "module_name";
	
	private String name, repositoryAddress;
	private JsonObject config = new JsonObject("{}");

	/**
	 * @param name
	 *            - Name of the Module (including Version)
	 */
	public Module(String name, String repositoryAddress, JsonObject config) {
		setName(name);
		setRepositoryAddress(repositoryAddress);
		if(config!=null) setConfig(config);
	}

	/* Accessors */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRepositoryAddress() {
		return repositoryAddress;
	}

	public void setRepositoryAddress(String repositoryAddress) {
		this.repositoryAddress = repositoryAddress;
	}

	public JsonObject getConfig() {
		return config;
	}

	public void setConfig(JsonObject config) {
		this.config = config;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Module)) return false;
		
		Module m = (Module) obj;
		
		if(!this.name.equals(m.getName())) return false;
		
		if(!this.repositoryAddress.equals(m.getRepositoryAddress())) return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return (name+repositoryAddress).hashCode();
	}
	
	public JsonObject toJson(){
		JsonObject json = new JsonObject();
		
		json.putString(NAME_FIELD, getName())
			.putString(ADDRESS_FIELD, getRepositoryAddress());
		
		if(config!=null){
			json.putObject(CONFIG_FIELD, getConfig());
		}
		
		return json;
	}
	
	public static Module fromJson(JsonObject json){
		
		log.v("Parsing "+json);
		
		String name = json.getString(NAME_FIELD);
		String address = json.getString(ADDRESS_FIELD);
		JsonObject config = json.getObject(CONFIG_FIELD);
		
		return new Module(name, address, config);
	}
}
