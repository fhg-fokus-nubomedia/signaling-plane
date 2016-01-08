package de.fhg.fokus.ims.core;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.ims.ImsException;
import javax.ims.core.Message;
import javax.ims.core.MessageBodyPart;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.TransactionUnavailableException;
import javax.sip.header.AcceptHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import de.fhg.fokus.ims.core.utils.SIPUtils;

/**
 * Implementation of the ServiceMethod Interface.
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @author Andreas Bachmann <andreas.bachmann@fraunhofer.fokus.de>
 */
public abstract class ServiceMethodImpl implements ServiceMethod2
{
	private static Logger LOGGER = LoggerFactory.getLogger(ServiceMethodImpl.class);
	
	/**
	 * Array containing all request messages
	 */
	private MessageImpl[] prevRequests = new MessageImpl[14];

	/**
	 * Array containing all response messages
	 */
	private MessageImpl[][] prevResponses = new MessageImpl[14][0];

	private MessageImpl nextRequest; // Outgoing request
	private MessageImpl nextResponse; // Outgoing response
	protected String remoteUserId;
	private String localUserId;
	protected CoreServiceImpl coreService;
	private Dialog dialog;
	private ReferenceInformationListener referenceInformationListener;
	private Request request;
	protected String callId;
	private Container container;
	private boolean outgoing = false;
	private String fromTag;
	private String toTag;

	/**
	 * Constructor for outgoing requests.
	 * 
	 * @param id
	 * @param method
	 * @param remoteUri
	 * @throws ImsException
	 */
	protected ServiceMethodImpl(CoreServiceImpl coreService, Container container, String remoteUri, String localUri) throws ImsException
	{
		this.coreService = coreService;
		remoteUserId = remoteUri;
		this.localUserId = localUri;
		this.container = container;
		outgoing = true;
	}

	/**
	 * Constructor for incoming requests
	 * 
	 * @param id
	 * @param request
	 * @param serverTransaction
	 * @throws ImsException
	 */
	protected ServiceMethodImpl(CoreServiceImpl coreService, Container container, Request request,String localUri) throws ImsException
	{
		this.coreService = coreService;
		this.request = request;
		remoteUserId = SIPUtils.getRequestorIdentity(request);
		this.localUserId = localUri;
		this.container = container;
		this.callId = ((CallIdHeader) request.getHeader("Call-ID")).getCallId();
		outgoing = false;
		this.dialog = coreService.getManager().getDialog(request);
	}

	public Message getPreviousRequest(int method) throws IllegalArgumentException
	{
		if (prevRequests[method] == null)
			throw new IllegalArgumentException("ServiceMethod:getPreviousRequest():Request for such method (" + method + ") doesn't exist!");

		return prevRequests[method];
	}

	public Message[] getPreviousResponses(int method)
	{
		return prevResponses[method];
	}

	public String getRemoteUserId()
	{
		return remoteUserId;
	}

	public void setRemoteUserId(String value)
	{
		if (!outgoing)
			throw new IllegalStateException("Remote user id can only be set in outgoing requests!");

		this.remoteUserId = value;
	}

	public String getFromTag()
	{
		return fromTag;
	}

	public String getToTag()
	{
		return toTag;
	}

	
	public Message getNextRequest()
	{
		return nextRequest;
	}

	public Message getNextResponse()
	{
		if (nextResponse == null)
		{
			Response response;
			try
			{
				response = coreService.getManager().createResponse(coreService, request, 200);
				nextResponse = createResponseMessage(response);
				nextResponse.setState(Message.STATE_UNSENT);
			} catch (ParseException e)
			{
				LOGGER.error(e.getMessage(), e);
				return null;
			}
		}

		return nextResponse;
	}

	protected void setNextRequest(MessageImpl nextRequest)
	{
		this.nextRequest = nextRequest;
	}

	protected CoreServiceImpl getCoreService()
	{
		return coreService;
	}

	public Request getRequest()
	{
		return request;
	}

	protected Container getContainer()
	{
		return container;
	}

	public String getCallId()
	{
		return callId;
	}

	public final void notifyRequest(Request request)
	{
		if (request.getMethod().equals(Request.CANCEL))
		{
			((SessionImpl) this).cancelReceived(request);
		} else
		{
			this.request = request;

			if (dialog == null)
				dialog = coreService.getManager().getDialog(request);

			MessageImpl requestMessage = createRequestMessage(request);
			setPreviousRequest(requestMessage);
			requestReceived(requestMessage);
		}
	}

