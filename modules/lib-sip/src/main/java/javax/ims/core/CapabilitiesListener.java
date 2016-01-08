package javax.ims.core;

/**
 * This listener type is used to notify the application about responses to capability queries.
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @version 1.0
 *
 */
public interface CapabilitiesListener
{

	/**
	 * Notifies the application that the capability queries to the remote
	 * end-point was successfully received.
	 * 
	 * @param capabilites - the concerned <code>Capabilities</code>
	 */
	public void capabilityQueryDelivered(Capabilities capabilites);

	/**
	 * Notifies the application that the capability queries to the remote
	 * end-point was not successfully received.
	 * 
	 * @param capabilites - the concerned <code>Capabilities</code>
	 */
	public void capabilityQueryDeliveryFailed(Capabilities capabilites);
}
