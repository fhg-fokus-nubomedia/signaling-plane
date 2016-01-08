package org.openxsp.java.session;

import java.util.HashMap;

import org.vertx.java.core.json.JsonObject;

/**
 * Created by Frank Schulze on 22.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public class SessionControlMessageHeader {
    
    private HashMap<String, String> header;

    public SessionControlMessageHeader parse(JsonObject jsonMessage) {

    	if(jsonMessage!=null){
    		for(String headerName : jsonMessage.getFieldNames()){
            	addHeader(headerName, jsonMessage.getString(headerName));
            }
    	}
    	
        return this;
    }
    
    public void addHeader(String name, String value){
    	if(header==null) header = new HashMap<String, String>();
    	
    	header.put(name, value);
    }
    
    public void setAllHeader(HashMap<String, String> header){
    	this.header = header;
    }
    
    public String getHeader(String headerName){
    	if(this.header!=null) {
    		return this.header.get(headerName);
    	}
    	
    	return null;
    }
    
    public HashMap<String, String> getAllHeader(){
    	return this.header;
    }

    public JsonObject create() {
    	
        JsonObject headerJson = new JsonObject();

        if(this.header!=null){
        	for(String headerName : this.header.keySet())
        		headerJson.putString(headerName, this.header.get(headerName));
        }

        return headerJson;
    }


}
