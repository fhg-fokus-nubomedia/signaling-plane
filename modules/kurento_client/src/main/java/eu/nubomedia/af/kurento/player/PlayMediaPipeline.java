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
package eu.nubomedia.af.kurento.player;

import static eu.nubomedia.af.kurento.call.CallMediaPipeline.RECORDING_EXT;
import static eu.nubomedia.af.kurento.call.CallMediaPipeline.RECORDING_PATH;

import org.kurento.client.EndOfStreamEvent;
import org.kurento.client.ErrorEvent;
import org.kurento.client.EventListener;
import org.kurento.client.MediaPipeline;
import org.kurento.client.PlayerEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.client.factory.KurentoClient;

import eu.nubomedia.af.kurento.SessionHandler;
import eu.nubomedia.af.kurento.util.log.Logger;
import eu.nubomedia.af.kurento.util.log.LoggerFactory;

/**
 * Media Pipeline (connection of Media Elements) for playing the recorded one to
 * one video communication.
 * 
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @since 5.0.0
 */
public class PlayMediaPipeline {

	private static final Logger log = LoggerFactory.getLogger(PlayMediaPipeline.class.getSimpleName());

	private WebRtcEndpoint webRtc;
	private PlayerEndpoint player;
	
	private SessionHandler handler;

	public PlayMediaPipeline(KurentoClient kurento, String user, SessionHandler sessionHandler) {

		this.handler = sessionHandler;
		
		// Media pipeline
		MediaPipeline pipeline = kurento.createMediaPipeline();

		// Media Elements (WebRtcEndpoint, PlayerEndpoint)
		webRtc = new WebRtcEndpoint.Builder(pipeline).build();
		player = new PlayerEndpoint.Builder(pipeline, RECORDING_PATH + user + RECORDING_EXT).build();

		// Connection
		player.connect(webRtc);

		// Player listeners
		player.addErrorListener(new EventListener<ErrorEvent>() {
			@Override
			public void onEvent(ErrorEvent event) {
				log.w("ErrorEvent: "+ event.getDescription());
				handler.onSessionError("", "", event.getDescription());
			}
		});
		player.addEndOfStreamListener(new EventListener<EndOfStreamEvent>() {
			@Override
			public void onEvent(EndOfStreamEvent event) {
				handler.onSessionEnded("", "");
			}
		});
	}

	public void play() {
		log.v("start playing ...");
		player.play();
	}

	public String generateSdpAnswer(String sdpOffer) {
		return webRtc.processOffer(sdpOffer);
	}

	public void stop(){
		if(player!=null){
			player.stop();
			player.release();
		}
		if(webRtc!=null) webRtc.release();
	}
}
