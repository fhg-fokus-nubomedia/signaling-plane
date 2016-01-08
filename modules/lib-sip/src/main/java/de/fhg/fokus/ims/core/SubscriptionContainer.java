package de.fhg.fokus.ims.core;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import java.util.Collection;

import javax.sip.address.SipURI;
import javax.sip.header.CallIdHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import de.fhg.fokus.ims.core.utils.SIPUtils;

public class SubscriptionContainer extends Container
{	
	private static Logger LOGGER = LoggerFactory.getLogger(SubscriptionContainer.class);


//XXX	public SubscriptionImpl get(String toUserId, String event)
//	{
//		String key = event;
//		int i = key.indexOf(';');
//		if (i > -1)
//			key = key.substring(0, i);
//		return (SubscriptionImpl) getMethod(new SubscriptionKey(toUserId, key));
//	}
	
	public SubscriptionImpl get(String callID)
	{
		return (SubscriptionImpl) getMethod(new SubscriptionKey(callID));
	}
	
	
//XXX
//	public void removeSubscription(String toUserId, String event)
//	{
//		remove(new SubscriptionKey(toUserId, event));
//	}
	
	public void removeSubscription(String callID)
	{
		remove(new SubscriptionKey(callID));
	}

	public Collection getSubscriptions()
	{
		return getMethods();
	}

	protected Object getKey(ServiceMethodImpl serviceMethod)
	{
		SubscriptionImpl subs = (SubscriptionImpl) serviceMethod;

		String event = subs.getEvent();
		int i = event.indexOf(';');
		if (i > -1)
			event = event.substring(0, i);
//XXX
//		return new SubscriptionKey(subs.getRemoteUserId(), event);
		return new SubscriptionKey(subs.getCallId());
	}

	private class SubscriptionKey
	{
		private String value;
//XXX
//		public SubscriptionKey(String userId, String event)
//		{
//			value = userId + "#" + event;
//		}
		
		public SubscriptionKey(String callID)
		{
			if (callID==null){
				LOGGER.warn("Call ID is null");
				throw new NullPointerException();
			}
			value = callID;
		}

		public int hashCode()
		{
			return value.hashCode();
		}

		public boolean equals(Object obj)
		{
			if (obj == null || !(obj instanceof SubscriptionKey))
				return false;

			return ((SubscriptionKey) obj).value.equals(value);
		}
		
		public String toString()
		{
			return value;
		}
	}

	public void dispatch(Request request)
	{

	}

	public void dispatch(Response response, Request request)
	{
		String event = ((EventHeader) request.getHeader(EventHeader.NAME)).getEventType();
		String toUserId = null;

		ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
		toUserId = SIPUtils.getIdentity((SipURI) toHeader.getAddress().getURI());

		//XXX identify subscription by call-id
		SubscriptionImpl subscription = get(((CallIdHeader)request.getHeader("Call-ID")).getCallId());
		if (subscription == null)
		{
			LOGGER.info("did not find subscription");

		} else
		{
			subscription.notifyResponse(response);
		}
	}

	public void timeout(Request request)
	{
		String event = ((EventHeader) request.getHeader(EventHeader.NAME)).getEventType();
		ToHeader toHeader = (ToHeader) request.getHeader(ToHeader.NAME);
		String toUserId = SIPUtils.getIdentity((SipURI) toHeader.getAddress().getURI());
//XXX:	identify subscription by call-id
//		SubscriptionImpl subscription = get(toUserId, event);
		SubscriptionImpl subscription = get(((CallIdHeader)request.getHeader("Call-ID")).getCallId());
		if (subscription == null)
		{
			LOGGER.info("did not find subscription");

		} else
		{
			subscription.methodDeliveryTimeout();
		}
	}
}
