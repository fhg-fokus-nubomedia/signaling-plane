package org.openxsp.cdn.connector.handler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.kurento.client.FaceOverlayFilter;
import org.kurento.client.MediaPipeline;
import org.kurento.client.RecorderEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.client.factory.KurentoClient;
import org.openxsp.cdn.connector.CdnConnector;
import org.openxsp.cdn.connector.ConnectorCallback;
import org.openxsp.cdn.connector.ConnectorException;
import org.openxsp.cdn.connector.ConnectorFactory;
import org.openxsp.cdn.connector.net.NetworkService;
import org.openxsp.cdn.connector.repo.CloudRepository;
import org.openxsp.cdn.connector.repo.DownloadCallback;
import org.openxsp.cdn.connector.util.log.Logger;
import org.openxsp.cdn.connector.util.log.LoggerFactory;
import org.vertx.java.core.json.JsonObject;



public class FileUploadSessionHandler implements SessionHandler{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4941532715044694936L;

	private static String 
			PARAM_KURENTO_ADDRESS = "kurento_address",
			RECORDING_EXT = ".mp4";
	
	private static Logger log = LoggerFactory.getLogger(FileUploadSessionHandler.class.getSimpleName());
	
	private NetworkService networkService;
	private CdnConnector connector;
	private JsonObject connectorConfig;
	
	private KurentoClient kurentoClient;
	
	private MediaPipeline pipeline;
	private WebRtcEndpoint webRtcCaller;
	private RecorderEndpoint recorder;
	private CloudRepository repository;
	
	private String fileName;
	
	public FileUploadSessionHandler(NetworkService net, JsonObject connectorConfig, CloudRepository repo) throws ConnectorException{
		
		log.v("Creating handler instance");
		
		String webSocketAddress = net.getConfig().getString(PARAM_KURENTO_ADDRESS);
		
		if(webSocketAddress==null){
			log.w("Cannot create kurento client - missing address in configuration");
			return;
		}

		this.kurentoClient = KurentoClient.create(webSocketAddress);
		
		if(kurentoClient==null){
			log.w("could not create Kurento client instance");
			return;
		}
		
		this.networkService = net;
		this.connector = ConnectorFactory.getConnector(connectorConfig);
		this.connectorConfig = connectorConfig;
		this.repository = repo;
		
	}
	
	public void onSessionCreate(String action, String sessionId, String sessionDescription, String sessionDescriptionType){
		
		log.d("handling session create with session id "+sessionId);
		
		if(repository==null){
			log.w("Cannot accept session - no repository to download the recorded file available");
			networkService.cancelSession("", sessionId, "Internal Error", "text/plain", FileUploadSessionHandler.this);
			return;
		}
		
		if(connector ==null){
			log.w("Could not create connector");
			networkService.cancelSession("", sessionId, "Could not create connector", "plain/text", null);
			return;
		}
		
		fileName = getTime()+"_session_"+sessionId;
		log.v("Session description: "+sessionDescription);
		
		pipeline = kurentoClient.createMediaPipeline();
		
		// Media Elements (WebRtcEndpoint, RecorderEndpoint, FaceOverlayFilter)
		webRtcCaller = new WebRtcEndpoint.Builder(pipeline).build();

		recorder = new RecorderEndpoint.Builder(pipeline, getPath(fileName)).stopOnEndOfStream().build();

		// Connections
		webRtcCaller.connect(recorder);
		//recorder.connect(webRtcCaller);
		webRtcCaller.connect(webRtcCaller);
		
		String kurentoSDP = webRtcCaller.processOffer(sessionDescription);
		if(kurentoSDP!=null){
			networkService.acceptSession("callResponse", sessionId, kurentoSDP, "application/sdp", this);
			
		}
		else{
			networkService.cancelSession("cancel", sessionId, null, null, this);
		}
		
	}
	
	
	public void onSessionAccepted(String action, String sessionId, String sessionDescription, String sessionDescriptionType){
		log.w("Session accepted?! - should not happen");
	}
	
	
	
	public void onSessionComfirmed(String action, String sessionId, String sessionDescription, String sessionDescriptionType){
		log.d("Session confirmed");
		
		recorder.record();
	}

	
	public void onSessionError(String action, String sessionId, String description){
		log.v("handle session error");
		
		tearDown();
	}
	
	
	public void onSessionCanceled(String action, String sessionId){
		log.v("handle session canceled");
		tearDown();
	}
	
	
	public void onSessionEnded(String action, final String sessionId){
		log.v("handle session ended");
		
		tearDown();
		
		//download file from repository
		//FIXME will probably fail as the media server will need some time to upload the file
		//TODO check if the media server can give a callback when the file has been uploaded
		repository.downloadFile(fileName, new DownloadCallback() {
			
			@Override
			public void onFileDownloaded(File file) {
				//upload video to CDN
				connector.uploadVideoAsFile(connectorConfig, file, new ConnectorCallback(){

					@Override
					public void onSuccess(String connectorId, JsonObject result) {
						log.i("File uploaded to CDN");
						networkService.confirmSession("uploaded", sessionId, result.toString(), "application/json", FileUploadSessionHandler.this);
						
					}

					@Override
					public void onError(ConnectorError e) {
						log.e(e.toString());
						networkService.cancelSession("", sessionId, e.toString(), "text/plain", FileUploadSessionHandler.this);
					}
					
				});
			}
			
			@Override
			public void onDownloadError(String fileName, String error) {
				log.w("Could not download file "+fileName+" from repository: "+error);
				networkService.cancelSession("", sessionId, error, "text/plain", FileUploadSessionHandler.this);
			}
		});
	}
	
	private void tearDown(){
		
		if(pipeline!=null) pipeline.release();
		
		if(recorder!=null){
			recorder.stop();
			recorder = null;
		}
		
		if(this.kurentoClient!=null) kurentoClient.destroy();
	}
	
	private void endSession(String sessionId){
		
		
		if(networkService==null){
			log.w("Network service not available");
			return;
		}
		
		if(sessionId==null) return; 
		
		log.i("Ending session");
		
		//TODO check if cancel or end
		networkService.endSession("stopCommunication", sessionId, this);
	}

	
	private String getPath(String videoId){
		String base = "file:///tmp/";
		return base+videoId+RECORDING_EXT;
	}
	
	private String getTime() {

		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy_hh:mm:ss:SSS");
		String formattedDate = formatter.format(new Date());

		return formattedDate;
	}
}
