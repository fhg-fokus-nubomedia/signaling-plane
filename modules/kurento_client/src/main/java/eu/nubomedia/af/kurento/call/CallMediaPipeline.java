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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.kurento.client.FaceOverlayFilter;
import org.kurento.client.MediaPipeline;
import org.kurento.client.RecorderEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.client.factory.KurentoClient;

/**
 * Media Pipeline (connection of Media Elements) for the advanced one to one
 * video communication.
 * 
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 5.0.0
 */
public class CallMediaPipeline {

	public static final String 
			RECORDING_PATH = "file:///tmp/",
			RECORDING_EXT = ".webm",
			HAT1_URI= "http://files.kurento.org/imgs/mario-wings.png",// "http://localhost:8080/mario-wings.png",
			HAT2_URI= "http://files.kurento.org/imgs/Hat.png"; //"http://localhost:8080/Hat.png";

	private MediaPipeline pipeline;
	private WebRtcEndpoint webRtcCaller, webRtcCallee;
	private RecorderEndpoint recorderCaller, recorderCallee;

	public CallMediaPipeline(KurentoClient kurento, String from, String to) {
		// Media pipeline
		pipeline = kurento.createMediaPipeline();

		// Media Elements (WebRtcEndpoint, RecorderEndpoint, FaceOverlayFilter)
		webRtcCaller = new WebRtcEndpoint.Builder(pipeline).build();
		webRtcCallee = new WebRtcEndpoint.Builder(pipeline).build();

		recorderCaller = new RecorderEndpoint.Builder(pipeline, RECORDING_PATH + from + RECORDING_EXT).build();
		recorderCallee = new RecorderEndpoint.Builder(pipeline, RECORDING_PATH + to + RECORDING_EXT).build();

		FaceOverlayFilter faceOverlayFilterCaller = new FaceOverlayFilter.Builder(pipeline).build();
		faceOverlayFilterCaller.setOverlayedImage(HAT1_URI, -0.35F, -1.2F, 1.6F, 1.6F);

		FaceOverlayFilter faceOverlayFilterCallee = new FaceOverlayFilter.Builder(pipeline).build();
		faceOverlayFilterCallee.setOverlayedImage(HAT2_URI, -0.2F, -1.35F, 1.5F, 1.5F);

		// Connections
		webRtcCaller.connect(faceOverlayFilterCaller);
		faceOverlayFilterCaller.connect(webRtcCallee);
		faceOverlayFilterCaller.connect(recorderCaller);

		webRtcCallee.connect(faceOverlayFilterCallee);
		faceOverlayFilterCallee.connect(webRtcCaller);
		faceOverlayFilterCallee.connect(recorderCallee);
	}

	public void record() {
		recorderCaller.record();
		recorderCallee.record();
	}

	public String generateSdpAnswerForCaller(String sdpOffer) {
		return webRtcCaller.processOffer(sdpOffer);
	}

	public String generateSdpAnswerForCallee(String sdpOffer) {
		return webRtcCallee.processOffer(sdpOffer);
	}

	public void release() {
		if (pipeline != null) {
			pipeline.release();
		}
	}
	
	public static void main(String[] args)  {
		
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(HAT1_URI));
		    try {
		        StringBuilder sb = new StringBuilder();
		        String line = br.readLine();

		        while (line != null) {
		            sb.append(line);
		            sb.append(System.lineSeparator());
		            line = br.readLine();
		        }
		        String everything = sb.toString();
		    } finally {
		        br.close();
		    }
		}catch(Exception e){
			e.printStackTrace();
		}
		
		 
	}
}
