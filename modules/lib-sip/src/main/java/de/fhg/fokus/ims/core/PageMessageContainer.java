package de.fhg.fokus.ims.core;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class PageMessageContainer extends Container
{
	public PageMessageImpl get(String callId)
	{
		return (PageMessageImpl) getMethod(callId);
	}

	protected Object getKey(ServiceMethodImpl serviceMethod)
	{
		PageMessageImpl message = (PageMessageImpl) serviceMethod;
		return message.getCallId();
	}

	public void dispatch(Request request)
	{
		//No request 
	}

	public void dispatch(Response response, Request request)
	{
		PageMessageImpl message = get(((CallIdHeader) request.getHeader("Call-ID")).getCallId());

		if (message != null)
		{
			message.notifyResponse(response);
			remove(message);
		}
	}
	
	public void timeout(Request request)
	{
		PageMessageImpl message = get(((CallIdHeader) request.getHeader("Call-ID")).getCallId());

		if (message != null)
		{
			remove(message);
			message.methodDeliveryTimeout();
		}
	}
}
