package de.fhg.fokus.ims.core;

import gov.nist.javax.sip.address.AddressFactoryImpl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;

import javax.ims.ImsException;
import javax.ims.ServiceClosedException;
import javax.ims.core.Message;
import javax.ims.core.Reference;
import javax.ims.core.ReferenceListener;
import javax.ims.core.ServiceMethod;
import javax.sip.Dialog;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.ReferToHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import de.fhg.fokus.ims.core.utils.SIPUtils;

/**
 * Implementation of the Reference Interface.
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @author Andreas Bachmann <andreas.bachmann@fraunhofer.fokus.de>
 */
public class ReferenceImpl extends ServiceMethodImpl implements Reference, ReferenceInformationListener
{
	/**
	 * Logger
	 */	
	private static Logger LOGGER = LoggerFactory.getLogger(ReferenceImpl.class);

	/**
	 * State names for simple logging
	 */
	private static final String[] states = new String[]
	{ "UNKNOWN", "INITIATED", "PROCEEDING", "REFERRING", "TERMINATED" };

	/**
	 * Reference to the listener
	 */
	private ReferenceListener listener;

	/**
	 * Holds the refer Method
	 */
	private String referMethod;

	/**
	 * Hold the refer to uri
	 */
	private Address referTo;

	/**
	 * Holds the refer To
	 */
	private int state;

	/**
	 * Creates a new outgoing reference for the given remote uri.
	 * 
	 * The request for this reference will create a new dialog.
	 * 
	 * @param coreService
	 * @param remoteUri
	 * @param method
	 * @param referTo
	 * @throws ImsException
	 */
	public ReferenceImpl(CoreServiceImpl coreService, ReferenceContainer container, String remoteUri, String method, String referTo, String localUri) throws ImsException
	{
		super(coreService, container, remoteUri, localUri);
		this.state = STATE_INITIATED;
		this.referMethod = method;
		try
		{
			this.referTo = coreService.getManager().getAddressFactory().createAddress(referTo);
		} catch (ParseException e)
		{
			throw new ImsException("Can't create uri! " + e.getMessage());
		}

		setNextRequest(new MessageImpl(Message.REFERENCE_REFER, "REFER"));
	}

	/**
	 * Creates a new outgoing reference based on the given dialog.
	 * 
	 * The request for this reference will not create a new dialog but will be
	 * sent along the given dialog.
	 * 
	 * @param coreService
	 *            Instance of the core thi
	 * @param dialog
	 * @param remoteUri
	 * @param method
	 * @param referTo
	 * @throws ImsException
	 */
	public ReferenceImpl(CoreServiceImpl coreService, ReferenceContainer container, Dialog dialog, String remoteUri, String method, String referTo, String localUri)
			throws ImsException
	{
		super(coreService, container, remoteUri, localUri);
		this.setDialog(dialog);
		this.state = STATE_INITIATED;
		this.referMethod = method;

		try
		{
			this.referTo = coreService.getManager().getAddressFactory().createAddress(referTo);
			if ((this.referTo.getURI() instanceof SipURI) && method != null)
				((SipURI) this.referTo.getURI()).setParameter("method", method);
		} catch (ParseException e)
		{
			throw new ImsException("Can't create uri! " + e.getMessage());
		}

		setNextRequest(new MessageImpl(Message.REFERENCE_REFER, "REFER"));
	}
	
	/**
	 * Creates a new incoming reference, based on the given request
	 * 
	 * @param coreService
	 * @param container
	 * @param request
	 * @throws ImsException
	 */
	public ReferenceImpl(CoreServiceImpl coreService, ReferenceContainer container, Request request, String localUri) throws ImsException
	{
		super(coreService, container, request, localUri);

		MessageImpl message = new MessageImpl(Message.REFERENCE_REFER, request);
		setPreviousRequest(message);

		ReferToHeader header = (ReferToHeader) request.getHeader(ReferToHeader.NAME);
		if (header.getAddress().getURI().isSipURI())
		{
			SipURI uri = (SipURI) header.getAddress().getURI();
			if (uri.getParameter("method") != null)
			{
				referMethod = uri.getParameter("method");
				uri.removeParameter("method");
				referTo = coreService.getManager().getAddressFactory().createAddress(uri);
			}
		} else
		{
			referMethod = header.getParameter("method");
			if (referMethod == null)
				referMethod = "INVITE"; // default method if no method is set
			referTo = (Address) header.getAddress().clone();
		}
		state = STATE_PROCEEDING;
	}

	public void reject() throws ServiceClosedException, IllegalStateException
	{
		if (state != STATE_PROCEEDING)
			throw new IllegalStateException("Method reject() can be called only in the PROCEEDING state! (Current state is " + states[state] + ")");
		sendResponse(406, "Not acceptable", null, null);
	}

