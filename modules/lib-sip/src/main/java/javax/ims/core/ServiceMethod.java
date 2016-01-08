package javax.ims.core;


/**
 * <p>The ServiceMethod interface provides methods to manipulate the next outgoing request message and to inspect previously sent request and response messages. 
 * The headers and body parts that are set will be transmitted in the next message that is triggered by an interface method, see code example below:
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @version 1.0
 */
public interface ServiceMethod
{
	
    /**
	 * Returns the user identity of the remote endpoint of this ServiceMethod. 
	 * This method will return null  if the ServiceMethod is a Subscription or a Publication
	 * 
	 * @return the remote user identity or null
	 */
    public abstract String getRemoteUserId();
    
    /**
	 * This method returns a handle to the next outgoing request Message within this ServiceMethod to be 
	 * manipulated by the local endpoint.
	 * 
	 * @return the next outgoing request Message created for this ServiceMethod
	 */
    public abstract Message getNextRequest();

    /**
     * This method enables the user to inspect a previously sent or received request message. It is only possible to inspect the last request of each interface method identifier. 
     * This method will return null  if the interface method identifier has not been sent/received.
     * 
     * @param method - the interface method identifier
     * @return the request Message
     * @throws IllegalArgumentException - if the method argument is not a valid identifier
     */
    public Message getPreviousRequest(int method) throws IllegalArgumentException;
    
    /**
     * This method enables the user to inspect previously sent or received response messages. It is only possible to inspect the response(s) for the last request of each interface method identifier. 
     * This method will return null if the interface method identifier has not been sent/received.
     * 
     * @param method - the interface method identifier
     * @return an array containing all responses associated to the method
     */
    public Message[] getPreviousResponses(int method);
}
