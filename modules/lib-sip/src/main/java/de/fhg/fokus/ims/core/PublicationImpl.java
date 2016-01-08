package de.fhg.fokus.ims.core;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.SIPHeaderNames;

import java.io.IOException;
import java.io.OutputStream;
import java.util.TimerTask;

import javax.ims.ImsException;
import javax.ims.ServiceClosedException;
import javax.ims.core.Message;
import javax.ims.core.MessageBodyPart;
import javax.ims.core.Publication;
import javax.ims.core.PublicationListener;
import javax.ims.core.Subscription;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.SIPETagHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Implementation of the Publication Interface.
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @author Andreas Bachmann <andreas.bachmann@fraunhofer.fokus.de>
 */
public class PublicationImpl extends ServiceMethodImpl implements Publication
{	
	private static Logger LOGGER = LoggerFactory.getLogger(PublicationImpl.class);

	private String event;
	private PublicationListener listener;
	private TimerTask timerTask;
	private byte[] presenceState;
	private String contentType;
	private int state;
	private String eTag;
	private Object syncRoot = new Object();
	private PublicationContainer container;

	class PublicationRefreshTask extends TimerTask
	{

		public PublicationRefreshTask()
		{
		}

		public void run()
		{
			try
			{
				publish(presenceState, contentType);
			} catch (ServiceClosedException e)
			{
				LOGGER.error("refreshing publication failed");
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	public PublicationImpl(CoreServiceImpl coreService, PublicationContainer container, String localUri, String remoteUri, String event) throws ImsException
	{
		super(coreService, container, remoteUri, localUri);

		listener = null;
		timerTask = null;
		presenceState = null;
		state = Publication.STATE_INACTIVE;
		this.event = event;
		this.container = container;
		setNextRequest(new MessageImpl(Message.PUBLICATION_PUBLISH, "PUBLISH"));

	}

	public String getETag()
	{
		return eTag;
	}

	public void setETag(String tag)
	{
		eTag = tag;
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
			container.remove(this);
	}

	public void setListener(PublicationListener listener)
	{
		if (listener == null)
			this.listener = null;
		else
			this.listener = listener;
	}

	public void publish(byte[] presenceState, String contentType) throws ServiceClosedException, IllegalStateException
	{
		if (state == Publication.STATE_PENDING)
			throw new IllegalStateException("publish() cannot be called in the PENDING state!");

		this.presenceState = presenceState;
		this.contentType = contentType;

		MessageImpl message = getPublishRequest();

		if (getETag() != null)
			message.internalAddHeader(SIPHeaderNames.SIP_IF_MATCH, getETag());

		if (sendNextRequest())
		{
			container.put(this);
			setState(STATE_PENDING);
		}
	}

	public void unpublish(boolean waitForAnswer) throws ServiceClosedException
	{
		unpublish();

		if (waitForAnswer)
		{
			synchronized (syncRoot)
			{
				try
				{
					syncRoot.wait(15000);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void unpublish() throws ServiceClosedException, IllegalStateException
	{
		if (state != Publication.STATE_ACTIVE)
			throw new IllegalStateException("Method unpublish() can be called only in the ACTIVE state! (Current state is " + state + ")");

		setNextRequest(new MessageImpl(Message.PUBLICATION_UNPUBLISH, Request.PUBLISH));

		MessageImpl message = (MessageImpl) getNextRequest();
		message.internalAddHeader("Expires", "0");
		message.internalAddHeader("Event", event);
		if (getETag() != null)
			message.internalAddHeader(SIPHeaderNames.SIP_IF_MATCH, getETag());

		setState(Subscription.STATE_PENDING);

		try
		{
			sendNextRequest();
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}

	public void methodDelivered(MessageImpl responseMessage)
	{
		try
		{
			referenceNotify(ReferenceInformationListener.STATE_REFERENCE_TERMINATED, 0, responseMessage + " " + responseMessage.getReasonPhrase());
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage());
		}

		setNextRequest(new MessageImpl(Message.PUBLICATION_PUBLISH, Request.PUBLISH));

		if (responseMessage.getStatusCode() == 200) // final sucess
		{
			LOGGER.info("PublicationImpl.methodDelivered(): publication to " + event + " successful.");
			// check for SIP-ETag Header
			if (responseMessage.getHeader("SIP-ETag") == null)
			{
				LOGGER.info("Getting 2xx PUBLISH " + "response without SIP-ETag header !");
				eTag = null;
			} else
				eTag = ((SIPETagHeader) responseMessage.getHeader("SIP-ETag")).getETag();
			LOGGER.info("etag value is " + eTag);

			int expires = 0;
			ExpiresHeader expiresHeader = (ExpiresHeader) responseMessage.getHeader(ExpiresHeader.NAME);
			if (expiresHeader != null)
			{
				expires = expiresHeader.getExpires();
			}

			LOGGER.info("Expires value is " + expires);
			if (expires > 0)
			{
				int refreshSeconds = (expires * 1000) - (expires/2 * 1000);

				if (refreshSeconds > 0)
				{
					// refresh publication after expires * 1000 milliseconds
					timerTask = new PublicationRefreshTask();
					getCoreService().getTimer().schedule(timerTask, refreshSeconds);
					setState(Publication.STATE_ACTIVE);

					LOGGER.info("publication state is active");
					if (listener != null)
						listener.publishDelivered(this);
				}
			} else
			{
				eTag = null;
				// prevent publication refreshing
				cancelTimer();
				setState(Publication.STATE_INACTIVE);
				LOGGER.info("publication state is inactive");
				if (listener != null)
					listener.publicationTerminated(this);
			}

			synchronized (syncRoot)
			{
				syncRoot.notifyAll();
			}
		}
	}

	public void methodDeliveryFailed(MessageImpl responseMessage)
	{
		int statusCode = responseMessage.getStatusCode();

		if (statusCode == 412)
		{
			this.state = STATE_INACTIVE;
			this.eTag = null;

			setNextRequest(new MessageImpl(Message.PUBLICATION_PUBLISH, "PUBLISH"));
			getPublishRequest();

			sendNextRequest();
		} else
		{
			setState(STATE_INACTIVE);
			eTag = null;

			cancelTimer();

			try
			{
				if (listener != null)
					listener.publicationTerminated(this);

				referenceNotify(ReferenceInformationListener.STATE_REFERENCE_TERMINATED, 0, responseMessage + " " + responseMessage.getReasonPhrase());
			} catch (Exception e)
			{
				LOGGER.error(e.getMessage());
			}
		}

		LOGGER.warn("delivery failed");
		synchronized (syncRoot)
		{
			syncRoot.notifyAll();
		}
	}

	protected void methodDeliveryTimeout()
	{
		setState(STATE_INACTIVE);
		eTag = null;
		cancelTimer();
		try
		{
			if (listener != null)
				listener.publicationTerminated(this);
			referenceNotify(ReferenceInformationListener.STATE_REFERENCE_TERMINATED, 0, 408 + " " + "Timeout");
		} finally
		{
			synchronized (syncRoot)
			{
				syncRoot.notifyAll();
			}
		}
	}

	/**
	 * This will never happen as clients do not process publish requests
	 */
	protected MessageImpl createRequestMessage(Request request)
	{
		return null;
	}

	protected MessageImpl createResponseMessage(Response response)
	{
		if (((MessageImpl) getNextRequest()).getIdentifier() == Message.PUBLICATION_PUBLISH)
			return new MessageImpl(Message.PUBLICATION_PUBLISH, response);
		else
			return new MessageImpl(Message.PUBLICATION_UNPUBLISH, response);
	}

	private MessageImpl getPublishRequest()
	{
		try
		{
			MessageImpl message = (MessageImpl) getNextRequest();
			message.internalAddHeader("Expires", "3600");
			message.internalAddHeader("Event", event);
			message.clearBodyParts();

			MessageBodyPart messagebodypart = message.createBodyPart();
			messagebodypart.setHeader("Content-Type", contentType);
			OutputStream os = messagebodypart.openContentOutputStream();
			os.write(presenceState);
			os.close();

			return message;
		} catch (ImsException e)
		{
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e)
		{
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

	private void cancelTimer()
	{
		if (timerTask != null)
		{
			LOGGER.info("terminating current PublicationTimerTask...");

			timerTask.cancel();
			timerTask = null;
		}
	}
}
