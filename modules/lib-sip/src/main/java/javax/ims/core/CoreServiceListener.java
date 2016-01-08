package javax.ims.core;

/**
 * A listener type for receiving notifications on remotely initiated core service methods.
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @version 1.0
 *
 */
public interface CoreServiceListener
{
	
	/**
	 * Notifies the application when a page message is received from a remote
	 * end-point.
	 * 
	 * @param service - the concerned Service
	 * @param message - the received PageMessage
	 */
    public void pageMessageReceived(CoreService coreservice, PageMessage pagemessage);

    /**
	 * Notifies the application when a session invitation is received from a
	 * remote end-point.
	 * 
	 * @param service - the concerned Service
	 * @param session - the received session invitation
	 */
    public void sessionInvitationReceived(CoreService coreservice, Session session);
    
    /**
	 * Notifies the application when a reference request is received from a
	 * remote end-point.
	 * 
	 * @param service - the concerned Service
	 * @param reference - the received reference
	 */
    public void referRequestReceived(CoreService coreservice, Reference reference);
    
    /**
	 * Notifies the application when an unsolicited notify is received.
	 * 
	 * @param service - the concerned Service
	 * @param notify - the received notify
	 */
    public void unsolicitedNotifyReceived(CoreService coreservice, String s);

    /**
     * Notifies the application when a CoreService is closed.
     * 
     * @param service - the concerned Service
     */
    public void serviceClosed(CoreService service);
}