	public void accept() throws ServiceClosedException, IllegalStateException
	{
		if (state != STATE_PROCEEDING)
			throw new IllegalStateException("Method accept() can be called only in the PROCEEDING state! (Current state is " + states[state] + ")");

		sendResponse(202, "Accepted", null, null);
		setState(STATE_REFERRING);
	}

	public void connectReferMethod(ServiceMethod aserviceMethod) throws IllegalStateException, IllegalArgumentException
	{
		ServiceMethod2 serviceMethod = (ServiceMethod2) aserviceMethod;

		MessageImpl message = (MessageImpl) getPreviousRequest(Message.REFERENCE_REFER);

		ReferToHeader header = (ReferToHeader) message.getHeader("Refer-To");

		String replaces = ((SipURI) header.getAddress().getURI()).getHeader("Replaces");

		if (replaces != null)
		{
			try
			{
				replaces = URLDecoder.decode(replaces, "utf-8");
				String fromTag = URLDecoder.decode(((SipURI) header.getAddress().getURI()).getHeader("from-tag"), "utf-8");
				String toTag = URLDecoder.decode(((SipURI) header.getAddress().getURI()).getHeader("to-tag"), "utf-8");
				((MessageImpl) serviceMethod.getNextRequest()).internalAddHeader("Replaces", replaces + ";from-tag=" + fromTag + ";to-tag=" + toTag);
			} catch (UnsupportedEncodingException e)
			{
				LOGGER.error(e.getMessage(), e);
			}
		}

		serviceMethod.setReferenceInformationListener(this);
	}

	public String getReferMethod()
	{
		return referMethod;
	}

	public String getReferToUserId()
	{
		return SIPUtils.getSipUri(referTo.toString());
	}

	public int getState()
	{
		return state;
	}

	private void setState(int state)
	{
		this.state = state;
		if (state == STATE_TERMINATED)
			getContainer().remove(this);
	}

	public void refer() throws ServiceClosedException
	{
		if (state != STATE_INITIATED)
			throw new IllegalStateException("Method accept() can be called only in the INITIATED state! (Current state is " + states[state] + ")");

		setState(STATE_REFERRING);

		MessageImpl message = (MessageImpl) getNextRequest();

		ReferToHeader header = getCoreService().getManager().getHeaderFactory().createReferToHeader(referTo);

		// if (referMethod != null)
		// {
		// try
		// {
		// header.setParameter("method", referMethod);
		// } catch (ParseException e)
		// {
		// throw new IllegalArgumentException("Could not set refer method: " +
		// referMethod + " " + e.getMessage());
		// }
		// }

		message.internalAddHeader(header.toString());
		sendNextRequest();
		setState(STATE_PROCEEDING);
		getContainer().remove(this);
	}

	public void setListener(ReferenceListener listener)
	{
		this.listener = listener;
	}

	public void referenceNotification(ServiceMethod serviceMethod, int state, int expires, String notification)
	{
		MessageImpl impl = new MessageImpl(-1, "NOTIFY");
		setNextRequest(impl);

		impl.internalAddHeader("Event", "refer");

		StringBuffer subsState = new StringBuffer();
		if (state == STATE_REFERENCE_ACTIVE)
		{
			subsState.append("active;expires=");
			subsState.append(expires);
		} else
		{
			subsState.append("terminated;reason=noresource");
		}

		impl.internalAddHeader("Subscription-State", subsState.toString());
		try
		{
			impl.createBodyPart(notification.getBytes("utf-8"), false, "Content-Type", "message/sipfrag");
		} catch (UnsupportedEncodingException e)
		{
			LOGGER.error(e.getMessage());
		}

		sendNextRequest();
	}

	protected void methodDelivered(MessageImpl responseMessage)
	{
		setState(STATE_REFERRING);

		if (listener != null)
			listener.referenceDelivered(this);
	}

	public void methodDeliveryFailed(MessageImpl responseMessage)
	{
		setState(STATE_TERMINATED);

		if (listener != null)
			listener.referenceDeliveryFailed(this);
	}

	protected void requestReceived(MessageImpl requestMessage)
	{
		sendResponse(200, "OK", null, null);

		SubscriptionStateHeader header = (SubscriptionStateHeader) requestMessage.getHeader("Subscription-State");
		if ("terminated".equals(header.getState()))
		{
			setState(STATE_TERMINATED);
			if (listener != null)
				listener.referenceTerminated(this);
			return;
		}

		if (listener != null)
			listener.referenceNotify(this, requestMessage);
	}

	protected MessageImpl createRequestMessage(Request request)
	{
		return new MessageImpl(Message.REFERENCE_REFER, request);
	}

	protected MessageImpl createResponseMessage(Response response)
	{
		return new MessageImpl(Message.REFERENCE_REFER, response);
	}

	public static void main(String[] args) throws ParseException
	{
		AddressFactoryImpl af = new AddressFactoryImpl();
		Address address = af.createAddress("<sip:bla@blub.de?Replaces=asdsad&dasdas=asdas>");

		System.out.println(address);
	}
}
