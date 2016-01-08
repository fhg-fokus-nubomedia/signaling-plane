package de.fhg.fokus.ims.core;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.SIPHeaderNames;

import java.util.Iterator;

import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Container to store all active exchanged cabability sets 
 * @author Alice Cheambe {alice.cheambe at fokus.fraunhofer.de}
 *
 */
public class CapabilityContainer extends Container 
{
	private static Logger LOGGER = LoggerFactory.getLogger(CapabilityContainer.class);
	
	public CapabilitiesImpl get(Request request)
	{
		String callId = ((CallIdHeader) request.getHeader("Call-ID")).getCallId();
		return (CapabilitiesImpl) getMethod(callId);
	}

	public CapabilitiesImpl get(Response response)
	{
		String callId = ((CallIdHeader) response.getHeader("Call-ID")).getCallId();
		return (CapabilitiesImpl) getMethod(callId);
	}
	
	public CapabilitiesImpl get(String sipId)
	{		
		for (Iterator iter = iterator(); iter.hasNext();)
		{
			CapabilitiesImpl capabilityImpl = (CapabilitiesImpl) iter.next();
			if (capabilityImpl.getRemoteUserId() == sipId)
			{
				return capabilityImpl;
			}
		}
		return null;
	}

	public CapabilitiesImpl[] getCapabilities()
	{
		return (CapabilitiesImpl[]) getMethods().toArray(new CapabilitiesImpl[size()]);
	}

	public void close()
	{
		CapabilitiesImpl[] temp = getCapabilities();

		if (temp == null || temp.length == 0)
			return;

//		for (int i = 0; i < temp.length; i++)
//		{
//			try
//			{
//				temp[i].unpublish(true);
//			} catch (ServiceClosedException e)
//			{
//				LOGGER.error(e.getMessage());
//			}
//		}
	}

	public void dispatch(Request request)
	{
		// Not applicable...
	}

	public void dispatch(Response response, Request request)
	{
		for (Iterator iterator = iterator(); iterator.hasNext();)
		{
			CapabilitiesImpl cap = (CapabilitiesImpl) iterator.next();

			if (cap.getCallId().equals(((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId()))
			{
				cap.notifyResponse(response);

				return;
			}
		}

		LOGGER.warn("No capabilities for response");
	}

	public void timeout(Request request)
	{
		for (Iterator iterator = iterator(); iterator.hasNext();)
		{
			CapabilitiesImpl cap = (CapabilitiesImpl) iterator.next();

			if (cap.getCallId().equals(((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId()))
			{
				cap.methodDeliveryTimeout();
				return;
			}
		}

		LOGGER.warn("No capabilities for response");
	}

	protected Object getKey(ServiceMethodImpl serviceMethod)
	{
//		String event = ((CapabilitiesImpl) serviceMethod).getEvent();
//		int i = event.indexOf(';');
//		if (i > -1)
//			event = event.substring(0, i);
//		return event;
		
		return null;
	}

}
