package javax.ims;

/**
 * A listener type for receiving notifications about changes to the IMS connection.
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface ConnectionStateListener
{
	/**
	 * Notifies the application when the device is connected to the IMS network.
	 * 
	 */
    public abstract void imsConnected();
    
    /**
     * Notifies the application when the device is disconnected from the IMS network.
     */
    public abstract void imsDisconnected();
}
