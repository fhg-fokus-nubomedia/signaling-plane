package javax.ims.core;

/**
 * A listener type for receiving notification on session events. 
 * When an event is generated for a Session the application is notified by having one of the methods called on the SessionListener.
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface SessionListener
{
	/**
	 * <p>Notifies the application that the session could not be established.
	 * <p>This callback is invoked at both involved endpoints.
	 * <p>The Session has transited to STATE_TERMINATED.
	 * 
	 * @param session - the concerned Session
	 */
	public void sessionStartFailed(Session session);
    
	/**
	 * <p>Notifies the application that the session has been established.
	 * <p>This callback is invoked at both involved endpoints.
	 * <p>The Session has transited to STATE_ESTABLISHED. 
	 * 
	 * @param session - the concerned Session
	 */
	public void sessionStarted(Session session);
	
	/**
	 * Notifies the application that the remote part's terminal is alerting the user of this session invitation.
	 * 
	 * @param session - the concerned Session
	 */
	public void sessionAlerting(Session session);
	
	/**
	 * <p>Notifies the application that the session update has been rejected.
	 * <p>This callback is invoked at both involved endpoints.
	 * <p>The Session has transited to STATE_ESTABLISHED. 
	 * 
	 * @param session - the concerned Session
	 */
	public void sessionUpdateFailed(Session session);
	
	/**
	 * <p>Notifies the application that the session has been updated.
	 * <p>This callback is invoked at both involved endpoints.
	 * <p>The Session has transited to STATE_ESTABLISHED. 
	 * 
	 * @param session - the concerned Session
	 */
	public void sessionUpdated(Session session);
	
	/**
	 * <p>Notifies the application that the remote endpoint requests an update of the sessions settings.
	 * <p>The Session has transited to STATE_RENEGOTIATING. 
	 * 
	 * @param session - the concerned Session
	 */
	public void sessionUpdateReceived(Session session);
	
	/**
	 * <p>Notifies the application that the session has been terminated or that the session no longer could stay established.
	 * <p>This callback can be invoked by the IMS engine if the access network is unavailable.
	 * <p>This method is invoked at both involved endpoints.
	 * <p>The Session has transited to STATE_TERMINATED. 
	 * 
	 * @param session - the concerned Session
	 */
	public void sessionTerminated(Session session);
	
	
	/**
	 * Notifies the application that a reference request has been received from a remote endpoint.
	 * Only references that are created in a session are notified in this method. 
	 * 
	 * @param session - the concerned Session
	 * @param reference - the Reference representing the request
	 */
	public void sessionReferenceReceived(Session session,
            Reference reference);
}
