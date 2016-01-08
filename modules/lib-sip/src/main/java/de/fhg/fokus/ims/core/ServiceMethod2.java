package de.fhg.fokus.ims.core;

import javax.ims.core.Message;
import javax.ims.core.ServiceMethod;

public interface ServiceMethod2 extends ServiceMethod
{
	void setRemoteUserId(String value);
	
	Message getNextResponse();

	String getFromTag();

	String getToTag();
	
	boolean sendNextRequest(String recipientAddress, int recipientPort) ;
	
	void setReferenceInformationListener(ReferenceInformationListener listener);
}
