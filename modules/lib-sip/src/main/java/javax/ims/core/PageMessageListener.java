package javax.ims.core;

/**
 * This listener type is used to notify the application about the status of sent page messages.
 * 
 * @see <code>PageMessage.setListener(PageMessageListener)</code>
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface PageMessageListener
{

    /**
     * Notifies the application that the PageMessage was successfully delivered.
     * 
     * @param pagemessage - the concerned PageMessage
     */
	public abstract void pageMessageDelivered(PageMessage pagemessage);

	/**
	 * Notifies the application that the PageMessage was not successfully delivered.
	 * 
	 * @param pagemessage - the concerned PageMessage
	 */
    public abstract void pageMessageDeliveredFailed(PageMessage pagemessage);
}
