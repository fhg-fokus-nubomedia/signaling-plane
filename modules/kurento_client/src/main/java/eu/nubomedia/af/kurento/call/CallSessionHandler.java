package eu.nubomedia.af.kurento.call;

import java.util.UUID;

import org.kurento.client.factory.KurentoClient;

import eu.nubomedia.af.kurento.SessionHandler;
import eu.nubomedia.af.kurento.net.NetworkService;
import eu.nubomedia.af.kurento.util.log.Logger;
import eu.nubomedia.af.kurento.util.log.LoggerFactory;

/*
 * Message flow:
 * 
 * Caller				Callee				This			 	   KurentoServer
 * 
 *   x---------------call(sdp)---------------> 
 *                    	   <----incoming()---x
 * 						   x----resp(sdp)---->
 * 											 x---ws(sdp_offer_callee)--->
 * 											 <--res(sdp_answer_callee)--x
 * 						   <----start(sdp)---x
 *                         <=================== Media ==================>
 * 						   					 x---ws(sdp_offer_caller)--->
 * 											 <--res(sdp_answer_caller)--x
 * 	<-------callResponse(sdp_answer)---------x
 *  <============================= Media ===============================>
 */

public class CallSessionHandler implements SessionHandler{
	
	private static Logger log = LoggerFactory.getLogger(CallSessionHandler.class.getSimpleName());

	private CallSession caller, callee;
	
	private NetworkService networkService;
	
	private KurentoClient kurentoClient;
	
	private CallMediaPipeline pipeline;
	
	
	public CallSessionHandler(KurentoClient kurentoClient, String from, String to, NetworkService net){
		
		this.caller = new CallSession(from);
		
		this.callee = new CallSession(to);
		
		this.networkService = net;
		
		this.kurentoClient = kurentoClient;
	}

	
	
	@Override
	public void onSessionCreate(String action, String sessionId, String sessionDescription, String sessionDescriptionType){
		
		log.v("Initiating caller and callee sessions");
		
		//TODO check if action = 'call'
		
		//read sdp
		caller.setSdpOffer(sessionDescription);
		caller.setSessionId(sessionId);
		
		//create session with callee
		log.v("creating session with callee "+callee.getName());
		String calleeSessionId = createSessionId();
		callee.setSessionId(calleeSessionId);
		networkService.createSession("incomingCall", calleeSessionId,  caller.getName(),callee.getName(), null, null, this);
	}
	
	
	@Override
	public void onSessionAccepted(String action, String sessionId, String sessionDescription, String sessionDescriptionType) {
		
		if(callee.getSessionId()!=null && callee.getSessionId().equals(sessionId)){
			//get media description and send it to the media server
			callee.setSdpOffer(sessionDescription);
			
			
			pipeline = new CallMediaPipeline(kurentoClient, caller.getName(), callee.getName());

			log.v("Asking kurento for SDP answer for callee");
			String calleeSdpAnswer = pipeline.generateSdpAnswerForCallee(callee.getSdpOffer());
			log.d("Got SDP answer from kurento server for callee");
			log.v(calleeSdpAnswer);
			callee.setSdpAnswer(calleeSdpAnswer);

//			JsonObject startCommunication = new JsonObject();
//			startCommunication.putString("id", "startCommunication");
//			startCommunication.putString("sdpAnswer", calleeSdpAnswer);
			//callee.sendMessage(startCommunication);
			networkService.confirmSession("startCommunication", callee.getSessionId(), calleeSdpAnswer, "application/sdp", this);

			log.v("Asking kurento for SDP answer for caller");
			String callerSdpAnswer = pipeline.generateSdpAnswerForCaller(caller.getSdpOffer());
			
			log.d("Got SDP from kurento for caller");
			log.v(callerSdpAnswer);
			
			caller.setSdpAnswer(callerSdpAnswer);
			
//			JsonObject response = new JsonObject();
//			response.putString("id", "callResponse");
//			response.putString("response", "accepted");
//			response.putString("sdpAnswer", callerSdpAnswer);
//			calleer.sendMessage(response);
			
			networkService.confirmSession("callResponse", caller.getSessionId(), callerSdpAnswer, "application/sdp", this);

			pipeline.record();
		}
		else{
			log.w("unexpected session accept for session with id "+sessionId);
		}
	}


	@Override
	public void onSessionComfirmed(String action, String sessionId, String sessionDescription, String sessionDescriptionType) {
		log.w("onSessionConfirmed - should not happen");
		
	}
	
	private String createSessionId(){
		return UUID.randomUUID().toString().substring(0,5);
	}
	

	@Override
	public void onSessionError(String action, String sessionId, String description) {
		log.v("handle session error");
		
		if(caller!=null && caller.getSessionId().equals(sessionId)){
			log.i("Session error of caller");
			caller = null;
			tearDown();
		}
		else if(callee!=null && callee.getSessionId().equals(sessionId)){
			log.i("Session error of callee");
			callee = null;
			tearDown();
		}
		else{
			log.w("Cannot handle canceled session - unknown session id "+sessionId);
		}
	}


	@Override
	public void onSessionCanceled(String action, String sessionId) {
		
		log.v("handle session cancel");
		
		if(caller!=null && caller.getSessionId().equals(sessionId)){
			log.i("Session of caller canceled by remote");
			caller = null;
			tearDown();
		}
		else if(callee!=null && callee.getSessionId().equals(sessionId)){
			log.i("Session of callee canceled by remote");
			callee = null;
			tearDown();
		}
		else{
			log.w("Cannot handle canceled session - unknown session id "+sessionId);
		}
	}


	@Override
	public void onSessionEnded(String action, String sessionId) {
		
		log.v("handler session end");
		
		if(caller!=null && caller.getSessionId().equals(sessionId)){
			log.i("Session of caller ended by remote");
			caller = null;
			tearDown();
		}
		else if(callee !=null && callee.getSessionId().equals(sessionId)){
			log.i("Session of callee ended by remote");
			callee = null;
			tearDown();
		}
		else{
			log.w("Cannot handle end session - unknown session id "+sessionId);
		}
		
	}
	
	private void tearDown(){
		
		endSession(caller);
		endSession(callee);
		
		if(pipeline!=null){
			pipeline.release();
		}
	}
	
	private void endSession(CallSession session){
		
		if(session == null) return; //session already ended
		
		if(networkService==null){
			log.w("Network service not available");
			return;
		}
		
		if(session.getSessionId()==null) return; 
		
		log.i("Ending session of "+session.getName());
		
		//TODO check if cancel or end
		networkService.endSession("stopCommunication", session.getSessionId(), this);
	}
}


	
