package de.fhg.fokus.ims.core.net;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class NetworkService
{
	private static StackLogger logger = CommonLogger.getLogger(NetworkService.class);

	private ArrayList<NetworkServiceListener> listeners = new ArrayList<NetworkServiceListener>(3);
	
	private SocketAddress localEndpoint;

	public void addNetworkServiceListener(NetworkServiceListener listener)
	{
		synchronized (listeners)
		{
			listeners.add(listener);
		}
	}
	
	public void removeNetworkServiceListener(NetworkServiceListener listener)
	{
		synchronized (listeners)
		{
			listeners.remove(listener);
		}		
	}
	
	public boolean isConnected()
	{
		return false;	
	}

	public void connect()
	{
		//TODO: create connection
	}
	
	public SocketAddress getLocalEndpoint()
	{
		return localEndpoint;	
	}
	
	public void setLocalEndpoint(SocketAddress localEndpoint)
	{
		if (this.localEndpoint == localEndpoint)
			return;
		
		this.localEndpoint = localEndpoint;
		
		fireLocalEnpointChanged();
	}
	

	public SocketAddress getLocalEndpoint(InetSocketAddress remoteEndpoint)
	{
		Socket socket = new Socket();
		try
		{
			socket.connect(remoteEndpoint);						
			SocketAddress sa = socket.getLocalSocketAddress();
			socket.close();
			
			return sa;
		} catch (IOException e)
		{
			logger.logError(e.getMessage(), e);
			return null;
		}
	}
	
	public SocketAddress getLocalEndpoint(String remoteHost, int remotePort)
	{
		InetSocketAddress socketAddress = new InetSocketAddress(remoteHost, remotePort);
		return getLocalEndpoint(socketAddress);
	}
	
	private void fireLocalEnpointChanged()
	{
		NetworkServiceListener[] listeners = getListenerArray();
		
		for (int i = 0; i < listeners.length; i++)
		{
			listeners[i].localEndpointChanged(this);
		}
	}
	
	private NetworkServiceListener[] getListenerArray()
	{
		synchronized (listeners)
		{
			return (NetworkServiceListener[]) listeners.toArray(new NetworkServiceListener[listeners.size()]);
		}
	}

	public static void main(String[] args) throws SocketException
	{
		Enumeration e = NetworkInterface.getNetworkInterfaces();
		
		while (e.hasMoreElements())
		{
			NetworkInterface elem = (NetworkInterface) e.nextElement();
			
			
			System.out.println(elem.getDisplayName());
	
			
			Enumeration e2 = elem.getInetAddresses();
			while (e2.hasMoreElements())
			{
				Object address = e2.nextElement();
				System.out.println(address);
			}
			
		}
	}
}
