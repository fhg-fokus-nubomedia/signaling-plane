package javax.ims.core.media;

import java.io.IOException;

import javax.ims.ImsException;

/**
 * The BasicUnreliableMedia represent a media connection with application content transported over UDP.
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface BasicUnreliableMedia extends Media
{	
	
    /**
     * Receives content from the remote endpoint.
     * 
     * @return
     * @throws IOException - if an I/O error occurs
     * @throws ImsException - if there is no content available for retrieval
     * @throws IllegalStateException - if the media's direction is DIRECTION_SEND or DIRECTION_INACTIVE 
     * @throws IllegalStateException - if the Media is not in STATE_ACTIVE
     */
	public abstract byte[] receive() throws IOException, IllegalArgumentException , IllegalStateException;

    /**
     * Sends content to the remote endpoint.
     * 
     * @param content - a byte array containing the content to be sent
     * @throws IOException - if an I/O error occurs
     * @throws IllegalStateException - - if the content argument is null
     * @throws IllegalStateException - if the media's direction is DIRECTION_SEND or DIRECTION_INACTIVE 
     * @throws IllegalStateException - if the Media is not in STATE_ACTIVE
     */
	public abstract void send(byte[] content)throws IOException,ImsException, IllegalStateException;

    /**
     * Sets the content type of this Media.
     * 
     * @param contentType - the content type
     * @throws IOException - if the Media is not in STATE_INACTIVE
     * @throws IllegalStateException - if the syntax of the contentType argument is invalid
     */
	public abstract void setContentType(String contentType) throws IOException, IllegalStateException;

    /**
     * Returns the content type of this Media.
     * 
     * @return the content type or null if the content type has not been set
     */
	public abstract String getContentType();
    
    /**
     * Sets a listener for this BasicUnreliableMedia, replacing any previous BasicUnreliableMediaListener. 
     * A null reference is allowed and has the effect of removing any existing listener.
     * 
     * @param listener - the listener to set, or null
     */
	public abstract void setListener(BasicUnreliableMediaListener listener);

}
