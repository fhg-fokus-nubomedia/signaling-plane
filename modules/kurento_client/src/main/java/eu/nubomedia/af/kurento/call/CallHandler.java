/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package eu.nubomedia.af.kurento.call;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.kurento.client.factory.KurentoClient;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;


import eu.nubomedia.af.kurento.player.PlayMediaPipeline;
import eu.nubomedia.af.kurento.util.log.Logger;
import eu.nubomedia.af.kurento.util.log.LoggerFactory;


/**
 * Protocol handler for 1 to 1 video call communication.
 * 
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 5.0.0
 */
public class CallHandler {

	/*
	private static final Logger log = LoggerFactory.getLogger(CallHandler.class.getSimpleName());

	private ConcurrentHashMap<String, CallMediaPipeline> pipelines = new ConcurrentHashMap<String, CallMediaPipeline>();

	private KurentoClient kurento;

	private UserRegistry registry;

	
	
	public void handleMessage(JsonObject jsonMessage) throws Exception {
		CallSession user = registry.getBySession(session);

		if (user != null) {
			log.d("Incoming message from user '{}': {}", user.getName(), jsonMessage);
		} else {
			log.d("Incoming message from new user:\r\n"+jsonMessage);
		}

		switch (jsonMessage.getString("id")) {
			
			case "call":
				call(user, jsonMessage);
				break;
			case "incomingCallResponse":
				incomingCallResponse(user, jsonMessage);
				break;
			case "play":
				play(session, jsonMessage);
				break;
			case "stop":
				stop(session);
			default:
				break;
		}
	}


	private void call(CallSession caller, JsonObject jsonMessage) throws IOException {
		String to = jsonMessage.getString("to");
		String from = jsonMessage.getString("from");
		JsonObject response = new JsonObject();

		if (registry.exists(to)) {
			CallSession callee = registry.getByName(to);
			caller.setSdpOffer(jsonMessage.getString("sdpOffer"));
			caller.setCallingTo(to);

			response.putString("id", "incomingCall");
			response.putString("from", from);

			callee.sendMessage(response);
			callee.setCallingFrom(from);
		} else {
			response.putString("id", "callResponse");
			response.putString("response", "rejected: user '" + to + "' is not registered");

			caller.sendMessage(response);
		}
	}

	private void incomingCallResponse(CallSession callee, JsonObject jsonMessage) throws IOException {
		String callResponse = jsonMessage.getString("callResponse");
		String from = jsonMessage.getString("from");
		CallSession calleer = registry.getByName(from);
		String to = calleer.getCallingTo();

		if ("accept".equals(callResponse)) {
			log.d("Accepted call from '"+from+"' to '"+to+"'");

			CallMediaPipeline pipeline = new CallMediaPipeline(kurento, from, to);
			pipelines.put(calleer.getSessionId(), pipeline);
			pipelines.put(callee.getSessionId(), pipeline);

			String calleeSdpOffer = jsonMessage.get("sdpOffer").getAsString();
			String calleeSdpAnswer = pipeline.generateSdpAnswerForCallee(calleeSdpOffer);

			JsonObject startCommunication = new JsonObject();
			startCommunication.putString("id", "startCommunication");
			startCommunication.putString("sdpAnswer", calleeSdpAnswer);
			callee.sendMessage(startCommunication);

			String callerSdpOffer = registry.getByName(from).getSdpOffer();
			String callerSdpAnswer = pipeline.generateSdpAnswerForCaller(callerSdpOffer);

			JsonObject response = new JsonObject();
			response.putString("id", "callResponse");
			response.putString("response", "accepted");
			response.putString("sdpAnswer", callerSdpAnswer);
			calleer.sendMessage(response);

			pipeline.record();

		} else {
			JsonObject response = new JsonObject();
			response.putString("id", "callResponse");
			response.putString("response", "rejected");
			calleer.sendMessage(response);
		}
	}

	public void stop(WebSocketSession session) throws IOException {
		String sessionId = session.getId();
		if (pipelines.containsKey(sessionId)) {
			pipelines.get(sessionId).release();
			pipelines.remove(sessionId);

			// Both users can stop the communication. A 'stopCommunication'
			// message will be sent to the other peer.
			CallSession stopperUser = registry.getBySession(session);
			CallSession stoppedUser = (stopperUser.getCallingFrom() != null) ? registry.getByName(stopperUser.getCallingFrom())
					: registry.getByName(stopperUser.getCallingTo());

			JsonObject message = new JsonObject();
			message.putString("id", "stopCommunication");
			stoppedUser.sendMessage(message);
		}
	}

	private void play(WebSocketSession session, JsonObject jsonMessage) throws IOException {
		String user = jsonMessage.getString("user");
		log.d("Playing recorded call of user '"+user+"'");
 
		PlayMediaPipeline pipeline = new PlayMediaPipeline(kurento, user, session);
		String sdpOffer = jsonMessage.getString("sdpOffer");
		String sdpAnswer = pipeline.generateSdpAnswer(sdpOffer);

		JsonObject response = new JsonObject();
		response.putString("id", "playResponse");
		response.putString("response", "accepted");
		response.putString("sdpAnswer", sdpAnswer);
		session.sendMessage(new TextMessage(response.toString()));

		pipeline.play();

	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		registry.removeBySession(session);
	}

*/
}
