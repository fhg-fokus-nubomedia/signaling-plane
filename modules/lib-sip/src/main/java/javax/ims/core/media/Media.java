package javax.ims.core.media;

/**
 * <p>This object is used to represent the media in an IMS session. A media can be seen as a pipe between the two endpoints where content can flow in either direction or both.
 * <p>When a calling terminal creates a session, a number of media objects can be added. When the session is started, the IMS engine creates a media offer based on properties of the included media, and sends it to the remote endpoint. If the session is accepted, the remote endpoint has a sufficient amount of information to allow it to create the corresponding media objects. In this way, both endpoints get the same view of the media transfer. A common and useful scenario for IMS applications is to stream video and audio to render in realtime. To support efficient implementations here, an application can pass the stream to a platform-supplied standard Player that supports an appropriate common codec to do the rendering.
 * <p>Media are subclassed based on the transport method used. 
 * 
 * <h3>Content Types</h3>
 * <p>Content types identifies the content type of a Media. They can be registered MIME types or some user defined type that follow the MIME syntax. See [RFC2045] and [RFC2046].
 * <p><b>Example content types:</b>
 * <ul>
 * <li>"text/plain"
 * <li> "image/png"
 * <li>"video/mpeg"
 * <li>"application/myChess"
 * </ul>
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface Media
{
    
    public static final int 	DIRECTION_INACTIVE  = 0;
    public static final int 	DIRECTION_RECEIVE 	= 1;
    public static final int 	DIRECTION_SEND 		= 2;
    public static final int 	DIRECTION_SEND_RECEIVE 	= 3;
    
    public static final int 	STATE_ACTIVE 	= 3;
    public static final int 	STATE_DELETED 	= 4;
    public static final int 	STATE_INACTIVE 	= 1;
    public static final int 	STATE_PENDING 	= 2;
    public static final int 	STATE_PROPOSAL 	= 5;
    
    public static final int 	UPDATE_MODIFIED = 2;
    public static final int 	UPDATE_REMOVED 	= 3;
    public static final int 	UPDATE_UNCHANGED = 1;
    
    /**
     * Returns the media descriptor(s) associated with this Media.
     * @return the media descriptor(s)
     */
    public MediaDescriptor[] getMediaDescriptors();
    
    /**
     * Returns the current direction of this Media.
     * @return the current direction of this Media
     */
    public int getDirection();
    
    /**
     * Sets the direction of this Media.
     * If a Media is changed in an established Session, the application is responsible to call update on the Session. 
     * 
     * @param direction - the direction of the Media
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE or STATE_ACTIVE
     * @throws IllegalArgumentException - if the direction argument is invalid
     */
    public void setDirection(int direction) throws IllegalStateException, IllegalArgumentException;
    
    /**
     * Returns the current state of this Media.
     * 
     * @return the current state
     */
    public int getState();
    
    /**
     * Returns the current update state of this Media.
     * 
     * @return the current update state
     * @throws IllegalStateException - if the Media is not in STATE_ACTIVE
     */
    public int getUpdateState() throws IllegalStateException;
    
    /**
     * Returns a fictitious media that is only meant to track changes that is about to be made to the media.
     * After the Session has been accepted or rejected this proposed media should be considered discarded.
     *  
     * @return a media proposal
     * @throws IllegalStateException - if the Media is not in STATE_ACTIVE 
     * @throws IllegalStateException - if the update state is not in UPDATE_MODIFIED
     */
    public Media getProposal() throws IllegalStateException;
}