	protected void requestReceived(MessageImpl requestMessage)
	{

	}

	public final void notifyResponse(Response response)
	{
		Dialog dialog = coreService.getManager().getDialog(response);
		if (this.dialog == null && dialog != null)
			this.dialog = dialog;

		MessageImpl responseMessage = createResponseMessage(response);
		addPreviousResponse(responseMessage);
		int code = responseMessage.getStatusCode();

		if (fromTag == null || toTag == null)
		{
			this.fromTag = ((FromHeader)response.getHeader("From")).getTag();
			this.toTag = ((ToHeader)response.getHeader("To")).getTag();
			System.err.println("from: " + fromTag + " to " + toTag);
		}

		if (code >= 100 && code < 200)
			provisionalResponse(responseMessage);
		else if (code >= 200 && code < 300)
		{
			methodDelivered(responseMessage);
		}
		else
			methodDeliveryFailed(responseMessage);
	}

	protected void provisionalResponse(MessageImpl responseMessage)
	{

	}

	protected void methodDelivered(MessageImpl responseMessage)
	{

	}

	protected void methodDeliveryFailed(MessageImpl responseMessage)
	{

	}

	protected void methodDeliveryTimeout()
	{

	}

	/**
	 * Sets the previous message for the given identifier
	 * 
	 * @param message
	 */
	protected void setPreviousRequest(MessageImpl message)
	{
		int id = message.getIdentifier();
		if (id >= 0 && id <= prevRequests.length - 1)
			prevRequests[id] = message;
	}

	protected void addPreviousResponse(MessageImpl responseMessage)
	{
		if (responseMessage.getIdentifier() < 0)
			return;

		MessageImpl[] temp = prevResponses[responseMessage.getIdentifier()];
		if (temp == null)
		{
			temp = new MessageImpl[1];
			temp[0] = responseMessage;
			prevResponses[responseMessage.getIdentifier()] = temp;
		} else
		{
			MessageImpl[] temp2 = new MessageImpl[temp.length + 1];
			System.arraycopy(temp, 0, temp2, 0, temp.length);
			temp2[temp2.length - 1] = responseMessage;
			prevResponses[responseMessage.getIdentifier()] = temp2;
		}
	}

	protected abstract MessageImpl createResponseMessage(Response response);

	protected abstract MessageImpl createRequestMessage(Request request);

	protected boolean sendNextRequest()
	{
		return sendNextRequest(null, 0);
	}
	
	public boolean sendNextRequest(String recipientAddress, int recipientPort) 
	{

		if (nextRequest == null)
			return false;

		try
		{
			Request currentRequest = null;

			if (Request.ACK.equals(nextRequest.getMethod()))
			{
				try
				{
					coreService.getManager().sendAck(getCoreService(), request);
					return true;
				} catch (InvalidArgumentException e)
				{
					e.printStackTrace();
					return false;
				}
			} else if (Request.CANCEL.equals(nextRequest.getMethod()))
			{
				currentRequest = coreService.getManager().createCancel(request);
			} else if (Request.BYE.equals(nextRequest.getMethod()))
			{
				Dialog dialog = coreService.getManager().getDialog(request);
				currentRequest = coreService.getManager().createRequest(dialog, Request.BYE);
								
				if (currentRequest.getHeader("Refer-To") != null)
					currentRequest.removeHeader("Refer-To");
			} else{
				currentRequest = createRequest(nextRequest);
			}										

			if(recipientAddress!=null && recipientPort>0)
			{
				coreService.getManager().sendP2PRequest(getCoreService(), currentRequest, recipientAddress, recipientPort);
			} else {
				coreService.getManager().sendRequest(getCoreService(), currentRequest);
			}

			callId = ((CallIdHeader) currentRequest.getHeader("Call-ID")).getCallId();

			//if (request == null)
			request = currentRequest;

			setPreviousRequest(nextRequest);
			return true;
		} catch (TransactionUnavailableException e)
		{
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		} catch (SipException e)
		{
			e.printStackTrace();
			LOGGER.info(e.getMessage());
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			return true;
		} catch (ImsException e)
		{
			e.printStackTrace();
			return true;
		}

		return false;	
	}


