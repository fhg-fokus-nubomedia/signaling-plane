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


import com.google.gson.JsonObject;

import eu.nubomedia.af.kurento.util.log.Logger;
import eu.nubomedia.af.kurento.util.log.LoggerFactory;

/**
 * User session.
 * 
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 5.0.0
 */
public class CallSession {

	private static final Logger log = LoggerFactory.getLogger(CallSession.class.getSimpleName());

	private String name;

	private String sdpOffer, sdpAnswer, sessionId, callEvent;

	public CallSession(String name) {
		this.name = name;
	}

	public CallSession(String name, String sessionId) {
		this.name = name;
		this.sessionId = sessionId;
	}
	
	
	public String getName() {
		return name;
	}

	public String getSdpOffer() {
		return sdpOffer;
	}

	public void setSdpOffer(String sdpOffer) {
		this.sdpOffer = sdpOffer;
	}
	
	public void setSdpAnswer(String sdpAnswer){
		this.sdpAnswer = sdpAnswer;
	}
	
	public String getSdpAnswer(){
		return sdpAnswer;
	}

//	public String getCallingTo() {
//		return callingTo;
//	}
//
//	public void setCallingTo(String callingTo) {
//		this.callingTo = callingTo;
//	}
//
//	public String getCallingFrom() {
//		return callingFrom;
//	}
//
//	public void setCallingFrom(String callingFrom) {
//		this.callingFrom = callingFrom;
//	}

//	public void sendMessage(JsonObject message) throws IOException {
//		log.d("Sending message from user '"+name+"': "+message);
//		session.sendMessage(new TextMessage(message.toString()));
//	}

	public String getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(String sId){
		this.sessionId = sId;
	}
}
