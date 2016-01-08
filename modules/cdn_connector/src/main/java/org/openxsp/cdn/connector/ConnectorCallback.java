package org.openxsp.cdn.connector;

import org.vertx.java.core.json.JsonObject;

public interface ConnectorCallback {
	
	public enum ConnectorError{
		
		OperationNotSupported("Operation Not Supported"),
		BadConfiguration("Bad Configuration"),
		InternalError("Internal Error"),
		NotReachable("CDN Not Reachable"),
		Unauthorized("CDN Authorization Error"),
		CdnError("CDN Error"),
		NotFound("Resource Not Found");
		
		private String error, message;
		
		private ConnectorError(String error){
			this.error = error;
		}
		
		public String getError(){
			return error;
		}
		
		public ConnectorError setMessage(String msg){
			this.message = msg;
			return this;
		}
		
		public String getMessage(){
			return message;
		}
		
		public String toString(){
			if(message!=null) return error+": "+message;
			return this.error;
		}
	}
	
	void onSuccess(String connectorId, JsonObject result);
	
	void onError(ConnectorError e);

}