	private Request createRequest(MessageImpl message) throws IllegalArgumentException, ImsException
	{
		if (remoteUserId == null)
			throw new ImsException("createRequest():The remote uri cannot be null!");

		Request request = null;
		try
		{
			if (dialog == null || (dialog.getState() != DialogState.CONFIRMED))
				request = coreService.getManager().createRequest(message.getMethod(), remoteUserId, localUserId, remoteUserId, callId);
			else

				request = coreService.getManager().createRequest(dialog, message.getMethod());

			/* add headers if any */
			Iterator<?> it = message.getHeaders().entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry entry = (Entry) it.next();
				addHeader(request, (String) entry.getKey(), (String) entry.getValue());
			}

			/* add content if any */
			MessageBodyPart[] parts = message.getBodyParts();

			if (parts != null)
			{
				if (parts.length == 1)
				{
					MessageBodyPartImpl bodypart = (MessageBodyPartImpl) parts[0];
					String contentType = bodypart.getHeader("Content-Type");
					request.setContent(bodypart.getRawContent(), coreService.getManager().getHeaderFactory().createContentTypeHeader(
							SIPUtils.getType(contentType), SIPUtils.getSubType(contentType)));

				} else if (parts.length > 1)
				{
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					OutputStreamWriter writer = new OutputStreamWriter(output, "utf-8");
					String boundary = "42";

					for (int i = 0; i < parts.length; i++)
					{
						writer.write("--");
						writer.write(boundary);
						writer.write("\r\n");
						MessageBodyPartImpl bodypart = (MessageBodyPartImpl) parts[i];
						String contentType = bodypart.getHeader("Content-Type");

						writer.write("Content-Type: ");
						writer.write(contentType);
						writer.write("\r\n\r\n");

						writer.flush();

						output.write(bodypart.getOutputContent().toByteArray());
						output.write('\r');
						output.write('\n');
						output.write('\r');
						output.write('\n');
					}

					writer.write("--");
					writer.write(boundary);
					writer.write("--\r\n");
					writer.flush();

					ContentTypeHeader header = coreService.getManager().getHeaderFactory().createContentTypeHeader(SIPUtils.getType("multipart"),
							SIPUtils.getSubType("mixed"));
					header.setParameter("boundary", boundary);
					request.setContent(output, header);
				}
			}

		} catch (InvalidArgumentException e)
		{
			e.printStackTrace();
		} catch (ParseException e)
		{
			e.printStackTrace();
		} catch (Exception exception)
		{
			LOGGER.error(exception.getMessage(), exception);
			throw new ImsException("Message:createRequest():Error during request creation! " + exception.getMessage());
		}

