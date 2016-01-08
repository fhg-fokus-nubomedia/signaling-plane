package javax.ims.core;

import javax.ims.ImsException;
import javax.ims.core.media.Media;


public interface Session extends ServiceMethod
{    
    public static final int STATE_INITIATED 	= 1;
    public static final int STATE_NEGOTIATING 	= 2;
    public static final int STATE_ESTABLISHING 	= 3;
    public static final int STATE_ESTABLISHED 	= 4;
    public static final int STATE_RENEGOTIATING = 5;
    public static final int STATE_REESTABLISHING =6; 
    public static final int STATE_TERMINATING 	= 7;
    public static final int STATE_TERMINATED 	= 8;
       
    
    /**
     * Sets a listener for this Session, replacing any previous SessionListener. 
     * A null reference is allowed and has the effect of removing any existing listener.
     * 
     * @param listener - the listener to set
     */
    public void setListener(SessionListener listener);

    /**
     * Synchronizes the session modifications with the remote endpoint that an application has done. 
     * Modifications include adding of media, removal of media, and change of existing media (e.g. directionality).
     * 
     * @throws IllegalStateException - if the hasPendingUpdate method returns false, meaning that there are no updates to be made to the session
     * @throws IllegalStateException - if the Session is not in STATE_ESTABLISHED
     * @throws ImsException - if a Media in the Session  is not initiated correctly
     */
    public void update() throws IllegalStateException, ImsException;
    
    /**
     * Starts a session. When this method is called the remote endpoint is invited to the session.
     * The Session will transit to STATE_NEGOTIATING after calling this method. 
     * 
     * @throws IllegalStateException  - if the Session is not in STATE_INITIATED
     * @throws ImsException - if a Media in the Session  is not initiated correctly
     */
    public void start() throws IllegalStateException, ImsException;

    /**
     * <p>Terminate or cancel a session. A session that has been started should always be terminated using this method.
     * <p>The Session will transit to STATE_TERMINATING.
     * <p>If the Session is STATE_TERMINATING or STATE_TERMINATED this method will not do anything. 
     */
    public void terminate();

    /**
     * Returns the current state of this Session.
     * @return the current state of this Session
     */
    public int getState();

    /**
     * Creates a Media object with a media type name and adds it to the Session. The following type names are recognized in this 
     * specification: BasicUnreliableMedia, BasicReliableMedia, FramedMedia and StreamMedia.
     * If a Media is added to an established Session, the application is responsible to call update on the Session. 
     * 
     * @param type - a Media type name
     * @param direction  - the direction of the Media flow
     * @return a new Media implementing the type name interface
     * @throws IllegalArgumentException - if the direction argument is invalid
     * @throws IllegalStateException - if the Session is not in STATE_ESTABLISHED, STATE_INITIATED
     * @throws ImsException - if the type could not be created
     */
    public Media createMedia(String type, int direction)  throws IllegalArgumentException, IllegalStateException , ImsException;
    
    /**
     * Removes a Media from the Session.
     * If a Media is removed from an established Session, the application is responsible to call update on the Session.
     * 
     * @param media - the Media to remove from the Session 
     * @throws IllegalArgumentException - if the Media does not exist in the Session or null 
     * @throws IllegalStateException - if the Session is not in STATE_ESTABLISHED, STATE_INITIATED
     */
    public void removeMedia(Media media)throws IllegalArgumentException, IllegalStateException;

    /**
     * Returns the Media that are part of this Session. An empty array will be returned if there are no Media in the Session.
     * 
     * @return an array of all Media in the Session 
     * @throws IllegalStateException - if the Session is in STATE_TERMINATED
     */
    public Media[] getMedia()throws IllegalStateException;

    /**
     * <p>This method can be used to accept a session invitation or a session update depending on the context:
     * <li>It can be used to accept a session invitation in STATE_NEGOTIATING if the remote endpoint has initiated the session. The Session will transit to STATE_ESTABLISHING.
     * <li>It can be used to accept a session update in STATE_RENEGOTIATING if the remote endpoint has initiated the session update. The Session will transit to STATE_REESTABLISHING.
     * 
     * @throws IllegalStateException - if the Session is not in STATE_NEGOTIATING or STATE_RENEGOTIATING
     */
    public void accept()throws IllegalStateException;

    /**
     * <p>This method can be used to reject a session invitation or a session update depending on the context:
     * <li>It can be used to reject a session invitation in STATE_NEGOTIATING if the remote endpoint has initiated the session. The Session will transit to STATE_TERMINATED.
     * <li>It can be used to reject a session update in STATE_RENEGOTIATING if the remote endpoint has initiated the session update. The Session will transit to STATE_ESTABLISHED and the update is discarded.
     * 
     * @throws IllegalStateException
     */
    public void reject()throws IllegalStateException;

    /**
     * Returns the session descriptor associated with this Session.
     * 
     * @return the session descriptor
     */
    public SessionDescriptor getSessionDescriptor();

    /**
     * This method checks if there are changes in this Session  that has not been negotiated.
     * 
     * @return true if there is a pending update, false otherwise
     */
    public boolean hasPendingUpdates();      

    
    /**
     * This method is used for referring the remote endpoint to a third party user or service.
     * 
     * @param referToUserId - the user identity to refer to
     * @param referMethod - the reference method to be used by the reference request, e.g. "INVITE", "BYE", see 3GPP TS 24.229, Stage 3, release 6
     * @return a new Reference 
     * @throws IllegalArgumentException - if the referToUserId argument is null or if the syntax is invalid 
     * @throws IllegalArgumentException - if the referMethod argument is null or if the syntax is invalid 
     * @throws IllegalStateException - if the Session is not in STATE_ESTABLISHED
     */
    public abstract Reference createReference(String referToUserId, String referMethod)
        throws IllegalArgumentException, IllegalStateException;
    
    /**
     * Creates a Capabilities with the remote endpoint.
     * 
     * @return a new Capabilities
     * @throws IllegalStateException
     */
    public Capabilities createCapabilities() throws IllegalStateException;
    
    /**
     * This method removes all updates that have been made to this Session and to 
     * medias that are part of this Session that have not been negotiated.
     * 
     * @throws IllegalStateException - if the Session is not in STATE_ESTABLISHED
     */
    public void restore() throws IllegalStateException;
    
}
