package de.fhg.fokus.ims.core;

import javax.ims.ImsException;
import javax.ims.ServiceClosedException;
import javax.ims.core.Message;
import javax.ims.core.MessageBodyPart;
import javax.ims.core.PageMessage;
import javax.ims.core.PageMessageListener;
import javax.sip.header.ContentTypeHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Implementation of the PageMessage Interface.
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @author Andreas Bachmann <andreas.bachmann@fraunhofer.fokus.de>
 * 
 */
public class PageMessageImpl extends ServiceMethodImpl implements PageMessage
{
	private static Logger LOGGER = LoggerFactory.getLogger(PageMessageImpl.class);


	private PageMessageListener listener;
	private byte[] content;
	private String contentType;
	private int state;
	private MessageImpl responseMessage;	

	public PageMessageImpl(CoreServiceImpl coreService, PageMessageContainer container, String remoteUri, String localUri) throws ImsException
	{
		super(coreService, container, remoteUri, localUri);
		listener = null;
		state = PageMessage.STATE_UNSENT;
		setNextRequest(new MessageImpl(Message.PAGEMESSAGE_SEND, "MESSAGE"));
		container.put(this);
	}

	public PageMessageImpl(CoreServiceImpl coreService, PageMessageContainer container, Request request, String localUri) throws ImsException
	{
		super(coreService, container, request, localUri);

		state = PageMessage.STATE_RECEIVED;
		listener = null;

		ContentTypeHeader cType = (ContentTypeHeader) request.getHeader(ContentTypeHeader.NAME);
		if (cType != null)
			contentType = cType.getContentType() + "/" + cType.getContentSubType();

		content = request.getRawContent();
	}

	public byte[] getContent()
	{
		return content;
	}

	public String getContentType()
	{
		return contentType;
	}

	public int getState()
	{
		return state;
	}
	
	public MessageImpl getResponseMessage() {
		return responseMessage;
	}

	public void send(byte[] content, String contentType) throws ServiceClosedException, IllegalStateException, IllegalArgumentException
	{
		if (state != PageMessage.STATE_UNSENT)
			throw new IllegalStateException("Method send() cannot be called. Current state (" + state + ") is not UNSENT!");

		if (content != null)
			this.content = content;

		if (contentType != null)
			this.contentType = contentType;

		Message message = getNextRequest();

		try
		{
			MessageBodyPart part = message.createBodyPart();
			part.setHeader("Content-Type", contentType);
			part.openContentOutputStream().write(content);
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage());
			return;
		}

		if (sendNextRequest())
		{
			state = PageMessage.STATE_SENT;
			getContainer().put(this);
		}
	}

	public void setListener(PageMessageListener pagemessagelistener)
	{
		if (pagemessagelistener == null)
			this.listener = null;
		else
			this.listener = pagemessagelistener;
	}

	protected MessageImpl createRequestMessage(Request request)
	{
		return new MessageImpl(Message.PAGEMESSAGE_SEND, request);
	}

	protected MessageImpl createResponseMessage(Response response)
	{
		return new MessageImpl(Message.PAGEMESSAGE_SEND, response);
	}

	// ------ Helper Methods ------ //
	public void methodDelivered(MessageImpl responseMessage)
	{
		LOGGER.info("PageMessageImpl: method delivered with {"+new Integer(responseMessage.getStatusCode())+"} {"+responseMessage.getMethod()+"}");
		this.responseMessage = responseMessage;
		// inform a waiting reference about responses
		try
		{
			referenceNotify(ReferenceInformationListener.STATE_REFERENCE_TERMINATED, 0, responseMessage.getStatusCode() + " "
					+ responseMessage.getReasonPhrase());
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		}

		if (listener != null)
			listener.pageMessageDelivered(this);
	}

	public void methodDeliveryFailed(MessageImpl responseMessage)
	{
		LOGGER.error("PageMessageImpl: method failed " + responseMessage.getMethod() + " - statuscode " + responseMessage.getStatusCode());

		// inform a waiting reference about responses
		try
		{
			referenceNotify(ReferenceInformationListener.STATE_REFERENCE_TERMINATED, 0, responseMessage.getStatusCode() + " "
					+ responseMessage.getReasonPhrase());
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		}

		if (listener != null)
			listener.pageMessageDeliveredFailed(this);
	}
}
