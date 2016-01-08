package de.fhg.fokus.ims.core;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import java.util.TimerTask;

import javax.ims.ImsException;
import javax.ims.ServiceClosedException;
import javax.ims.core.Message;
import javax.ims.core.Subscription;
import javax.ims.core.SubscriptionListener;
import javax.sip.header.CSeqHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Implementation of the Subscription Interface.
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @author Andreas Bachmann <andreas.bachmann@fraunhofer.fokus.de>
 */
public class SubscriptionImpl extends ServiceMethodImpl implements Subscription, Subscription2
{
	private static Logger LOGGER = LoggerFactory.getLogger(SubscriptionImpl.class);


	private static final String[] STATES = new String[]
	{ "UNKNOWN", "INACTIVE", "PENDING", "ACTIVE", "RECEIVED" };

	private String event;
	private SubscriptionListener listener;
	private int state;
	protected TimerTask timerTask;
	private Object syncroot = new Object();

	private boolean unsubscribing;

	private boolean incomingSubscription = false;

	private static final String expires = "3600";

	private static class SubscriptionRefreshTask extends TimerTask
	{
		private final SubscriptionImpl sub;

		public SubscriptionRefreshTask(SubscriptionImpl subscribption)
		{
			this.sub = subscribption;
		}

		public void run()
		{
			try
			{
				sub.subscribe();
			} catch (ServiceClosedException e)
			{
				LOGGER.info("refreshing subscription failed");
			}
		}
	}

	private static class SubscriptionTerminationTask extends TimerTask
	{
		private final SubscriptionImpl sub;

		public SubscriptionTerminationTask(SubscriptionImpl subscribption)
		{
			this.sub = subscribption;
		}

		public void run()
		{
			if (!sub.incomingSubscription)
				sub.terminate(null, null);
		}
	}

	/**
	 * Creates a new subscription.
	 * 
	 * @param coreService
	 * @param container
	 * @param localUri
	 * @param remoteUri
	 * @param eventPackage
	 * @throws ImsException
	 */
	public SubscriptionImpl(CoreServiceImpl coreService, SubscriptionContainer container, String localUri, String remoteUri, String eventPackage)
			throws ImsException
	{
		super(coreService, container, remoteUri, localUri);
		
		if (getCallId()==null){
			callId = IMSManager.getInstance().getSipProvider().getNewCallId().getCallId();
		}
		
		MessageImpl m = new MessageImpl(Message.SUBSCRIPTION_SUBSCRIBE, "SUBSCRIBE"); 
		setNextRequest(m);
		listener = null;
		state = Subscription.STATE_INACTIVE;
		this.event = eventPackage;
		container.put(this);
	}

	/**
	 * Creates a new subscription base on the incoming request
	 * 
	 * @param coreService
	 *            Instance of the core service the request was received by
	 * @param container
	 *            The container for the subscription
	 * @param request
	 *            The incoming SUBSCRIBE request
	 * @throws ImsException
	 *             in case of any errors
	 */
	public SubscriptionImpl(CoreServiceImpl coreService, SubscriptionContainer container, Request request, String localUri) throws ImsException
	{
		super(coreService, container, request, localUri);

		state = STATE_RECEIVED;

		MessageImpl message = new MessageImpl(Message2.SUBSCRIPTION_START, request);
		setPreviousRequest(message);

		this.event = ((EventHeader) request.getHeader("Event")).getEventType();
		int expires = request.getExpires().getExpires();

		if (expires > 0)
		{
			timerTask = new SubscriptionTerminationTask(this);
			getCoreService().getTimer().schedule(timerTask, expires * 1000);
		}

		container.put(this);

		MessageImpl response = (MessageImpl) getNextResponse();
		response.internalAddHeader("Expires", String.valueOf(expires));
		sendResponse(200, "OK", null, null);

		this.incomingSubscription = true;

		setNextRequest(new MessageImpl(Message2.SUBSCRIPTION_NOTIFY, Request.NOTIFY));

		setState(STATE_ACTIVE);
	}

	public String getEvent()
	{
		return event;
	}

	public int getState()
	{
		return state;
	}

	public void setState(int state)
	{
		if (this.state == state)
			return;

		this.state = state;

		if (state == STATE_INACTIVE)
		{
			getContainer().remove(this);
		}
	}

	public void setListener(SubscriptionListener listener)
	{
		if (listener == null)
			this.listener = null;
		else
			this.listener = listener;

	}

	public boolean isIncomingSubscription()
	{
		return incomingSubscription;
	}

	public void subscribe() throws IllegalStateException, ServiceClosedException
	{
		LOGGER.info("SubscriptionImpl.subscribe()");

		if (state == Subscription.STATE_PENDING)
			throw new IllegalStateException("subscribe() is not allowed when in STATE_PENDING!");

		MessageImpl message = (MessageImpl) getNextRequest();
		message.internalAddHeader("Expires", expires);
		message.internalAddHeader("Event", event);

		try
		{
			if (sendNextRequest())
				// Send out success
				setState(Subscription.STATE_PENDING);
			else
				// Send out failed
				setState(STATE_INACTIVE);

		} catch (Exception ex)
		{
			LOGGER.error(ex.getMessage(), ex);
		}
	}

