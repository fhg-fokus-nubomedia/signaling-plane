package javax.ims;

import java.util.Vector;



/**
 * <p>The ConnectionState class is used to monitoring the IMS connection and accessing user identities.
 * <p>The IMS engine runs autonomously on the device and therefore the application cannot assume that the device is connected 
 * or disconnected to the IMS network. If the IMS engine is not connected when the application creates a service with Connector.open the 
 * IMS engine will connect to the IMS network.
 * <P>While being connected to the IMS network, an application can retrieve the available network-provisioned user identities
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public class ConnectionState
{
	protected static ConnectionState instance;
	
	private boolean connected;
	private ConnectionStateListener listener;
	private Vector userIdentities;
	
	public ConnectionState()
	{
		userIdentities = new Vector();	
		instance = this;
		//TODO populate the user identities vector 
	}
	
	
	/**
	 * Returns a ConnectionState that monitors the connection to the IMS network.
	 * 
	 * @return a ConnectionState instance that monitors the connection to the IMS network
	 */
	public static ConnectionState getConnectionState()
	{
		if(instance == null)
			instance = new ConnectionState();
		return instance;
	}
	
	/**
	 * This method can be used to determine if the device is connected to the IMS network.
	 * 
	 * @return true if the device is connected to the IMS network, false otherwise
	 */
	public boolean isConnected()
	{
		return connected;
	}
	
	/**
	 * This method enables/disables the connection to the IMS network
	 */
	public void setConnectionStatus(boolean isConnected)
	{	
		this.connected = isConnected;
		if(listener != null)
		{
			if(connected)
				listener.imsConnected();
			else
				listener.imsDisconnected();
		}
			
	}
	
	public void setPublicIdentities(String[] publicIdenties)
	{
		userIdentities.clear();
		for (int i = 0; i < publicIdenties.length; i++)
		{
			userIdentities.add(publicIdenties[i]);
		}
	}
	
	/**
	 * Sets a listener for this ConnectionState, replacing any previous ConnectionStateListener. 
	 * A null  reference is allowed and has the effect of removing any existing listener.
	 * 
	 * @param listener - the listener to set, or null to remove it
	 */
	public void setListener(ConnectionStateListener listener)
	{
		if(listener == null)
			this.listener = null;
		else
			this.listener = listener;
	}
	
	public String[] getUserIdentities()
	{
		String users[] = new String[userIdentities.size()];
		userIdentities.copyInto(users);
		return users;
	}
	
	
}
