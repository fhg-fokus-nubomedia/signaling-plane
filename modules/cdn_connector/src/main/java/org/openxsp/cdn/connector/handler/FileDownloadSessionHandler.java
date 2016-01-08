package org.openxsp.cdn.connector.handler;

import org.kurento.client.factory.KurentoClient;
import org.openxsp.cdn.connector.net.NetworkService;
import org.openxsp.cdn.connector.util.log.Logger;
import org.openxsp.cdn.connector.util.log.LoggerFactory;

public class FileDownloadSessionHandler implements SessionHandler{

private static Logger log = LoggerFactory.getLogger(FileDownloadSessionHandler.class);
	
	private NetworkService networkService;
	
	private KurentoClient kurentoClient;
	
	//private CallSession userSession;
	
	private String fileName, sessionId;
	
	private PlayMediaPipeline pipeline;
	
	public FileDownloadSessionHandler(String kurento, String fileName, NetworkService net){
		this.kurentoClient = KurentoClient.create(kurento);;
		this.networkService = net;
		this.fileName = fileName;
	}
	
	public void onSessionCreate(String action, String sessionId, String sessionDescription, String sessionDescriptionType){
		
		if(this.networkService==null){
			log.w("Network service is null");
			return;
		}
		log.d("Creating session");
		
		log.d("sending sdp offer to kurento");
		pipeline = new PlayMediaPipeline(kurentoClient, fileName, this);
		String sdpAnswer = pipeline.generateSdpAnswer(sessionDescription);

		log.d("Received SDP answer from kurento");
		
		networkService.confirmSession("playResponse", sessionId, sdpAnswer, "application/sdp", this);

		pipeline.play();
	}
	
	
	public void onSessionAccepted(String action, String sessionId, String sessionDescription, String sessionDescriptionType){
		log.w("Session accepted?! - should not happen");
	}
	
	
	
	public void onSessionComfirmed(String action, String sessionId, String sessionDescription, String sessionDescriptionType){
		log.d("Session confirmed");
	}

	
	public void onSessionError(String action, String sessionId, String description){
		log.v("handle session error");
		
		tearDown();
	}
	
	
	public void onSessionCanceled(String action, String sessionId){
		log.v("handle session canceled");
		tearDown();
	}
	
	
	public void onSessionEnded(String action, String sessionId){
		log.v("handle session ended");
		
		tearDown();
	}
	
	private void tearDown(){
		
		endSession();
		
		if(pipeline!=null){
			pipeline.stop();
			pipeline = null;
		}
	}
	
	private void endSession(){
		
		if(networkService==null){
			log.w("Network service not available");
			return;
		}
		
		//TODO check if cancel or end
		networkService.endSession("stopCommunication", sessionId, this);
	}

}