	public void unsubscribe() throws ServiceClosedException
	{
		LOGGER.info("SubscriptionImpl.unsubscribe()");

		if (state != STATE_ACTIVE)
			throw new IllegalStateException("Expected state " + STATES[STATE_ACTIVE] + " but was " + STATES[state]);

		unsubscribing = true;
		MessageImpl message = new MessageImpl(Message.SUBSCRIPTION_UNSUBSCRIBE, "SUBSCRIBE");
		setNextRequest(message);

		message.internalAddHeader("Expires", "0");
		message.internalAddHeader("Event", event);

		if (sendNextRequest())
			// Send out success - wait for response
			setState(Subscription.STATE_PENDING);
		else
			// Send out failed - remove from container
			setState(STATE_INACTIVE);
	}

	public void unsubscribe(boolean waitForTermination)
	{
		LOGGER.info("SubscriptionImpl.unsubscribe()");

		if (state != STATE_ACTIVE)
			throw new IllegalStateException("Expected state " + STATES[STATE_ACTIVE] + " but was " + STATES[state]);

		unsubscribing = true;
		MessageImpl message = (MessageImpl) getNextRequest();
		message.internalAddHeader("Expires", "0");
		message.internalAddHeader("Event", event);

		synchronized (syncroot)
		{
			sendNextRequest();
			setState(Subscription.STATE_PENDING);

			if (waitForTermination)
			{
				try
				{
					syncroot.wait(5000);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized void notify(byte[] content, String contentType)
	{
		if (!incomingSubscription)
			throw new IllegalStateException("Not allowed on originated side");

		if (state != STATE_ACTIVE)
			throw new IllegalStateException("Expected state " + STATES[STATE_ACTIVE] + " but was " + STATES[state]);

		setState(STATE_PENDING);

		MessageImpl request = (MessageImpl) getNextRequest();
		request.internalAddHeader("Event", event);

		int expTime = (int) ((timerTask.scheduledExecutionTime() - System.currentTimeMillis()) / 1000);
		request.internalAddHeader("Subscription-State", "active;expires=" + expTime);

		if (content != null)
		{
			if (contentType == null)
				contentType = "application/octet-stream";

			request.createBodyPart(content, false, "Content-Type", contentType);
		}

		sendNextRequest();
	}

	public void terminate(byte[] content, String contentType)
	{
		if (!incomingSubscription)
			throw new IllegalStateException("Not allowed on originated side");

		MessageImpl request = (MessageImpl) getNextRequest();
		request.internalAddHeader("Event", event);
		request.internalAddHeader("Subscription-State", "terminated");

		sendNextRequest();
	}

	protected void requestReceived(MessageImpl requestMessage)
	{
		if (Request.NOTIFY.equals(requestMessage.getMethod()))
			processNotify(requestMessage);

		if (Request.SUBSCRIBE.equals(requestMessage.getMethod()))
			processSubscribe(requestMessage);
	}

	private void processSubscribe(MessageImpl requestMessage)
	{
		LOGGER.info("processSubscribeRequest(): procesing incoming SUBSCRIBE request");

		EventHeader temp = ((EventHeader) requestMessage.getRequest().getHeader("Event"));

		int expires = requestMessage.getRequest().getExpires().getExpires();

		timerTask.cancel();

		if (expires > 0)
		{
			timerTask = new SubscriptionTerminationTask(this);
			coreService.getTimer().schedule(timerTask, expires);
		}

		sendResponse(200, "OK", null, null);

		if (listener != null && listener instanceof SubscriptionListener2)
			((SubscriptionListener2) listener).subscriptionStarted(this);
	}

	private void processNotify(MessageImpl requestMessage)
	{
		LOGGER.info("processNotifyRequest(): procesing incoming Notify request");

		SubscriptionStateHeader subscriptionStateHeader = (SubscriptionStateHeader) requestMessage.getHeader("Subscription-State");

		sendResponse(Response.OK, "OK", null, null);

		String subscriptionState = subscriptionStateHeader.getState();
		int expires = subscriptionStateHeader.getExpires();
		if (subscriptionState.equalsIgnoreCase("terminated") || expires == 0)
		{
			LOGGER.info("subscription terminated");
			setState(Subscription.STATE_INACTIVE);

			try
			{
				referenceNotify(ReferenceInformationListener.STATE_REFERENCE_TERMINATED, 0, "200 OK");

				if (timerTask != null)
				{
					timerTask.cancel();
					timerTask = null;
				}
			} finally
			{
				synchronized (syncroot)
				{
					syncroot.notifyAll();
				}

				if (listener != null)
					listener.subscriptionTerminated(this);
			}
		} else
		{
			try
			{
				if (listener != null)
					listener.subscriptionNotify(this, requestMessage);
			} catch (Exception e)
			{
				LOGGER.error(e.getMessage(), e);
			} finally
			{
				int refreshSeconds = (expires * 1000) / 2;
				if (refreshSeconds > 0)
				{
					LOGGER.info("subscription active");
					if (timerTask != null)
						timerTask.cancel();
					timerTask = new SubscriptionRefreshTask(this);
					getCoreService().getTimer().schedule(timerTask, refreshSeconds);
				}
			}
		}
	}

	public void methodDelivered(MessageImpl responseMessage)
	{
		switch (responseMessage.getStatusCode())
		{
		case 200:
			if (((CSeqHeader) responseMessage.getResponse().getHeader("CSeq")).getMethod().equals(Request.NOTIFY))
			{
				if (listener != null && listener instanceof SubscriptionListener2)
					((SubscriptionListener2) listener).notifyDelivered(this);

				setNextRequest(new MessageImpl(Message2.SUBSCRIPTION_NOTIFY, Request.NOTIFY));
				setState(STATE_ACTIVE);
				return;
			}
		case 202:
			LOGGER.info("SubscriptionImpl.methodDelivered(): subscription to " + event + " successful.");

			int expires = -1;
			ExpiresHeader expiresHeader = (ExpiresHeader) responseMessage.getHeader(ExpiresHeader.NAME);

			if (expiresHeader != null)
				expires = expiresHeader.getExpires();

			if (expires > 0)
			{
				int refreshSeconds = Math.max((expires * 1000) / 2, (int) 300 * 1000);

				if (refreshSeconds > 0)
				{
					// refresh subscription after expires * 1000 milliseconds
					LOGGER.info("refreshing subscription after " + refreshSeconds + " seconds");
					if (timerTask != null)
						timerTask.cancel();
					timerTask = new SubscriptionRefreshTask(this);
					getCoreService().getTimer().schedule(timerTask, refreshSeconds);

					setState(Subscription.STATE_ACTIVE);

					referenceNotify(ReferenceInformationListener.STATE_REFERENCE_ACTIVE, expires, responseMessage.getStatusCode() + " "
							+ responseMessage.getReasonPhrase());

					if (listener != null)
						listener.subscriptionStarted(this);
				}
			}

			setNextRequest(new MessageImpl(Message.SUBSCRIPTION_SUBSCRIBE, "SUBSCRIBE"));
			break;
		default:
			LOGGER.info("SubscriptionImpl.methodDelivered(): subscription to " + event + " not successful - statuscode " + responseMessage.getStatusCode());
			setState(Subscription.STATE_INACTIVE);

			referenceNotify(ReferenceInformationListener.STATE_REFERENCE_TERMINATED, 0, responseMessage.getStatusCode() + " "
					+ responseMessage.getReasonPhrase());
			if (listener != null)
				listener.subscriptionStartFailed(this);

			break;
		}
	}

	public void methodDeliveryFailed(MessageImpl responseMessage)
	{
		LOGGER.info("SubscriptionImpl.methodDeliveryFailed():");

		if (unsubscribing)
		{
			LOGGER.info("unsuccesfull unsubscribe - terminating subscribtion");
			setState(Subscription.STATE_INACTIVE);

			if (timerTask != null)
			{
				timerTask.cancel();
				timerTask = null;
			}

			getContainer().remove(this);

			if (listener != null)
				listener.subscriptionTerminated(this);

			synchronized (syncroot)
			{
				syncroot.notifyAll();
			}
		} else
		{
			setState(Subscription.STATE_INACTIVE);

			referenceNotify(ReferenceInformationListener.STATE_REFERENCE_TERMINATED, 0, responseMessage.getStatusCode() + " "
					+ responseMessage.getReasonPhrase());

			if (listener != null)
				listener.subscriptionStartFailed(this);
		}
	}

	protected MessageImpl createRequestMessage(Request request)
	{
		if (Request.NOTIFY.equals(request.getMethod()))
			return new MessageImpl(Message2.SUBSCRIPTION_NOTIFY, request);

		return new MessageImpl(Message.SUBSCRIPTION_SUBSCRIBE, request);
	}

	protected MessageImpl createResponseMessage(Response response)
	{
		if (state == STATE_RECEIVED)
			return new MessageImpl(Message2.SUBSCRIPTION_START, response);
		if (((MessageImpl) getNextRequest()).getIdentifier() == Message.SUBSCRIPTION_SUBSCRIBE)
			return new MessageImpl(Message.SUBSCRIPTION_SUBSCRIBE, response);
		if (((MessageImpl) getNextRequest()).getIdentifier() == Message.SUBSCRIPTION_UNSUBSCRIBE)
			return new MessageImpl(Message.SUBSCRIPTION_UNSUBSCRIBE, response);

		return new MessageImpl(-1, response);
	}

	protected void methodDeliveryTimeout()
	{
		setState(Subscription.STATE_INACTIVE);

		if (listener != null)
			listener.subscriptionStartFailed(this);
	}
}
