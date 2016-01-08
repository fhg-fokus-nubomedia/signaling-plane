package javax.ims.core;

import javax.ims.ServiceClosedException;

/**
 * <p>The Publication is used for publishing event state to a remote endpoint. When a publication is created, an event package must 
 * be specified which identifies the type of content that is about to be published. A typical event package is 'presence' that can 
 * be used to publish online information. Other endpoints can then subscribe to that event package and get callbacks when the 
 * subscribed user identity changes online status.
 * 
 * <p>The published event state will be refreshed periodically until unpublish is called. Updates of the published event state may 
 * be achieved by calling the publish method again with modified event state.
 * 
 * <p>The life cycle consist of three states, STATE_INACTIVE, STATE_PENDING and STATE_ACTIVE.
 * <p>A new Publication starts in STATE_INACTIVE and when publish is called the state transits to STATE_PENDING and remains there until a response arrives from the remote endpoint.
 * <p>In STATE_ACTIVE, unpublish can be called to cancel the publication and the state will then transit to STATE_PENDING and remain there until a response arrives from the remote endpoint. 
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface Publication
    extends ServiceMethod
{
    public static final int STATE_INACTIVE = 1;
    public static final int STATE_PENDING = 2;
    public static final int STATE_ACTIVE = 3;
    
    /**
     * <p<Sends a publication request with an event state to the remote endpoint.
     * 
     * <p>This method can be invoked publish(null, null) if the application uses the Message interface to fill the event state. In the case that the application uses both the Message interface and publish(state, contentType), the event state will be added as the last body part.
     * <p>The Publication will transit to STATE_PENDING after calling this method. See example above. 
     * 
     * @param state - event state to publish
     * @param contentType - the content MIME type
     * @throws ServiceClosedException -if the Service is closed 
     * @throws IllegalStateException - if the Publication is in STATE_PENDING	
     */
    public void publish(byte[] state, String contentType)throws ServiceClosedException, IllegalStateException ;
    
    
    /**
     * <p>Terminates this publication.
     * <p>The Publication will transit to STATE_PENDING  after calling this method.
     * @throws ServiceClosedException - if the Service is closed
     * @throws IllegalStateException - if the Publication is not in STATE_ACTIVE
     */
    public void unpublish()throws ServiceClosedException , IllegalStateException;
    
    /**
     * Sets a listener for this Publication, replacing any previous PublicationListener. A null reference is allowed and has the effect of removing any existing listener.
     * @param  listener - the listener to set, or null
     */
    public void setListener(PublicationListener listener);
    
    /**
     * Returns the event package corresponding to this Publication.
     * @return - the event package
     */
    public String getEvent();
    
    /**
     * Returns the current state of the state machine of this Publication.
     * 
     * @return the current state
     */
    public int getState();
}
