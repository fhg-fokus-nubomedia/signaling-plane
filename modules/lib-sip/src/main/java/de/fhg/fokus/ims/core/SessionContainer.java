package de.fhg.fokus.ims.core;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

public class SessionContainer extends Container
{	
	private static Logger LOGGER = LoggerFactory.getLogger(SessionContainer.class);

	public SessionImpl get(Request request)
	{
		String callId = ((CallIdHeader) request.getHeader("Call-ID")).getCallId();
		return (SessionImpl) getMethod(callId);
	}

	public SessionImpl get(Response response)
	{
		String callId = ((CallIdHeader) response.getHeader("Call-ID")).getCallId();
		return (SessionImpl) getMethod(callId);
	}

	public void dispatch(Request request)
	{
		String callId = ((CallIdHeader) request.getHeader("Call-ID")).getCallId();
		((SessionImpl) getMethod(callId)).notifyRequest(request);
	}

	public void dispatch(Response response, Request request)
	{
		String callId = ((CallIdHeader) request.getHeader("Call-ID")).getCallId();
		SessionImpl session = (SessionImpl) getMethod(callId);
		if (session != null)
			session.notifyResponse(response);
	}

	public void timeout(Request request)
	{
		String callId = ((CallIdHeader) request.getHeader("Call-ID")).getCallId();
		((SessionImpl) getMethod(callId)).methodDeliveryTimeout();
	}

	protected Object getKey(ServiceMethodImpl serviceMethod)
	{
		return serviceMethod.getCallId();
	}

	public void close()
	{
		SessionImpl[] sessions = (SessionImpl[]) getMethods().toArray(new SessionImpl[size()]);

		try
		{
			for (int i = 0; i < sessions.length; i++)
			{
				sessions[i].terminate(true);
			}
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}
}
