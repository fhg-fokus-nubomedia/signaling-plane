package javax.ims.core.media;

import java.io.IOException;

/**
 * <p>The FramedMedia represents a media connection on which content is delivered in packets. It can be used for instant messages, serialized data objects or files. The media is transported with the Message Session Relay Protocol (MSRP), according to [RFC4975]. There are two different ways to access the send and receive methods: with content as byte arrays or as files.
 *
 * <p>Large contents may be divided into multiple transactions. The different parts will be merged together in the remote terminal. It is possible to send and receive multiple messages simultaneous using the same FramedMedia.
 *
 * <p>FramedMedia uses the FileConnection API, [JSR75], to format the locator string in sendFile and receiveFile. The following code can be used to send a file on a file system, where CFCard is a valid existing file system root name for a given implementation:
 *
 * <code>framedMedia.sendFile("file:///CFCard/images/bob.png", "image/png", headers);</code>
 * <p>Headers that can be set in sendFile, sendBytes and retrieved in getHeader are defined in the BNF for 'Other-Mime-header' in [RFC4975].
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface FramedMedia extends Media
{

    /**
     * Sends the content to the remote endpoint over a reliable connection. 
     * This method is asynchronous and will return before the content is sent.
     * 
     * @param content - a byte array containing the content to be sent
     * @param contentType - the content MIME type
     * @param headers - an array of arrays, specifying key and value, null  can be used to indicate that no extra headers are desired
     * @return an identifier for the content sent
     * @throws IllegalStateException - if the media direction is DIRECTION_RECEIVE or DIRECTION_INACTIVE
     * @throws IllegalStateException - if the Media is not in STATE_ACTIVE
     * @throws IllegalArgumentException - if the content argument is null 
     * @throws IllegalArgumentException - if the syntax of the contentType or headers argument is invalid 
     * @throws IllegalArgumentException - if the contentType is not part of the accepted content types
     * @throws java.io.IOException - if an I/O error occurs
     */
	public String sendBytes(byte[] content, String contentType, String[][] headers)
        throws IllegalStateException, IllegalArgumentException, java.io.IOException;

    /**
     * Sends a file to the remote endpoint over a reliable connection. 
     * This method is asynchronous and will return before the content is sent.
     * 
     * @param locator - the location of the file to send
     * @param contentType - the content MIME type
     * @param headers - an array of arrays, specifying key and value, null  can be used to indicate that no extra headers are desired
     * @return an identifier for the content sent
     * @throws IOException - if an I/O error occurs when operating with the file or that the file could not be opened
     * @throws IllegalStateException - if the media direction is DIRECTION_RECEIVE or DIRECTION_INACTIVE
     * @throws IllegalStateException - if the Media is not in STATE_ACTIVE 
     * @throws IllegalArgumentException - if the syntax of the contentType or headers argument is invalid
     * @throws IllegalArgumentException - if the contentType is not part of the accepted content types
     */
	public String sendFile(String locator,String contentType,String[][] headers)
        throws IOException, IllegalStateException, IllegalArgumentException;

    /**
     * Returns the content type of the content that is identified by the messageId parameter.
     * 
     * @param messageId - an identifier for the content
     * @return the content type
     * @throws IllegalArgumentException - if the messageId is not found
     */
	public String getContentType(String messageId)
        throws IllegalArgumentException;

    /**
     * Sets the accepted content type(s) of this media.
     * If the accepted content type(s) has not been set, it will default to "*" and indicate that any content type may be transferred. 
     * 
     * @param acceptedContentTypes - an array of content types accepted in this media
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE
     * @throws IllegalArgumentException - if the syntax of the acceptedContentTypes  argument is invalid
     */
	public void setAcceptedContentTypes(String[] acceptedContentTypes)
        throws IllegalStateException, IllegalArgumentException;

    /**
     * Returns the accepted content type(s) of this media.
     * If the accepted content type(s) has not been set, it will default to "*" and indicate that any content type may be transferred. 
     * 
     * @return the accepted content type(s) of this media
     */
	public String[] getAcceptedContentTypes();

    /**
     * Returns the header value from the content that is identified by the messageId parameter.
     * 
     * @param messageId - an identifier for the content
     * @param key - the header name
     * @return a string containing the header value or null if the header does not exist
     * @throws IllegalArgumentException - if the messageId is not found 
     * @throws IllegalArgumentException - if the key is null
     */
	public String[] getHeader(String messageId, String key)
        throws IllegalArgumentException;
   
    /**
     * Receives content from the remote endpoint.
     * 
     * @param messageId - identifies which content to receive
     * @return a byte array containing the received content 
     * @throws IllegalArgumentException - if the messageId is not found or if there is no completely downloaded content corresponding to the messageId 
     * @throws java.io.IOException - if an I/O error occurs
     */
	public byte[] receiveBytes(String messageId)
        throws java.io.IOException, IllegalArgumentException;

    /**
     * Receives a file from the remote endpoint. 
     * This method will create a new file at the specified location. If the file already exists, it will be overwritten.
     * 
     * @param messageId - identifies which file to receive
     * @param locator - sets the location where the file will be stored
     * 
     * @throws IllegalArgumentException - if the messageId is not found or if there is no completely downloaded content corresponding to the messageId 
     * @throws java.io.IOException - if an I/O error occurs when operating with the file or that the file could not be opened
     * @throws SecurityException - if receiving a file is not permitted
     */
	public void receiveFile(String messageId, String locator)
        throws IOException, SecurityException, IllegalArgumentException;

    /**
     * Cancels the ongoing transfer. This method can be called for outgoing and incoming content. 
     * If no matching messageId is found or if the content is already transferred, this method will not do anything.
     * 
     * @param messageId - the identifier corresponding to the content to be cancelled
     */
	public void cancel(String messageId);

    /**
     * Sets a listener for this FramedMedia, replacing any previous FramedMediaListener. 
     * A null reference is allowed and has the effect of removing any existing listener.
     * @param listener - the listener to set, or null
     */
	public void setListener(FramedMediaListener listener);
}