		return request;
	}

	protected void sendResponse(int statusCode, String reasonPhrase, String contentType, byte[] payload)
	{
		LOGGER.debug("sending response: {new Integer("+statusCode+"} {"+reasonPhrase+"}");

		Response response = null;
		MessageImpl responseMessage = null;
		if (nextResponse != null)
		{
			response = nextResponse.getResponse();
			responseMessage = nextResponse;
		}

		try
		{
			ContentTypeHeader ctHeader = null;

			if (payload == null & responseMessage == null)
			{
				response = coreService.getManager().createResponse(coreService, request, statusCode);
			} else
			{
				if (response == null)
				{
					int index = contentType.indexOf("/");
					String type = contentType.substring(0, index);
					String subType = contentType.substring(index + 1);

					ctHeader = coreService.getManager().getHeaderFactory().createContentTypeHeader(type, subType);

					response = coreService.getManager().createResponse(coreService, request, statusCode, ctHeader, payload);
				} else
				{
					response.setStatusCode(statusCode);
					response.setReasonPhrase(reasonPhrase);

					if (responseMessage == null)
					{
						response.setContent(payload, ctHeader);
					} else
					{
						if (payload == null)
						{
							MessageBodyPart[] parts = responseMessage.getBodyParts();
							for (int i = 0; i < parts.length; i++)
							{
								MessageBodyPartImpl bodypart = (MessageBodyPartImpl) parts[i];

								contentType = bodypart.getHeader("Content-Type");
								response.setContent(bodypart.openContentOutputStream(), coreService.getManager().getHeaderFactory().createContentTypeHeader(
										SIPUtils.getType(contentType), SIPUtils.getSubType(contentType)));
							}
						} else
						{
							response.setContent(payload, coreService.getManager().getHeaderFactory().createContentTypeHeader(SIPUtils.getType(contentType),
									SIPUtils.getSubType(contentType)));
						}
					}
				}
			}

			if (reasonPhrase != null)
				response.setReasonPhrase(reasonPhrase);

			if (responseMessage == null)
				responseMessage = createResponseMessage(response);

			/* add headers if any */
			if (responseMessage.getHeaders() != null)
			{
				Iterator<?> it = responseMessage.getHeaders().entrySet().iterator();
				while (it.hasNext())
				{
					Map.Entry entry = (Entry) it.next();
					addHeader(response, (String) entry.getKey(), (String) entry.getValue());
				}
			}

			LOGGER.debug("giving response to coreService");
			coreService.getManager().sendResponse(getCoreService(), request, response);

			addPreviousResponse(responseMessage);

			if (fromTag == null || toTag == null)
			{
				this.fromTag = ((FromHeader)request.getHeader("From")).getTag();
				this.toTag = ((ToHeader)response.getHeader("To")).getTag();
				
				System.err.println("from: " + fromTag + " to " + toTag);
			}
			
			// Clear next response
			this.nextResponse = null;
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}

	protected void addHeader(javax.sip.message.Message message, String key, String value) throws IllegalStateException, ImsException, IllegalArgumentException
	{
		try
		{
			if (key.equals("Expires"))
			{
				ExpiresHeader expiresHeader = coreService.getManager().getHeaderFactory().createExpiresHeader(Integer.parseInt(value));
				message.setExpires(expiresHeader);
			} else if (key.equals("Event"))
			{
				EventHeader eventHeader = coreService.getManager().getHeaderFactory().createEventHeader(value);
				message.setHeader(eventHeader);

				AcceptHeader header = null;
				if (value.equalsIgnoreCase("presence"))
				{
					header = coreService.getManager().getHeaderFactory().createAcceptHeader("application", "pidf+xml");
					message.addHeader(header);
				} else if (value.equalsIgnoreCase("presence.winfo"))
				{
					header = coreService.getManager().getHeaderFactory().createAcceptHeader("application", "watcherinfo+xml");
					message.addHeader(header);
				} else if (value.equalsIgnoreCase("reg"))
				{
					header = coreService.getManager().getHeaderFactory().createAcceptHeader("application", "reginfo+xml");
					message.addHeader(header);
				} else if (value.startsWith("ua-profile"))
				{
					/*
					 * there is nothing specific in the event for XDMS, we put
					 * this always, as default
					 */
					header = coreService.getManager().getHeaderFactory().createAcceptHeader("application", "xml");
					message.addHeader(header);
				}
			} else if (key.equals("Content-Type"))
			{
				String type = SIPUtils.getType(value);
				String subType = SIPUtils.getSubType(value);
				message.addHeader(coreService.getManager().getHeaderFactory().createContentTypeHeader(type, subType));
			} else if (key.equals("Sip-If-Match"))
			{
				ExtensionHeader sipIfMatchHeader = (ExtensionHeader) coreService.getManager().getHeaderFactory().createHeader(key, value);
				message.addHeader(sipIfMatchHeader);
			} else
			{
				Header header = coreService.getManager().getHeaderFactory().createHeader(key, value);
				message.addHeader(header);
			}
		} catch (Exception sipexception)
		{
			throw new IllegalArgumentException("Header '" + key + "' cannot be added to the request. Exception: " + sipexception.getMessage());
		}
	}

	public Dialog getDialog()
	{
		return dialog;
	}

	protected void setDialog(Dialog dialog)
	{
		this.dialog = dialog;
	}

	public void setReferenceInformationListener(ReferenceInformationListener listener)
	{
		this.referenceInformationListener = listener;
	}

	protected void referenceNotify(int state, int expires, String notification)
	{
		try
		{

			if (referenceInformationListener != null)
				referenceInformationListener.referenceNotification(this, state, expires, notification);
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}

//	public void setLocalUserId(String localUserId) {
//		this.localUserId = localUserId;
//	}
//
//	public String getLocalUserId() {
//		return localUserId;
//	}
}
