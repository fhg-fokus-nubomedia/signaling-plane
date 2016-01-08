package javax.ims.core;

import javax.ims.ImsException;
 /**
  * The Message interface provides functionality to manipulate headers and body parts of outgoing request messages and to inspect previously transferred messages. 
  * A Message can be retrieved by calling ServiceMethod.getNextRequest, 
  * ServiceMethod.getPreviousRequest or ServiceMethod.getPreviousResponses
  * 
  * <h3>Restricted headers</h3>
  * <p>The access to these headers are restricted and an exception will be thrown if they are read or modified.
  * 
  * <h2>Read-only headers</h2>
  * <p>These headers can only be read, an ImsException should be thrown if the application tries to modify the following headers:
  * <p>Contact, Content-Length, Content-Type, From, P-Access-Network-Info, P-Asserted-Identity, P-Preferred-Identity, Referred-By, To
  * 
  * <h2>Inaccessible headers</h2>
  * 
  * <p>These headers can not be read or modified, an ImsException  should be thrown if the application tries to read or modify the following headers:
  * <p>Accept-Contact, Authentication-Info, Authorization, Call-ID, CSeq, Event, Max-Forwards, Min-Expires, Proxy-Authenticate, Proxy-Authorization, P-Associated-URI, RAck, Record-Route, Refer-To, Replaces, RSeq, Security-Client, Security-Server, Security-Verify, Service-Route, SIP-ETag, SIP-If-Match, Via, WWW-Authenticate 
  * 
  * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
  *
  */
public interface Message
{	   
    public static final int 	CAPABILITIES_QUERY 	= 1;
    public static final int 	PAGEMESSAGE_SEND 	= 2;
    public static final int 	PUBLICATION_PUBLISH = 3;
    public static final int 	PUBLICATION_UNPUBLISH =	4;
    public static final int 	REFERENCE_REFER = 5;
    public static final int 	SESSION_START 	= 6;
    public static final int 	SESSION_TERMINATE =	8;
    public static final int 	SESSION_UPDATE 	= 7;    
    public static final int 	SUBSCRIPTION_SUBSCRIBE = 9;
    public static final int 	SUBSCRIPTION_UNSUBSCRIBE = 10;
    public static final int 	INFOMESSAGE_SEND = 11;
    
    public static final int 	STATE_UNSENT = 1;
    public static final int 	STATE_SENT 	= 2;
    public static final int 	STATE_RECEIVED 	= 3;
    
    /**
     * Creates a new MessageBodyPart and adds it to the message.
     * 
     * @return a MessageBodyPart
     * @throws IllegalStateException - if the Message is not in STATE_UNSENT
     * @throws ImsException - if the body part could not be created
     */
    public MessageBodyPart createBodyPart() throws IllegalStateException, ImsException;
    
    /**
     * Returns all body parts that are added to the message. If the body part is a Session Description Protocol (SDP) it is not returned.
     * 
     * @return a copy of all MessageBodyParts in the message body, an empty array will be returned if there are no body parts in the Message
     */
    public MessageBodyPart[] getBodyParts();
    
    /**
     * Adds a header value, either on a new header or appending a new value to an already existing header.
     * The header(s) that can be added must be defined as a WriteHeader in the JAD file property or set with the setRegistry method in the Configuration class. 
     * 
     * @param key - the header name
     * @param value - the header value
     * @throws IllegalStateException - if the Message is not in STATE_UNSENT
     * @throws ImsException - if the key is not a defined header or a restricted header
     * @throws IllegalArgumentException  - if the key/value is null or invalid
     */
    public void addHeader(String key, String value) throws IllegalStateException, ImsException, IllegalArgumentException ;
    
    /**
     * Returns the value(s) of a header in this message. The header must be defined as a ReadHeader in the JAD file or set with the setRegistry method in the Configuration class
     * 
     * @param key - the header namehe header name
     * @return an array of all value(s) of the header, an empty array is returned if the header does not exist
     * @throws ImsException - if the key is not a defined header or a restricted header
     */
    public String[] getHeaders(String key)throws ImsException;
    
    /**
     * Returns the SIP method for this Message.
     * 
     * @return SIP method name, INVITE, UPDATE, BYE, etc. or null  if the method is not available
     */
    public String getMethod();
    
    /**
     * Returns the current state of this Message.
     * @return - the current state of this Message
     */
    public int getState();
    
    /**
     * Returns the status code of the response. This method can only be used for response messages as request messages do not have a status code.
     * See [RFC3261] (chapter 7.2 Responses) for more detailed information.
     * 
     * @return the status code, returns 0 if the status code is not available
     */
    public int getStatusCode();

    /**
     * Returns the reason phrase of the response. This method can only be used for response messages as request messages do not have a reason phrase.
     * 
     * @return -the reason phrase or null if the reason phrase is not available
     */
    public String getReasonPhrase();
    
}
