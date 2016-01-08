package javax.ims.core;

/**
* A MessageBodyPart can contain different kinds of content, for example text, an image or an audio clip.
* There are two main uses for a MessageBodyPart:
* <li>A MessageBodyPart can be created and attached to the next outgoing request Message by calling createBodyPart on a Message.</li>
* <li>A MessageBodyPart can be extracted from a previously sent or received Message by calling getBodyParts on a Message.</li>
*
* See [RFC2045, 2046] for more information about body parts. [RFC2045] also describes some possible headers to set with the setHeader  method.
*   
*
* @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
* @version 1.0
*
*/
public interface MessageBodyPart
{
	/**
	 * Opens an InputStream to read the content of the MessageBodyPart.
	 * 
	 * @return an InputStream to read the content of the MessageBodyPart 
	 * @throws java.io.IOException - if the InputStream could not be opened
	 */
	public java.io.InputStream openContentInputStream()throws java.io.IOException;
	
	/**
	 * Opens an OutputStream to write the content of the MessageBodyPart. 
	 * If the OutputStream is not closed, it will be closed by the ServiceMethod that invoked a transmission of the Message.
	 * 
	 * @return an OutputStream to write the content of the MessageBodyPart
	 * @throws java.io.IOException - if the OutputStream could not be opened or if the OutputStream already has been opened
	 * @throws IllegalStateException  - if the Message is not in STATE_UNSENT
	 */
	public java.io.OutputStream openContentOutputStream() throws java.io.IOException, IllegalStateException ;
	
	/**
	 * Sets a header to the MessageBodyPart. If the header key already exists the value will be replaced. Only headers with the prefix "Content-" are allowed to set with this method. 
	 * 
	 * @param key - the header name
	 * @param value - the header value
	 * @throws IllegalArgumentException - if the key/value is null or invalid
	 * @throws IllegalStateException - if the Message is not in STATE_UNSENT
	 */
	public void setHeader(String key, String value) throws IllegalArgumentException, IllegalStateException  ;
	
	/**
	 * Returns the value of a header in this MessageBodyPart.
	 * Only headers with the prefix "Content-" are allowed to be retrieved with this method.
	 * 
	 * @param key - the header name
	 * @return a string containing the header value or null if the header does not exist
	 * @throws IllegalArgumentException  - if the key is null or invalid
	 */
	public String getHeader(String key) throws IllegalArgumentException ;
}
