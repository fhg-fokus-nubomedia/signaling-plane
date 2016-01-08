package org.openxsp.cdn.connector;

import org.openxsp.cdn.connector.util.log.Logger;
import org.openxsp.cdn.connector.util.log.LoggerFactory;
import org.openxsp.cdn.connector.youtube.YoutubeConnector;
import org.vertx.java.core.json.JsonObject;

public class ConnectorFactory {

	public static Logger log = LoggerFactory.getLogger(ConnectorFactory.class);
	
	public static CdnConnector getConnector(JsonObject config) throws ConnectorException{
		
		// TODO dispatch which kind of connector to return. For now use always
		// youtube connector since no other has been implemented so far
		
		log.w("Returning youtube connector");
		
		CdnConnector connector = new YoutubeConnector();
		
		return connector;
	}
}
