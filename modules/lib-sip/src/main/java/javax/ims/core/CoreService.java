package javax.ims.core;

import javax.ims.ImsException;
import javax.ims.Service;
import javax.ims.ServiceClosedException;

/**
 * <p>The CoreService gives the application the possibility to call remote peers over the IMS network. 
 * <p>There is a set of basic call types in the CoreService, created via factory methods. 
 * <p>The most useful service method is the session where media can be exchanged, see <code>ServiceMethod</code>.
 * <p>The application can react to incoming IMS calls by implementing the listener interface CoreServiceListener. 
 *	
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @version 1.0
 * @see CoreServiceListener
 *
 */
public interface CoreService extends Service
{

    
	/**
	 * Creates a Capabilities with fromUserId as sender, addressed to toUserId.
	 * 
	 * @param fromUserId - sender user identity, if null the sender user identity is assumed to be the local user identity of the Service
	 * @param toUserId - the remote user identity
	 * @return a new Capabilities
	 * @throws IllegalArgumentException - if any of the arguments are invalid 
     * @throws ServiceClosedException - if the Service is closed 
     * @throws ImsException - if the Capability could not be created
	 */
	public abstract Capabilities createCapabilities(String fromUserId, String toUserId) 
		throws ServiceClosedException,ImsException;

	/**
	 * Creates a PageMessage with fromUserId as sender, addressed to toUserId.
	 * 
	 * @param fromUserId - sender user identity, if null the user identity is assumed to be the local user identity of the Service
	 * @param toUserId - toUserId - the user identity to send a PageMessage to
	 * @return a new PageMessage 
	 * @throws IllegalArgumentException - if the toUserId argument is null
	 * @throws IllegalArgumentException - if the syntax of the fromUserId or toUserId argument is invalid 
     * @throws ServiceClosedException - if the Service is closed 
     * @throws ImsException - if the pagemessage could not be created
	 */
    public abstract PageMessage createPageMessage(String fromUserId, String toUserId) 
    	throws IllegalArgumentException, ServiceClosedException;

    /**
     * Creates a Publication for an event package with fromUserId as sender and toUserId as the user identity to publish event state on.
     * The event package must be defined as a JAD file property or set with the setRegistry method in the Configuration class. 
     * 
     * @param fromUserId - sender user identity, if null the user identity is assumed to be the local user identity of the Service
     * @param toUserId - the user identity to publish event state information on, if null the user identity is assumed to be the local user identity of the Service
     * @param event - the event package to publish event state information on
     * @return a new Publication - if the syntax of the fromUserId or toUserId argument is invalid
     * @throws IllegalArgumentException - if the event argument is not a defined event package
     * @throws ServiceClosedException - if the Service is closed
     * @throws ImsException - if the Publication could not be created
     */
    public abstract Publication createPublication(String fromUserId, String toUserId, String event) 
    	throws IllegalArgumentException, ServiceClosedException, ImsException  ;

    /**
	 * Creates a Reference with fromUserId as sender, addressed to toUserId and
	 * referToUserId as the user identity to refer to.
	 *
	 * @param fromUserId - sender user identity, if null the sender user identity is assumed to be the local user identity of the Service
	 * @param toUserId - the remote user identity
	 * @param referToUserId - the user identity to refer to
	 * @param referMethod - the reference method to be used by the reference request, for example "INVITE" 
	 * @return a new Reference
	 * 
	 * @throws IllegalArgumentException - if any of the arguments are invalid 
     * @throws ServiceClosedException - if the Service is closed 
     * @throws ImsException - if the reference could not be created
	 */
    public abstract Reference createReference(String fromUserId, String toUserId, String referToUserId, String referMethod)
        throws IllegalArgumentException, ServiceClosedException, ImsException ;

    /**
	 * Creates a Session with fromUserId as sender, addressed to toUserId.
	 *
	 * @param fromUserId - sender user identity, if null the sender user identity is assumed to be the local user identity of the Service
	 * @param toUserId - the remote user identity
	 * @return a new Session
	 * 
	 * @throws IllegalArgumentException - if any of the arguments are invalid 
     * @throws ServiceClosedException - if the Service is closed 
     * @throws ImsException - if the session could not be created
	 */
    public abstract Session createSession(String fromUserId, String toUserId)
        throws IllegalArgumentException, ServiceClosedException, ImsException;

    /**
	 * Creates a Subscription for an event package with fromUserId as sender and
	 * toUserId as the user identity to subscribe event state on.
	 *
	 * @param fromUserId - sender user identity, if null the sender user identity is assumed to be the local user identity of the <code>Service</code>
	 * @param toUserId - the remote user identity to subscribe state information on, or null
	 * @param event - the event package to subscribe state information on
	 * @return a new Subscription
	 * @throws IllegalArgumentException - if any of the arguments are invalid 
     * @throws IllegalStateException - if the Service is not in STATE_OPEN
	 */
    public abstract Subscription createSubscription(String fromUserId, String toUserId, String event)
        throws ServiceClosedException,ImsException;

    /**
	 * Sets a listener for this CoreService, replacing any previous
	 * CoreServiceListener.
	 * 
	 * @param listener
	 */
    public abstract void setListener(CoreServiceListener listener);

    /**
	 * Returns the local user identity stored in this CoreService.
	 * This method will return the default user identity if no user identity has been set with setLocalUserId. 
	 * 
	 * @return the local user identity 
     * @throws IllegalStateException - if the Manager is not in STATE_ONLINE
	 */
    public abstract String getLocalUserId()
        throws IllegalStateException;
    
    
}
