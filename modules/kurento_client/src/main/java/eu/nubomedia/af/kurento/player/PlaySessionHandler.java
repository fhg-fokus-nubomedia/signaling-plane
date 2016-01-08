package eu.nubomedia.af.kurento.player;

import org.kurento.client.factory.KurentoClient;

import eu.nubomedia.af.kurento.SessionHandler;
import eu.nubomedia.af.kurento.call.CallSession;
import eu.nubomedia.af.kurento.net.NetworkService;
import eu.nubomedia.af.kurento.util.log.Logger;
import eu.nubomedia.af.kurento.util.log.LoggerFactory;




public class PlaySessionHandler implements SessionHandler{

	private static Logger log = LoggerFactory.getLogger(PlaySessionHandler.class.getSimpleName());
	
	private NetworkService networkService;
	
	private KurentoClient kurentoClient;
	
	private CallSession userSession;
	
	private PlayMediaPipeline pipeline;
	
	public PlaySessionHandler(KurentoClient kurento, String remoteUserVideo, NetworkService net){
		this.kurentoClient = kurento;
		this.userSession = new CallSession(remoteUserVideo);
		this.networkService = net;
	}
	
	public void onSessionCreate(String action, String sessionId, String sessionDescription, String sessionDescriptionType){
		
		if(this.networkService==null){
			log.w("Network service is null");
			return;
		}
		log.d("Creating session");
		
		userSession.setSessionId(sessionId);
		userSession.setSdpOffer(sessionDescription);
		
		log.d("sending sdp offer to kurento");
		pipeline = new PlayMediaPipeline(kurentoClient, userSession.getName(), this);
		String sdpAnswer = pipeline.generateSdpAnswer(userSession.getSdpOffer());

		log.d("Received SDP answer from kurento");
		
		userSession.setSdpAnswer(sdpAnswer);
		
//		JsonObject response = new JsonObject();
//		response.putString("id", "playResponse");
//		response.putString("response", "accepted");
//		response.putString("sdpAnswer", sdpAnswer);
//		session.sendMessage(new TextMessage(response.toString()));
		
		networkService.confirmSession("playResponse", userSession.getSessionId(), sdpAnswer, "application/sdp", this);

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
		
		if(userSession == null) return; //session already ended
		
		if(networkService==null){
			log.w("Network service not available");
			return;
		}
		
		if(userSession.getSessionId()==null) return; 
		
		log.i("Ending session of "+userSession.getName());
		
		//TODO check if cancel or end
		networkService.endSession("stopCommunication", userSession.getSessionId(), this);
		userSession=null;
	}
}
