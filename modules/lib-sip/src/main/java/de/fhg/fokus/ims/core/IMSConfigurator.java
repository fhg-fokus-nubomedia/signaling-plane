package de.fhg.fokus.ims.core;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import de.fhg.fokus.ims.core.net.NetworkService;

public class IMSConfigurator
{
	private IMSProfile imsProfile;

	public static String DEFAULT_PROXYTRANSPORT = "udp";

	public IMSConfigurator(IMSProfile imsProfile)
	{
		this.imsProfile = imsProfile;
	}

	public void configure(IMSManager imsManager, NetworkService networkService) throws Exception
	{
		String remoteIP = null;
		int remotePort = 0;
		String transport = DEFAULT_PROXYTRANSPORT;
		remoteIP = imsProfile.getPCSCFAddress();
		remotePort = Integer.parseInt(imsProfile.getPCSCFPort());		

		imsManager.setRemoteIP(remoteIP);
		imsManager.setRemotePort(remotePort);
		imsManager.setTransport(transport);

		int localPort = Integer.parseInt(imsProfile.getLocalPort());

		imsManager.setLocalPort(localPort);

		String localIP = imsProfile.getLocalInterface();

		InetAddress localAddress = null;
		if (localIP == null)
		{

			InetSocketAddress socketAddress = (InetSocketAddress) networkService.getLocalEndpoint(remoteIP, remotePort);

			if (socketAddress == null)
			{
				throw new Exception("Can't connect to remote host: " + remoteIP + " on port: " + remotePort + "! \nPlease check the configuration "
						+ "and make sure the configured P-CSCF is reachable via TCP on the configured port.");
			}

			localAddress = socketAddress.getAddress();
		} else
		{
			localAddress = InetAddress.getByName(localIP);
		}

		imsManager.setLocalAddress(localAddress);

		imsManager.setDomain(imsProfile.getDomain());
		imsManager.setPrivateUserId(imsProfile.getPrivateId());
		imsManager.setSessionExpireTime(imsProfile.isSessionTimerEnabled());
		imsManager.setSessionExpireTime(Integer.parseInt(imsProfile.getSessionExpireTimer()));
		imsManager.setPrackSupport(imsProfile.isPrackSupportEnabled());
		imsManager.setPublicUserId(new String[]
		{ imsProfile.getPublicId() });
		imsManager.setSecretKey(imsProfile.getSecretKey());
		if (Boolean.FALSE.equals(imsProfile.getRegister()))
			imsManager.setSendRegistration(false);
		else
			imsManager.setSendRegistration(true);
		if (Boolean.FALSE.equals(imsProfile.getEnableLog()))
			imsManager.setEnableSipLog(false);
		else
			imsManager.setEnableSipLog(true);
		
		imsManager.setDisplayName(imsProfile.getDisplayName());
		imsManager.setGRUU(imsProfile.getID());
		imsManager.setSubscribeToReg(imsProfile.getSubscribeToReg());
		String expireTime = imsProfile.getRegisterTime();

		if (expireTime != null)
		{
			try
			{
				int i = Integer.parseInt(expireTime);
				imsManager.setExpireTime(i);
			} catch (NumberFormatException e)
			{

			}
		}
	}
}
