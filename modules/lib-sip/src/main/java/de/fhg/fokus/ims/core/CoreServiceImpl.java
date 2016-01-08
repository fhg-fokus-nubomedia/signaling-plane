package de.fhg.fokus.ims.core;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import java.net.InetAddress;
import java.util.Timer;

import javax.ims.ConnectionState;
import javax.ims.ImsException;
import javax.ims.ServiceClosedException;
import javax.ims.core.Capabilities;
import javax.ims.core.CoreServiceListener;
import javax.ims.core.Message;
import javax.ims.core.PageMessage;
import javax.ims.core.Publication;
import javax.ims.core.Reference;
import javax.ims.core.Session;
import javax.ims.core.Subscription;
import javax.sip.address.SipURI;
import javax.sip.header.CallIdHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.FromHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import de.fhg.fokus.ims.ServiceImpl;
import de.fhg.fokus.ims.core.media.BasicReliableMediaImpl;
import de.fhg.fokus.ims.core.media.BasicUnreliableMediaImpl;
import de.fhg.fokus.ims.core.media.FramedMediaImpl;
import de.fhg.fokus.ims.core.media.MediaImpl;
import de.fhg.fokus.ims.core.utils.SIPUtils;

/**
 * Implementation of the CoreService Interface.
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @author Andreas Bachmann (andreas.bachmann@fokus.fraunhofer.de)
 */
public class CoreServiceImpl extends ServiceImpl implements CoreService2, IMSManagerListener
{
	private static Logger LOGGER = LoggerFactory.getLogger(CoreServiceImpl.class);

	/**
	 * Reference to the manager, responsible for this service
	 */
	private IMSManager manager;

	/**
	 * The listener assigned to this service
	 */
	private CoreServiceListener listener;

	private String localUserId;

	private String id;

	/* Service session containers */
	private SessionContainer sessionContainer = new SessionContainer();

	private PageMessageContainer messageContainer = new PageMessageContainer();

	private SubscriptionContainer subscriptionsContainer = new SubscriptionContainer();

	private PublicationContainer publicationContainer = new PublicationContainer();
	
	private CapabilityContainer capabilityContainer = new CapabilityContainer();

	private StreamMediaFactoryBase streamMediaFactory;

	private boolean closed = false;

	private ReferenceContainer referenceContainer = new ReferenceContainer();

	private FeatureTagSet featureSet;
	
	private boolean defaultCoreService = false;

	public boolean isDefaultCoreService() {
		return defaultCoreService;
	}

	public void setDefaultCoreService(boolean defaultCoreService) {
		this.defaultCoreService = defaultCoreService;
	}

	/**
	 * Gets a timer instance, that can be used for scheduling the
	 * refreshing/retry/terminating tasks.
	 */
	public Timer getTimer()
	{
		return manager.getTimer();
	}

	public CoreServiceImpl(IMSManager manager, String coreServiceId, String appId, String userId)
	{
		super(appId, "imscore");
		this.manager = manager;
		this.localUserId = userId;
		this.id = coreServiceId;
	}

	public String getId()
	{
		return id;
	}

	public FeatureTagSet getFeatureSet()
	{
		return featureSet;
	}

	public void setFeatureTagSet(FeatureTagSet featureSet)
	{
		this.featureSet = featureSet;
	}

	public IMSManager getManager()
	{
		return manager;
	}

	public boolean isConnected()
	{
		return ConnectionState.getConnectionState().isConnected();
	}

	/**
	 * Computes the public IP address of the device
	 * 
	 * Mechanism; opens a socket to the pCSCF and if that fails, it tries to
	 * open the socket to the xdms In case that fails, then it returns the local
	 * host
	 */
	public InetAddress getLocalEndpoint()
	{
		return manager.getLocalAddress();
	}

	public String getServiceRoute()
	{
		StringBuffer buffer = new StringBuffer();

		// TODO: get service route from manager
		return buffer.toString();
	}

	public ReferenceContainer getReferences()
	{
		return referenceContainer;
	}

	public SubscriptionContainer getSubscriptions()
	{
		return subscriptionsContainer;
	}

