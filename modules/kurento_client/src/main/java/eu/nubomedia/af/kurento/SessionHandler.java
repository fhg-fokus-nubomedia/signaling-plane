package eu.nubomedia.af.kurento;


/**
 * 
 * @author fsc
 *
 */
public interface SessionHandler {
	
	/**
	 * Invoked when a session create request has been received. Handles session creation.
	 * @param action the action to be performed
	 * @param sessionId the ID of the session that has been accepted
	 * @param sessionDescription the remote parameters of the session, e.g. SDP or JSON description (might be null)
	 * @param sessionDescriptionType the content type of the session description (might be null)
	 */
	void onSessionCreate(String action, String sessionId, String sessionDescription, String sessionDescriptionType);
	
	
	/**
	 * Session has been accepted by remote
	 * @param action the action to be performed
	 * @param sessionId the ID of the session that has been accepted
	 * @param sessionDescription the remote parameters of the session, e.g. SDP or JSON description (might be null)
	 * @param sessionDescriptionType the content type of the session description (might be null)
	 */
	void onSessionAccepted(String action, String sessionId, String sessionDescription, String sessionDescriptionType);
	
	
	/**
	 * Session has been confirmed by remote. This means the session is established.
	 * @param action the action to be performed
	 * @param sessionId the ID of the session that has been accepted
	 * @param sessionDescription the remote parameters of the session, e.g. SDP or JSON description (might be null)
	 * @param sessionDescriptionType the content type of the session description (might be null)
	 */
	void onSessionComfirmed(String action, String sessionId, String sessionDescription, String sessionDescriptionType);

	/**
	 * An error occured during the session
	 * @param action the action to be performed
	 * @param sessionId the ID of the session 
	 * @param description
	 */
	void onSessionError(String action, String sessionId, String description);
	
	
	/**
	 * The session has been canceled by remote
	 * @param action the action to be performed
	 * @param sessionId the ID of the session that has been canceled
	 */
	void onSessionCanceled(String action, String sessionId);
	
	/**
	 * The session has been ended by remote
	 * @param action the action to be performed
	 * @param sessionId the ID of the session that has been ended
	 */
	void onSessionEnded(String action, String sessionId);
}
