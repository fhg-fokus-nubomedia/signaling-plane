package de.fhg.fokus.ims.core;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.SIPHeaderNames;

import java.util.Iterator;

import javax.ims.ServiceClosedException;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

public class PublicationContainer extends Container
{
	private static Logger LOGGER = LoggerFactory.getLogger(PublicationContainer.class);
	
	public PublicationImpl get(String event)
	{
		return (PublicationImpl) getMethod(event);
	}

	public PublicationImpl[] getPublications()
	{
		return (PublicationImpl[]) getMethods().toArray(new PublicationImpl[size()]);
	}

	public void close()
	{
		PublicationImpl[] temp = getPublications();

		if (temp == null || temp.length == 0)
			return;

		for (int i = 0; i < temp.length; i++)
		{
			try
			{
				temp[i].unpublish(true);
			} catch (ServiceClosedException e)
			{
				LOGGER.error(e.getMessage());
			}
		}
	}

	public void dispatch(Request request)
	{
		// Not applicable...
	}

	public void dispatch(Response response, Request request)
	{
		for (Iterator iterator = iterator(); iterator.hasNext();)
		{
			PublicationImpl pub = (PublicationImpl) iterator.next();

			if (pub.getCallId().equals(((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId()))
			{
				pub.notifyResponse(response);

				return;
			}
		}

		LOGGER.warn("No publication for response");
	}

	public void timeout(Request request)
	{
		for (Iterator iterator = iterator(); iterator.hasNext();)
		{
			PublicationImpl pub = (PublicationImpl) iterator.next();

			if (pub.getCallId().equals(((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId()))
			{
				pub.methodDeliveryTimeout();
				return;
			}
		}

		LOGGER.warn("No publication for response");
	}

	protected Object getKey(ServiceMethodImpl serviceMethod)
	{
		String event = ((PublicationImpl) serviceMethod).getEvent();
		int i = event.indexOf(';');
		if (i > -1)
			event = event.substring(0, i);
		return event;
	}
}