	public void close()
	{
		if (closed)
			return;

		try
		{
			sessionContainer.close();
			sessionContainer = null;

			publicationContainer.close();
			publicationContainer = null;

			Unsubscriber unsubscriber = new Unsubscriber(subscriptionsContainer);
			unsubscriber.run();
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		} finally
		{
			closed = true;
			manager.closed(this);
			if (listener != null)
				listener.serviceClosed(this);
		}
	}

	/***************** core service implementation ********************** */
	public Capabilities createCapabilities(String fromUserId, String toUserId) throws IllegalArgumentException, ServiceClosedException,
	ImsException
	{

		if (closed)
			throw new ServiceClosedException("Service is closed");
		try
		{
			CapabilitiesImpl cap = new CapabilitiesImpl(this, capabilityContainer, toUserId, fromUserId);
			return cap;
		} catch (ImsException e)
		{
			LOGGER.error("there has been an error in creating the capability object");
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
		
	}

	public PageMessage createPageMessage(String fromUserId, String toUserId) throws ServiceClosedException
	{
		if (closed)
			throw new ServiceClosedException("Service is closed");

		if (!ConnectionState.getConnectionState().isConnected())
			throw new IllegalStateException("The core is not connected to the IMS network");

		try
		{
			return new PageMessageImpl(this, messageContainer, toUserId, fromUserId);
		} catch (ImsException e)
		{
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	public Publication createPublication(String fromUserId, String toUserId, String event) throws IllegalArgumentException, ServiceClosedException,
			ImsException
	{
		if (closed)
			throw new ServiceClosedException("Service is closed");

		Publication pub = (Publication) publicationContainer.get(event);

		if (pub == null)
		{
			try
			{
				pub = new PublicationImpl(this, publicationContainer, fromUserId, toUserId, event);
				return pub;
			} catch (ImsException e)
			{
				LOGGER.error("there has been an error in creating the publication object");
				LOGGER.error(e.getMessage(), e);
				throw e;
			}
		} else
			return pub;
	}

	public Reference createReference(String fromUserId, String toUserId, String referToUserId, String referMethod) throws ServiceClosedException
	{
		if (closed)
			throw new ServiceClosedException("Service is closed");

		try
		{
			return new ReferenceImpl(this, referenceContainer, toUserId, referMethod, referToUserId, fromUserId);
		} catch (ImsException e)
		{
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}

	public Session createSession(String fromUserId, String toUserId) throws ServiceClosedException, ImsException
	{
		if (closed)
			throw new ServiceClosedException("Service is closed");

		if (!ConnectionState.getConnectionState().isConnected())
			throw new IllegalStateException("The core is not connected to the IMS network");

		if (toUserId == null)
			throw new IllegalArgumentException("CoreServiceImpl.createSession(): The remote uri cannot be null");

		return new SessionImpl(this, sessionContainer, toUserId, fromUserId);
	}

	public Subscription createSubscription(String fromUserId, String toUserId, String event) throws ServiceClosedException, ImsException
	{
		if (closed)
			throw new ServiceClosedException("Service is closed");

		if (!ConnectionState.getConnectionState().isConnected())
			throw new IllegalStateException("The core is not connected to the IMS network");

		if (event == null)
			throw new IllegalArgumentException("Event must not be null");

//XXX this prevented creating more than one subscription per user+event
//		SubscriptionImpl subscription = subscriptionsContainer.get(toUserId, event);
//		if (subscription != null)
//			return subscription;

		LOGGER.info("Creating subscription for: {}" + toUserId);
		SubscriptionImpl subscription = new SubscriptionImpl(this, subscriptionsContainer, fromUserId, toUserId, event);
		return subscription;
	}

	public String getLocalUserId() throws IllegalStateException
	{
		return localUserId;
	}

	public void setListener(CoreServiceListener listener)
	{
		if (listener == null)
			this.listener = null;
		else
			this.listener = listener;
	}

	public CoreServiceListener getListener()
	{
		return listener;
	}

	public void setStreamMediaFactory(StreamMediaFactoryBase streamMediaFactory)
	{
		this.streamMediaFactory = streamMediaFactory;
	}

	public void request(Request request)
	{
		String method = request.getMethod();
		LOGGER.info("CoreServiceImpl: Incoming Request\r\n{"+request+"}");
		LOGGER.info("> Incoming request: {"+method+"} {"+request.getRequestURI()+"} {"+ request.getHeader("From")+"}");

		if (method.equalsIgnoreCase(Request.MESSAGE))
		{
			handleIncomingMessage(request);
		} else if (method.equalsIgnoreCase(Request.NOTIFY))
		{
			handleIncomingNotify(request);
		} else if (method.equalsIgnoreCase(Request.INVITE) || method.equalsIgnoreCase(Request.ACK) || method.equalsIgnoreCase(Request.BYE)
				|| method.equalsIgnoreCase(Request.CANCEL) || method.equalsIgnoreCase(Request.PRACK))
		{
			handleIncomingSessionResquest(request);
		} else if (method.equals(Request.REFER))
		{
			handleIncomingReferRequest(request);
		} else if (Request.OPTIONS.equals(method))
		{
			handleIncomingOptionsRequest(request);
		} else if (method.equalsIgnoreCase(Request.INFO))
		{
			LOGGER.info(method + " requests are not handled by ims client core.");
			handleIncomingInfoRequest(request);
		} else if (method.equals(Request.SUBSCRIBE))
		{
			handleIncomingSubscribeRequest(request);
		} else
		{
			LOGGER.info(method + " requests are not handled by ims client core.");
			sendErrorResponse(request, Response.NOT_IMPLEMENTED);
		}
	}

	public void provisionalResponse(Response response)
	{
		Request request = manager.getRequest(response);
		if (request == null)
		{
			LOGGER.info("Received response but not request exists!");
			return;
		}

		String method = request.getMethod();
		if (method.equalsIgnoreCase(Request.INVITE) || method.equalsIgnoreCase(Request.ACK) || method.equalsIgnoreCase(Request.PRACK)
				|| method.equalsIgnoreCase(Request.BYE))
		{
			sessionContainer.dispatch(response, request);
		}
	}

	public void finalResponse(Response response)
	{
		Request request = manager.getRequest(response);
		if (request == null)
		{
			LOGGER.info("Received response but not request exists!");
			return;
		}
		String method = request.getMethod();

		if (method.equalsIgnoreCase(Request.MESSAGE))
		{
			messageContainer.dispatch(response, request);
		} else if (method.equalsIgnoreCase(Request.PUBLISH))
		{
			publicationContainer.dispatch(response, request);

		} else if (method.equalsIgnoreCase(Request.SUBSCRIBE) || method.equals(Request.NOTIFY))
		{
			SessionImpl session = null;
			if (sessionContainer != null)
				session = sessionContainer.get(request);

			if (session != null)
			{
				if (((EventHeader) request.getHeader("Event")).getEventType().equals("refer"))
				{

				} else
				{
					session.getSubscriptions().dispatch(response, request);
				}
			} else
				subscriptionsContainer.dispatch(response, request);
		} else if (method.equalsIgnoreCase(Request.INVITE) || method.equalsIgnoreCase(Request.ACK) || method.equalsIgnoreCase(Request.PRACK)
				|| method.equalsIgnoreCase(Request.BYE))
		{ 
			sessionContainer.dispatch(response, request);
		} else if (method.equalsIgnoreCase(Request.INFO))
			LOGGER.info("INFO not yet implemented");
		else if (method.equalsIgnoreCase(Request.REFER))
		{
			SessionImpl session =  sessionContainer.get(request);
			if (session == null)
				referenceContainer.dispatch(response, request);
			else
				session.getOutgoingReference().notifyResponse(response);
		} else if (method.equalsIgnoreCase(Request.OPTIONS))
		{
			CapabilitiesImpl capabilities = capabilityContainer.get(request);
			if(capabilities == null)
				capabilityContainer.dispatch(response, request);
//			else
//				capabilities.getListener().capabilityQueryDelivered(capabilities);

		} 
		else
			LOGGER.info("Responses for " + request.getMethod() + " are not handled." + response.getStatusCode() + " " + response.getReasonPhrase()
					+ " received.");
	}

	public void timeout(Request request)
	{
		LOGGER.debug("Timeout for request: {"+request.getMethod()+"} {"+request.getRequestURI()+"}");
		String method = request.getMethod();
		if (method.equals(Request.PUBLISH))
			publicationContainer.timeout(request);
		else if (method.equals(Request.SUBSCRIBE))
			subscriptionsContainer.timeout(request);
		else if (method.equals(Request.MESSAGE))
			messageContainer.timeout(request);
		else if (method.equals(Request.INVITE) || method.equals(Request.BYE) || method.equals(Request.CANCEL) || method.equals(Request.UPDATE))
			sessionContainer.timeout(request);
		else if (method.equals(Request.REFER))
			referenceContainer.timeout(request);
	}

	private void sendErrorResponse(Request request, int statusCode)
	{
		Response response;
		try
		{
			response = manager.createResponse(request, statusCode);
			manager.sendResponse(this, request, response);
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage());
		}
	}

	/* ********* handlers for incoming messages ************************** */
	
	/**
	 * Handles incoming page message request
	 * @param request
	 */
	private void handleIncomingMessage(Request request)
	{
		LOGGER.debug("Handling incoming page message request");
		
		PageMessageImpl pageMessage = null;
		try
		{
			pageMessage = new PageMessageImpl(this, null, request, null);
			pageMessage.sendResponse(Response.OK, "OK", null, null);
		} catch (Exception ex)
		{
			LOGGER.error(ex.getMessage(), ex);
			return;
		}

		if (listener != null)
			listener.pageMessageReceived(this, pageMessage);
	}

	private void handleIncomingNotify(Request request)
	{
		LOGGER.debug("Handling incoming notify request");
		
		FromHeader fromHeader = (FromHeader) request.getHeader("From");
		String toUserId = SIPUtils.getIdentity((SipURI) fromHeader.getAddress().getURI());

		EventHeader eh = (EventHeader) request.getHeader(EventHeader.NAME);

		if (eh == null)
		{
			sendErrorResponse(request, 400);
			return;
		}
		
		String event = eh.getEventType().toString();

		int i = event.indexOf(';');
		if (i > -1)
			event = event.substring(0, i);

		if (event.equals("refer"))
		{
			LOGGER.debug("Handling NOTIFY for REFER request");
			
			ReferenceImpl reference = referenceContainer.get(manager.getDialog(request));
			if (reference == null)
			{
				SessionImpl session = sessionContainer.get(request);

				if (session == null || session.getOutgoingReference() == null)
					sendErrorResponse(request, Response.GONE);
				else
				{
					LOGGER.debug("Handling NOTIFY for session REFER");
					session.getOutgoingReference().notifyRequest(request);
				}
			} else
			{
				LOGGER.debug("Handling NOTIFY for standalone REFER");
				reference.notifyRequest(request);
			}
		} else
		{
			LOGGER.debug("Handling NOTIFY for SUBSCRIBE request");
			
			SubscriptionImpl sub = null;

			if (sessionContainer != null)
			{
				SessionImpl session = sessionContainer.get(request);
				if (session != null)
				{
					sub = session.getSubscriptions().get(((CallIdHeader)request.getHeader("Call-ID")).getCallId());
					if (sub != null)
						LOGGER.debug("Handling NOTIFY for session SUBSCRIBE");
					else{
						LOGGER.debug("could not get subscription from sessions");
					}
				}
				else{
					LOGGER.debug("Could not find session for subscription");
				}
			}

			if (sub == null)
			{	
//				XXX
//				sub = subscriptionsContainer.get(toUserId, event);
				sub = subscriptionsContainer.get(((CallIdHeader)request.getHeader("Call-ID")).getCallId());
				if (sub != null)
					LOGGER.debug("Handling NOTIFY for session SUBSCRIBE");
			}

			if (sub == null)
			{
				LOGGER.info("Received a NOTIFY for " + toUserId + " but I couldn't retrieve the subscription,");
				sendErrorResponse(request, Response.GONE);
				return;
			}

			sub.notifyRequest(request);
		}
	}

	private void handleIncomingReferRequest(Request request)
	{
		try
		{
			ReferenceImpl reference = new ReferenceImpl(this, referenceContainer, request, null);

			SessionImpl session = sessionContainer.get(request);

			if (session != null)
				session.reference(reference);
			else if (listener != null)
				listener.referRequestReceived(this, reference);

		} catch (ImsException e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Handles incoming BYE, CANCEL, UPDATE, and INVITE requests
	 * 
	 * @param request
	 *            - the incoming request
	 * @param serverTransaction
	 *            - the server transaction of the request
	 */
	private void handleIncomingSessionResquest(Request request)
	{
		LOGGER.info("Handling incoming session request "+request.getMethod());
		
		String method = request.getMethod();

		SessionImpl session = sessionContainer.get(request);

		if (Request.INVITE.equalsIgnoreCase(method))
		{
			try
			{
				if (session == null)
				{
					LOGGER.info("Creating new session object for incoming request");
					session = new SessionImpl(this, sessionContainer, request, null);
					if (listener != null)
						listener.sessionInvitationReceived(this, session);
					
					return;
				}
			} catch (ImsException e)
			{
				LOGGER.error(e.getMessage(), e);
			}
		}

		if (session == null)
		{
			if (!Request.ACK.equals(method))
				sendErrorResponse(request, Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
			return;
		}

		LOGGER.info("Forwarding request to existing session");
		session.notifyRequest(request);
	}

	private void handleIncomingOptionsRequest(Request request)
	{
		try
		{
			CapabilitiesImpl capabilities= null;
			if(capabilityContainer == null || (capabilities = capabilityContainer.get(request)) == null)
			{
				capabilities = new CapabilitiesImpl(this, capabilityContainer, request, null);
				capabilities.sendResponse(200, "OK", null, null);

//				if (listener != null && listener instanceof CoreServiceListenerAdapter)
//					((CoreServiceListenerAdapter) listener).capabilityReceived(this, capabilities);
				
				return;
			}
		} catch (ImsException e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void handleIncomingSubscribeRequest(Request request)
	{
		try
		{
			SessionImpl session = null;
			if (sessionContainer == null || (session = sessionContainer.get(request)) == null)
			{
				SubscriptionImpl subscription = new SubscriptionImpl(this, this.subscriptionsContainer, request, null);
				if (listener != null && listener instanceof CoreServiceListener2)
					((CoreServiceListener2) listener).subscriptionReceived(this, subscription);
			} else if (session != null)
			{
				SubscriptionImpl subscription = new SubscriptionImpl(this, session.getSubscriptions(), request, null);
				session.subscription(subscription);
			}
		} catch (ImsException e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	private void handleIncomingInfoRequest(Request request)
	{
		try
		{
			ServiceMethodImpl smethod = new ServiceMethodImpl(this, null, request, null) {
				
				protected MessageImpl createResponseMessage(Response response) {
					
					return new MessageImpl(Message.INFOMESSAGE_SEND, response);
				}
				
				protected MessageImpl createRequestMessage(Request request) {
					return null;
				}
			};
			smethod.sendResponse(Response.OK, "OK", null, null);
		} catch (Exception ex)
		{
			LOGGER.error(ex.getMessage(), ex);
			return;
		}		
	}

	/**
	 * Creates new media object
	 * 
	 * @param session
	 * @param type
	 * @return
	 */
	protected MediaImpl createMedia(SessionImpl session, String type)
	{
		if ("StreamMedia".equals(type))
		{
			if(streamMediaFactory == null)
				return null;
			return this.streamMediaFactory.createMedia(session);
		} else if ("FramedMedia".equals(type))
			return new FramedMediaImpl(session);
		else if ("BasicReliableMedia".equals(type))
			return new BasicReliableMediaImpl(session);
		else if ("BasicUnreliableMedia".equals(type))
			return new BasicUnreliableMediaImpl(session);
		return null;
	}
}
