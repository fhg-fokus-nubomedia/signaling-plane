package javax.ims.core.media;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface BasicReliableMedia extends Media
{	
    /**
     *  Returns the content type of this Media.
     *  
     * @return the content type or null if the content type has not been set
     */
    public String getContentType();

    /**
     * Returns an InputStream where incoming content can be read. 
     * It is only possible to invoke this method if the media direction is setup to receive content.
     * 
     * @return
     * @throws IllegalStateException - if the media's direction is DIRECTION_RECEIVE or DIRECTION_INACTIVE
     * @throws IllegalStateException - if the Media is not in STATE_ACTIVE 
     * @throws IOException - if an I/O error occurs
     */
    public InputStream getInputStream()throws IllegalStateException, IOException;

    /**
     * Returns an OutputStream where outgoing content can be written. 
     * It is only possible to invoke this method if the media direction is set up to send content.
     * 
     * @return
     * @throws IllegalStateException - if the media's direction is DIRECTION_RECEIVE or DIRECTION_INACTIVE
     * @throws IllegalStateException - if the Media is not in STATE_ACTIVE 
     * @throws IOException - if an I/O error occurs
     */
    public OutputStream getOutputStream()throws IllegalStateException, IOException;

    /**
     * Sets the content type of this Media.
     * 
     * @param contentType - the content type
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE
     * @throws IllegalArgumentException - if the syntax of the contentType argument is invalid
     */
    public void setContentType(String contentType) throws IllegalStateException, IllegalArgumentException;

   
    /**
     * Sets a listener for this BasicReliableMedia, replacing any previous BasicReliableMediaListener. 
     * A null reference is allowed and has the effect of removing any existing listener
     * 
     * @param listener - the listener to set, or null
     */
    public void setListener(BasicReliableMediaListener listener); 
}
