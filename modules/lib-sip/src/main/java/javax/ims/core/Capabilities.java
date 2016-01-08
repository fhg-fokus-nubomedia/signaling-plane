package javax.ims.core;

/**
 * The Capabilities is used to query a remote end-point about its capabilities
 *  
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @version 1.0
 *
 */
public interface Capabilities  extends ServiceMethod
{

	public static final int STATE_ACTIVE = 3;
	public static final int STATE_INACTIVE = 1;
	public static final int STATE_PENDING = 2;

	/**
	 * Returns all AppId that the remote endpoint has registered. An empty array will be returned if no AppId could be retrieved. 
	 * This method can only return AppIds that the local endpoint has installed
	 * 
	 * @return
	 */
	public String[] getRemoteAppIds();

	/**
	 * Returns an array of strings representing valid user identities for the remote endpoint.
	 * 
	 * @return an array of user identities, an empty array will be returned if no user identities could be retrieved
	 * @throws <code>IllegalStateException</code> - if the Capabilities is not in STATE_ACTIVE
	 */
	public String[] getRemoteUserIdentities();

	/**
	 * Returns the current state of this Capabilities.
	 * 
	 * @return
	 */
	int getState();

	/**
	 * <p>This method checks if the remote endpoint has the needed capabilities of the IMS application with appId argument.</p>
	 * 
	 * <p>Example: capabilities.hasCapabilities("myChess");</p>
	 * 
	 * @param    appId - the appId to check
	 * @return   true if the remote endpoint has the capabilities, otherwise false 
	 */
	public boolean hasCapabilities(String appId);

	
	/**
	 * Sends a capabilities request to a remote end-point.
	 * The Capabilities will transit to STATE_PENDING  after calling this method.
	 * 
	 * @throws <code>IllegalStateException</code> - if the Capabilities is in STATE_PENDING 
     * @throws <code>IllegalStateException</code> - if the Service is not in STATE_OPEN 
     * @throws <code>SecurityException</code> - if querying Capabilities is not permitted
	 */
	public void queryCapabilities();

	/**
	 * Sets a listener for this Capabilities, replacing any previous
	 * CapabilitiesListener.
	 * A null reference is allowed and has the effect of removing any existing listener.
	 * 
	 * @param listener - the listener to set, or null
	 */
	void setListener(CapabilitiesListener listener);
}
