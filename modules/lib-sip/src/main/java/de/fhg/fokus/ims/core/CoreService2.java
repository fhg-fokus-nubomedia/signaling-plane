package de.fhg.fokus.ims.core;

import java.net.InetAddress;

import javax.ims.core.CoreService;

public interface CoreService2 extends CoreService
{
	InetAddress getLocalEndpoint();
	
	String getServiceRoute();
}
