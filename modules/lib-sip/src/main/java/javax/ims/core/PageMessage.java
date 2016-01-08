package javax.ims.core;

import javax.ims.ServiceClosedException;

/**
 * <p>The PageMessage interface is used for simple instant messages or exchange of small amounts of content outside of a session.
 * 
 * <p>The life cycle consist of three states, STATE_UNSENT, STATE_SENT and STATE_RECEIVED.
 * 
 * <p>A PageMessage created with the factory method createPageMessage in the CoreService interface will reside in STATE_UNSENT. 
 * When the message is sent with the send method the state will transit to STATE_SENT and further send requests cannot be made on 
 * the same PageMessage.
 * 
 * <P>An incoming PageMessage will always reside in STATE_RECEIVED. 
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface PageMessage
    extends ServiceMethod
{
	public static final int STATE_UNSENT = 1;
    public static final int STATE_SENT = 2;
    public static final int STATE_RECEIVED = 3;
    
    /**
     * Returns the content from this PageMessage. This method will return the content from the first body part if there are more that one.
     * 
     * @return - an array containing the content
     */
    public abstract byte[] getContent();

    /**
     * Returns the content MIME type of this PageMessage. This method will return the content MIME type from the first body part if there are more than one.
     * 
     * @return - the content MIME type
     */
    public abstract String getContentType();

    /**
     * <p>Sends the PageMessage.
     * <p>This method can be invoked send(null, null) if the application uses the Message interface to fill the content. 
     * In the case that the application uses both the Message interface and send(content, contentType), the content will be added as the last body part.
     * <p>The PageMessage will transit to STATE_SENT after calling this method. See example above. 
     * 
     * @param content - a byte array containing the content to be sent
     * @param contentType - the content MIME type
     * @throws ServiceClosedException if the Service is closed
     * @throws IllegalArgumentException  - if the syntax of the contentType argument is invalid or if one of the arguments is null
     * @throws IllegalStateException    - if the PageMessage is not in STATE_UNSENT   
     */
    public abstract void send(byte[] content, String contentType)
    throws ServiceClosedException, IllegalStateException, IllegalArgumentException ;
    
    /**
     * Sets a listener for this PageMessage, replacing any previous PageMessageListener. 
     * A null reference is allowed and has the effect of removing any existing listener.
     * 
     * @param pagemessagelistener - the listener to set, or null
     */
    public abstract void setListener(PageMessageListener pagemessagelistener);

    /**
     * Returns the current state of this PageMessage. 	 	
     * @return the current state of this PageMessage
     */
    public abstract int getState();

    
}
