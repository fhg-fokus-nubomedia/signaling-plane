package de.fhg.fokus.ims.core;

import gov.nist.javax.sip.header.AcceptContact;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.SIPHeaderNames;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.SIPDialog;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;

import javax.ims.Configuration;
import javax.ims.ConnectionState;
import javax.ims.ImsException;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransactionUnavailableException;
import javax.sip.TransportNotSupportedException;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.AcceptContactHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RequireHeader;
import javax.sip.header.RetryAfterHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.swing.event.EventListenerList;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import de.fhg.fokus.ims.core.auth.Authenticator;
import de.fhg.fokus.ims.core.auth.AKA.DigestAKAResponse;
import de.fhg.fokus.ims.core.net.NetworkService;
import de.fhg.fokus.ims.core.net.NetworkServiceListener;

/**
 * Manager for mapping core services to the sip stack.
 * 
 * This class will keep trace of all core service instances and the capabilities
 * they provide. It also manages the one and only sip stack instance and
 * dispatches incoming message.
 * 
 * @author Andreas Bachmann (andreas.bachmann@fokus.fraunhofer.de)
 * 
 */
public final class IMSManager implements SipListener, NetworkServiceListener
{	
	private static Logger LOGGER = LoggerFactory.getLogger(IMSManager.class);


	private EventListenerList listeners = new EventListenerList();
	/**
	 * Name of the sip stack
	 */
	private static final String SIP_STACK_NAME = "FOKUS OPENXSP SIP Stack";

	/**
	 * Object placeholder for time out responses
	 */
	private static final Integer TIMEOUT = new Integer(1);

	/**
	 * Constant values for services without service id.
	 */
	private static final String NULL_SERVICE_ID = "NullServiceID";

	/**
	 * The one and only sip stack
	 */
	private SipStack sipStack;

	/**
	 * The one and only sip provider
	 */
	private SipProvider sipProvider;

	/**
	 * the instance for the
	 */
	private static IMSManager instance = new IMSManager();

	/**
	 * Map of active server transactions - key: <method>#<call id> (
	 * {@link #getTransactionKey(Request)}
	 */
	private HashMap serverTransactions = new HashMap();

	/**
	 * Map of requests where no final response arrived. Key: The Call ID of the
	 * request.
	 */
	private HashMap outstandingRequests = new HashMap();

	/**
	 * Map of core service instances. Key: Service ID of CoreService instance
	 */
	private HashMap coreServices = new HashMap();

	private CoreServiceImpl[] coreServiceList;

	/**
	 * Map of incoming responses, used for synced sending - key: outgoing
	 * request
	 */
	private HashMap responses = new HashMap();

	/**
	 * Map of active dialogs. Key: Call-ID of dialog creating request
	 */
	private HashMap dialogs = new HashMap();

	/**
	 * Map of sip providers for secure and un-secure ports. Key: SipProviderType object
	 */
	private HashMap sipProviders = new HashMap();

	/**
	 * Holds the factory for creating headers
	 */
	private HeaderFactory headerFactory;

	/**
	 * Holds the factory for creating messages
	 */
	private MessageFactory messageFactory;

	/**
	 * Holds the factory for creating addresses
	 */
	private AddressFactory addressFactory;

	/**
	 * The one and only listening point for UDP (TODO: check if there is more
	 * than one required)
	 */
	private ListeningPoint udpListeningPoint;

//	private ListeningPoint tcpListeningPoint;

	/**
	 * Configuration data: the ip of the remote proxy
	 */
	private String remoteIP;

	/**
	 * Configuration data: the port of the remote proxy
	 */
	private int remotePort;

	/**
	 * Configuration data: the local port of the sip stack
	 */
	private int localPort;

	private String transport = "UDP";

	private List viaHeader;

	private String privateUserId;

	private String[] publicUserId;

	private String domain;

	private String secretKey;

	private Authenticator authenticator;

	private int cseq = 1;
	private int tag = 1000;

	// Header
	private MaxForwardsHeader maxForwardsHeader;

	private UserAgentHeader userAgentHeader;

	private ArrayList allowHeaders;

	private int registeredCoreServices = 0;

	private boolean isRunning = false;

	private int expireTime = 3600;

	private boolean enableSessionTimer = false;

	private boolean prackSupport = false;

	private int sessionExpireTimer = 3600;

	private InetAddress localAddress;

	private Registration registration;

	/**
	 * Configuration data: the ip of the local interface used for communication
	 */
	private String localAddressString;

	private StreamMediaFactoryBase streamMediaFactory;

	private Timer timer = new Timer(true);

	private boolean sendRegistration = true;

	private SIPLog sipLog = new SIPLog(1000);

	private boolean enableLog = true;

	private String gruu;

	private String displayName;

	private boolean unregisterAll = false;

	private SupportedHeader supported100Rel = null;

	private SupportedHeader supportedTimer = null;

	private ExtensionHeader sessionExpiresHeader = null;

	private TimerTask timerTask;

	private boolean subscribeToReg;	

	private boolean isimAuth = false;

	private Vertx vertx;


	class RequestRetransmissionTask extends TimerTask
	{
		Request request;
		RequestData requestData;

		public RequestRetransmissionTask(RequestData requestData, Request request)
		{
			this.requestData = requestData;
			this.request = request;
		}

		public void run()
		{
			try
			{
				if (requestData != null)
					sendRequest(requestData.coreService, requestData.request);
				else if (request != null)
					sendRequest(request);
			} catch (Exception e)
			{
				LOGGER.error("retransmitting request failed");
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	private IMSManager()
	{
	}

	public static IMSManager getInstance()
	{
		return instance;
	}

	public SIPLog getSIPLog()
	{
		return sipLog;
	}

	public boolean isLogEnabled()
	{
		return enableLog;
	}

	public AddressFactory getAddressFactory()
	{
		return addressFactory;
	}

	public HeaderFactory getHeaderFactory()
	{
		return headerFactory;
	}

	public String getRemoteIP()
	{
		return remoteIP;
	}

	public void setRemoteIP(String ipAddress)
	{
		this.remoteIP = ipAddress;
	}

	public InetAddress getLocalAddress()
	{
		return localAddress;
	}

	public void setLocalAddress(InetAddress localAddress)
	{
		System.out.println("setting local address "+ localAddress);
		this.localAddress = localAddress;
		localAddressString = localAddress.getHostAddress();
	}

	public int getRemotePort()
	{
		return remotePort;
	}

	public void setRemotePort(int remotePort)
	{
		this.remotePort = remotePort;
	}

	public int getLocalPort()
	{
		return localPort;
	}
	
	public void setVertx(Vertx vertx){
		this.vertx = vertx;
	}
	
	public void setLocalPort(int localPort)
	{
		this.localPort = localPort;
	}

	public void setPrivateUserId(String privateUserId)
	{
		this.privateUserId = privateUserId;
	}

	public String[] getPublicUserId()
	{
		String[] res = new String[publicUserId.length];
		System.arraycopy(publicUserId, 0, res, 0, res.length);
		return res;
	}

	public Timer getTimer()
	{
		return timer;
	}

	public int getSessionExpireTimer()
	{
		return sessionExpireTimer;
	}

	public Registration getRegistration() {
		return registration;
	}

	public void setPublicUserId(String[] userId)
	{
		this.publicUserId = new String[userId.length];
		System.arraycopy(userId, 0, this.publicUserId, 0, userId.length);
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	public void setSecretKey(String secretKey)
	{
		this.secretKey = secretKey;
	}

	public void setExpireTime(int expireTime)
	{
		this.expireTime = expireTime;
	}

	public void setSessionExpireTime(int expireTime)
	{
		this.sessionExpireTimer = expireTime;
	}

	public void setSessionExpireTime(boolean isEnabled)
	{
		this.enableSessionTimer = isEnabled;
	}

	public void setPrackSupport(boolean isPrackSupported)
	{
		this.prackSupport = isPrackSupported;
	}

	public void setSendRegistration(boolean value)
	{
		this.sendRegistration = value;
	}

	public void setStreamMediaFactory(StreamMediaFactoryBase streamMediaFactory)
	{
		this.streamMediaFactory = streamMediaFactory;
	}

	public void setEnableSipLog(boolean enableLog)
	{
		this.enableLog = enableLog;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public void setGRUU(String gruu)
	{
		this.gruu = gruu;
	}

	public void setTransport(String transport)
	{
		this.transport = transport;
	}

	public void setSubscribeToReg(boolean subscribeToReg)
	{
		this.subscribeToReg = subscribeToReg;
	}

	public void setISIMAuth(boolean isISIMAuth) {
		this.isimAuth = isISIMAuth;
	}


	public CoreServiceImpl openCoreService(String appId, String serviceId, String pUserId) throws IOException
	{
		if (!isRunning)
			throw new IOException("IMS Network is not reachable!");

		if (serviceId == null)
			serviceId = NULL_SERVICE_ID;

		String userId = pUserId;

		if (userId == null)
			userId = publicUserId[0];

		CoreServiceImpl coreService = (CoreServiceImpl) coreServices.get(serviceId);

		if (coreService == null)
		{
			String coreServiceId = serviceId;
			coreService = new CoreServiceImpl(this, coreServiceId, appId, userId);
			coreService.setStreamMediaFactory(streamMediaFactory);

			try
			{
				if (registeredCoreServices == 0)
				{
					registration.register();
				}
				registeredCoreServices++;
			} catch (Exception e)
			{
				LOGGER.error(e.getMessage(), e);
				throw new IOException(e.getMessage());
			}

			coreService.setFeatureTagSet(new FeatureTagSet(registration.getFeatureTags(coreService)));
			coreServices.put(coreServiceId, coreService);
			coreServiceList = (CoreServiceImpl[]) coreServices.values().toArray(new CoreServiceImpl[coreServices.size()]);
		}

		ConnectionState.getConnectionState().setPublicIdentities(publicUserId);
		ConnectionState.getConnectionState().setConnectionStatus(registeredCoreServices > 0);
		return coreService;
	}

	/**
	 * Removes the given core service from the internal map of services
	 * 
	 * @param coreServiceImpl
	 */
	public void closed(CoreServiceImpl coreServiceImpl)
	{
		LOGGER.info("closing coreservice");
		try
		{
			if (!unregisterAll)
				unregisterCoreService(coreServiceImpl);
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		} finally
		{
			this.coreServices.remove(coreServiceImpl.getId());
			this.coreServiceList = (CoreServiceImpl[]) coreServices.values().toArray(new CoreServiceImpl[coreServices.size()]);
			ConnectionState.getConnectionState().setPublicIdentities(new String[0]);
			ConnectionState.getConnectionState().setConnectionStatus(coreServices.size() > 0);
		}
	}

	public Dialog getDialog(Request request)
	{
		String id = ((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId();
		return (Dialog) dialogs.get(id);
	}

	public void stopDialog(Request request)
	{
		String id = ((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId();
		SIPDialog dialog=null;
		if(dialogs.containsKey(id))
			dialog = (SIPDialog)dialogs.remove(id);
		dialog.delete();
	}

	public Request getRequest(Response response)
	{
		String id = getId(response);
		RequestData requestData = (RequestData) outstandingRequests.get(id);

		if (requestData != null)
			return requestData.request;

		return null;
	}

	public Dialog getDialog(Response response)
	{
		String id = getId(response);
		RequestData requestData = (RequestData) outstandingRequests.get(id);

		if (requestData != null && requestData.transaction != null)
			return requestData.transaction.getDialog();

		return null;
	}

	public Request createCancel(Request request) throws SipException
	{
		String id = "INVITE#" + ((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId();

		RequestData requestData = (RequestData) outstandingRequests.get(id);

		if (requestData == null || requestData.transaction == null)
			return null;

		return requestData.transaction.createCancel();
	}

	public void removeCoreService(String serviceId, String appId, String userId)
	{

	}

	// / --- Sip listener methods --- ///
	public void processRequest(RequestEvent requestEvent)
	{
		Request request = requestEvent.getRequest();

		LOGGER.debug("IMSMANAGER: Incoming Request\r\n{"+request+"}");

		if (enableLog)
			sipLog.addRequest(request, true);

		// SipURI sipUri = (SipURI) request.getRequestURI();
		// InetAddress requestTargetAddress = null;
		// try
		// {
		// requestTargetAddress = InetAddress.getByName(sipUri.getHost());
		// } catch (UnknownHostException e2)
		// {
		// LOGGER.error("Error during parsing: " + e2.getMessage(), e2);
		// return;
		// }
		//
		// if (!localAddress.equals(requestTargetAddress))
		// {
		// logger.warn("Received request not intended for this host! Expected {}, Found: {} ",
		// localAddress.getHostAddress(), requestTargetAddress);
		// return;
		// }

		ServerTransaction serverTransaction = requestEvent.getServerTransaction();

		if (!request.getMethod().equals("ACK"))
		{
			if (serverTransaction == null)
			{
				try
				{
					serverTransaction = sipProvider.getNewServerTransaction(request);
				} catch (TransactionAlreadyExistsException e)
				{
					LOGGER.error(e.getMessage(), e);
					return;
				} catch (TransactionUnavailableException e)
				{
					LOGGER.error(e.getMessage(), e);
					return;
				}
			}

			if (request.getMethod().equals(Request.PRACK))
			{
				Response response;
				try
				{
					response = messageFactory.createResponse(200, request);
					serverTransaction.sendResponse(response);

					return;
				} catch (Exception e)
				{
					LOGGER.error(e.getMessage());
				}
			} else
			{
				String key = getTransactionKey(request);
				serverTransactions.put(key, serverTransaction);

				if (request.getMethod().equals(Request.NOTIFY) && "reg".equals(((EventHeader) request.getHeader("Event")).getEventType()))
				{
					registration.processRegNotify(request);
				} else
				{
					if (serverTransaction.getDialog() != null)
					{
						String dialogId = ((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId();
						dialogs.put(dialogId, serverTransaction.getDialog());
					}
					dispatchRequest(request);
				}
			}
		} else
			dispatchRequest(request);
	}

	public void processResponse(ResponseEvent responseEvent)
	{
		Response response = responseEvent.getResponse();

		LOGGER.debug("Incoming Response\r\n {"+response+"}");
		if (enableLog)
			sipLog.addResponse(response, true);

		ClientTransaction cl = responseEvent.getClientTransaction();
		if (cl == null)
		{
			LOGGER.warn("No client transaction set for the given response!");
			return;
		}

		String id = getId(response);

		LOGGER.debug("outstanding request: {"+new Integer(outstandingRequests.size())+"}");

		RequestData requestData = (RequestData) outstandingRequests.get(id);

		if (response.getStatusCode() > 100 && response.getStatusCode() < 200)
		{
			RequireHeader requireHeader = (RequireHeader) response.getHeader("Require");
			if (requireHeader != null && requireHeader.getOptionTag().indexOf("100rel") > -1)
			{
				try
				{
					Request request = cl.getDialog().createPrack(response);
					request.removeHeader("P-Asserted-Identity");
					ClientTransaction transaction = sipProvider.getNewClientTransaction(request);
					cl.getDialog().sendRequest(transaction);
					// sendRequest(requestData.coreService, request);
				} catch (SipException e)
				{
					LOGGER.error(e.getMessage(), e);
				}
			}
		}

		// Service unavailable?
		if (response.getStatusCode() == 500 || response.getStatusCode() == 503)
		{
			// Check if there is a retry header
			RetryAfterHeader retryAfterHeader = (RetryAfterHeader) response.getHeader("Retry-After");
			if (retryAfterHeader != null && retryAfterHeader.getRetryAfter() > 0)
			{
				int retryAfter = retryAfterHeader.getRetryAfter();
				int retransmissionSeconds = (retryAfter * 1000) - (300 * 1000);

				if (retransmissionSeconds > 0)
				{
					// retransmit request after retryAfter * 1000 milliseconds
					if (requestData != null)
					{
						timerTask = new RequestRetransmissionTask(requestData, null);
						timer.schedule(timerTask, retransmissionSeconds);

						LOGGER.info("the following request has be scheduled to be retransmitted in " + retransmissionSeconds + " \n" + requestData.request);
					} else
					{
						timerTask = new RequestRetransmissionTask(null, cl.getRequest());
						timer.schedule(timerTask, retransmissionSeconds);

						LOGGER.info("the following request has be scheduled to be retransmitted in " + retransmissionSeconds + " \n" + cl.getRequest());
					}

				}
			}
		}

		if (responses.containsKey(id))
			responses.put(id, response);

		if (cl.getDialog() != null)
		{
			dialogs.put(((CallIdHeader) response.getHeader(SIPHeaderNames.CALL_ID)).getCallId(), cl.getDialog());
		}

		Request request = cl.getRequest();
		synchronized (request)
		{
			request.notifyAll();
		}

		if (requestData != null && requestData.coreService != null)
		{
			if (response.getStatusCode() < 200)
				requestData.coreService.provisionalResponse(response);
			else
			{
				requestData.coreService.finalResponse(response);
				LOGGER.debug("removing outstanding request: {"+id+"}");
				outstandingRequests.remove(id);
				LOGGER.debug("remove request, left: {"+new Integer(outstandingRequests.size())+"}");
			}
		}
	}

	public void processTimeout(TimeoutEvent timeoutEvent)
	{
		LOGGER.warn("request timeout");
		if (timeoutEvent.isServerTransaction())
		{
			ServerTransaction tx = timeoutEvent.getServerTransaction();
			Request request = tx.getRequest();
			String key = getTransactionKey(request);
			serverTransactions.remove(key);
		} else
		{
			ClientTransaction tx = timeoutEvent.getClientTransaction();
			Request request = tx.getRequest();

			String key = request.getMethod() + "#" + ((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId();
			RequestData data = (RequestData) outstandingRequests.remove(key);           

			responses.put(key, TIMEOUT);
			synchronized (request)
			{
				request.notifyAll();
			}
			if (data != null)
				data.coreService.timeout(request);
		}
	}

	public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent)
	{
		if (!transactionTerminatedEvent.isServerTransaction())
		{
			Request request = transactionTerminatedEvent.getClientTransaction().getRequest();
			String id = ((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId();
			responses.remove(id);
		}
	}

	public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent)
	{
		String key = dialogTerminatedEvent.getDialog().getCallId().getCallId();
		dialogs.remove(key);
	}

	public void processIOException(IOExceptionEvent exceptionEvent)
	{
		// TODO Auto-generated method stub
	}

	// / --- end sip listener --- ///

	/**
	 * Creates a new initial sip request containing the given values
	 * 
	 * @param method
	 *            The method of the request
	 * @param arequestUri
	 *            The target uri for the request
	 * @param from
	 *            The from address
	 * @param to
	 *            The to address
	 * @return A new request which can be send using the
	 *         {@link #sendRequest(CoreServiceImpl, Request)} method
	 * @throws ParseException
	 *             If one of the parameters can't be parsed
	 * @throws InvalidArgumentException
	 *             if invalid arguments are provided
	 */
	public synchronized Request createRequest(String method, String arequestUri, String from, String to) throws ParseException, InvalidArgumentException
	{
		return createRequest(method, arequestUri, from, to, null);
	}

	/**
	 * Creates a new initial sip request containing the given values
	 * 
	 * @param method
	 *            The method of the request
	 * @param arequestUri
	 *            The target uri for the request
	 * @param from
	 *            The from address
	 * @param to
	 *            The to address
	 * @return A new request which can be send using the
	 *         {@link #sendRequest(CoreServiceImpl, Request)} method
	 * @throws ParseException
	 *             If one of the parameters can't be parsed
	 * @throws InvalidArgumentException
	 *             if invalid arguments are provided
	 */
	public synchronized Request createRequest(String method, String arequestUri, String from, String to, String req_callId) throws ParseException, InvalidArgumentException
	{
		LOGGER.debug("Create request: " + method);

		CallIdHeader callid;
		if (req_callId!=null){
			callid = new CallID();
			try {
				callid.setCallId(req_callId);
			} catch (java.text.ParseException ex) {
			}
		}
		else callid = sipProvider.getNewCallId();


		CSeqHeader cSeqHeader = null;

		URI requestURI = (URI) addressFactory.createURI(arequestUri);

		if (method.equals("PUBLISH"))
			cSeqHeader = headerFactory.createCSeqHeader(1L, method);
		else
			cSeqHeader = headerFactory.createCSeqHeader(getNextCSeq(), method);

		if (from == null)
			from = publicUserId[0];

		FromHeader fromHeader = headerFactory.createFromHeader(addressFactory.createAddress(from), String.valueOf(getNextTag()));

		ToHeader toHeader = headerFactory.createToHeader(addressFactory.createAddress(to), null);

		List list = new ArrayList();

		ViaHeader viaHeaderUDP = headerFactory.createViaHeader(localAddressString, udpListeningPoint.getPort(), transport, null);
		list.add(viaHeaderUDP);

		Request request = messageFactory.createRequest(requestURI, method, callid, cSeqHeader, fromHeader, toHeader, list, maxForwardsHeader);

		if (!method.equals(Request.REGISTER))
			this.addServiceRoutes(request);

		return request;
	}

	/**
	 * Creates a new request within the given dialog
	 * 
	 * @param dialog
	 *            The dialog the request should be create within
	 * @param method
	 *            The method of the request
	 * @return the new request
	 * @throws SipException
	 * @throws InvalidArgumentException 
	 * @throws ParseException 
	 */
	public synchronized Request createRequest(Dialog dialog, String method) throws SipException
	{
		LOGGER.debug("Create request dialog: " + method);
		if (dialog == null)
			throw new IllegalArgumentException("dialog MUST NOT be null!");

		if (method == null)
			throw new IllegalArgumentException("method MUST NOT be null!");

		Request request = dialog.createRequest(method);
		request.setHeader((Header) maxForwardsHeader.clone());
		try
		{
			request.setHeader(headerFactory.createCSeqHeader(getNextCSeq(), method));
		} catch (ParseException e)
		{
			LOGGER.error(e.getMessage());
		} catch (InvalidArgumentException e)
		{
			LOGGER.error(e.getMessage());
		}
		return request;
	}

	/**
	 * Creates a response according to the
	 * 
	 * @param request
	 * @param statusCode
	 * @return
	 * @throws ParseException
	 */
	public synchronized Response createResponse(Request request, int statusCode) throws ParseException
	{
		return createResponse(null, request, statusCode);
	}

	public synchronized Response createResponse(CoreServiceImpl coreService, Request request, int statusCode) throws ParseException
	{
		Response response = null;
		if (request.getMethod().equals("INVITE"))
		{
			RequireHeader header = (RequireHeader) request.getHeader("Require");

			if (header != null && header.getOptionTag().indexOf("100rel") > -1 && (statusCode > 100 & statusCode < 200))
			{
				Dialog dialog = (Dialog) dialogs.get(((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId());

				try
				{
					response = dialog.createReliableProvisionalResponse(statusCode);
				} catch (InvalidArgumentException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SipException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else
				response = messageFactory.createResponse(statusCode, request);
		} else
		{
			response = messageFactory.createResponse(statusCode, request);
		}
		setDefaultHeaders(coreService, request, response);
		return response;
	}

	public synchronized Response createResponse(CoreServiceImpl coreService, Request request, int statusCode, ContentTypeHeader ctHeader, byte[] payload) throws ParseException
	{
		Response response = messageFactory.createResponse(statusCode, request, ctHeader, payload);
		setDefaultHeaders(coreService, request, response);
		return response;
	}

	// / Sending
	public synchronized void sendRequest(CoreServiceImpl coreService, Request request) throws SipException
	{
		setDefaultHeaders(coreService, request);
		((SIPRequest) request).setTransaction(null);
		ClientTransaction transaction = sipProvider.getNewClientTransaction(request);

		RequestData requestData = new RequestData(coreService, request, transaction);

		String key = request.getMethod() + "#" + ((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId();

		LOGGER.debug("put outstanding request: {"+key+"}");
		outstandingRequests.put(key, requestData);

		LOGGER.debug("after put outstanding request: {"+new Integer(outstandingRequests.size())+"}");

		Dialog dialog = getDialog(request);

		if (enableLog)
			sipLog.addRequest(request, false);

		if (dialog == null || dialog.getState() != DialogState.CONFIRMED)
		{
			LOGGER.debug("Sending non dialog request: \r\n{"+request+"}");
			transaction.sendRequest();
		} else
		{
			LOGGER.debug("Sending dialog request: \r\n{"+request+"}");
			dialog.sendRequest(transaction);
		}
	}

	public void sendP2PRequest(CoreServiceImpl coreService,	Request request, String recipientAddress, int recipientPort) throws SipException
	{
		ClientTransaction transaction = sipProvider.getNewClientTransaction(request, recipientAddress, recipientPort);

		Dialog dialog = getDialog(request);

		LOGGER.debug("Sending request: \r\n{"+request+"}");
		if (enableLog)
			sipLog.addRequest(request, false);

		if (dialog == null || dialog.getState() != DialogState.CONFIRMED)
		{
			transaction.sendRequest();
		} else
		{
			dialog.sendRequest(transaction);
		}
	}

	// / Sending
	public synchronized void sendRequest(Request request) throws SipException
	{
		((SIPRequest) request).setTransaction(null);
		ClientTransaction transaction = sipProvider.getNewClientTransaction(request);

		Dialog dialog = getDialog(request);

		LOGGER.debug("Sending request: \r\n{"+request+"}");
		if (enableLog)
			sipLog.addRequest(request, false);

		if (dialog == null || dialog.getState() != DialogState.CONFIRMED)
		{
			transaction.sendRequest();
		} else
		{
			dialog.sendRequest(transaction);
		}
	}

	public synchronized Response sendSyncRequest(CoreServiceImpl coreService, Request request) throws SipException
	{

		//new to notify application when a sync request is send.
		notifySyncRequest(new IMSManagerEvent(this));
		setDefaultHeaders(coreService, request);

		LOGGER.debug("Sending request: \r\n{"+request+"}");
		if (enableLog)
			sipLog.addRequest(request, false);

		ClientTransaction transaction = sipProvider.getNewClientTransaction(request);

		transaction.sendRequest();

		String id = request.getMethod() + "#" + ((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId();

		responses.put(id, request);

		synchronized (request)
		{
			try
			{
				request.wait(45000);
			} catch (InterruptedException e)
			{
				LOGGER.error(e.getMessage(), e);
				return null;
			}
		}

		Object o = responses.remove(id);
		if (o instanceof Response)
		{
			//new to notify a client when sync was successful.
			notifyConnected(new IMSManagerEvent(this));
			return (Response) o;
		} else if (TIMEOUT == o)
		{
			//new to notify a client when timeout accoured.
			notifyDisconnected(new IMSManagerEvent(this));
			throw new SipException("Timeout");
		}

		return null;
	}


	public void addListener(IMSManagerConnectionListener listener) {
		listeners.add(IMSManagerConnectionListener.class, listener);
	}

	public void removeListener(IMSManagerConnectionListener listener) {
		listeners.remove(IMSManagerConnectionListener.class, listener);
	}


	protected synchronized void notifyConnected(IMSManagerEvent event) {
		IMSManagerConnectionListener[] listenerArray = (IMSManagerConnectionListener[]) listeners
				.getListeners(IMSManagerConnectionListener.class);

		for (int i = 0; i < listenerArray.length; i++) {
			listenerArray[i].connected(event);
		}
	}


	protected synchronized void notifyDisconnected(IMSManagerEvent event) {
		IMSManagerConnectionListener[] listenerArray = (IMSManagerConnectionListener[]) listeners
				.getListeners(IMSManagerConnectionListener.class);

		for (int i = 0; i < listenerArray.length; i++) {
			listenerArray[i].disconnected(event);
		}
	}


	protected synchronized void notifySyncRequest(IMSManagerEvent event) {
		IMSManagerConnectionListener[] listenerArray = (IMSManagerConnectionListener[]) listeners
				.getListeners(IMSManagerConnectionListener.class);

		for (int i = 0; i < listenerArray.length; i++) {
			listenerArray[i].sendingSyncRequest(event);
		}
	}


	public synchronized void sendResponse(CoreServiceImpl coreService, Request request, Response response) throws SipException, InvalidArgumentException
	{
		LOGGER.debug("sending respone");
		RequireHeader requireHeader = (RequireHeader) request.getHeader("Require");

		if (request.getMethod().equals(Request.INVITE) && response.getStatusCode() > 100 && response.getStatusCode() < 200 && requireHeader != null
				&& "100rel".equals(requireHeader.getOptionTag()))
		{
			Dialog dialog = (Dialog) dialogs.get(((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId());

			LOGGER.debug("Sending reliable response: \r\n{"+response+"}");
			if (enableLog)
				sipLog.addResponse(response, false);

			dialog.sendReliableProvisionalResponse(response);
		} else
		{
			String key = getTransactionKey(request);
			ServerTransaction transaction = (ServerTransaction) serverTransactions.get(key);

			if (transaction == null)
				return;

			if (request.getMethod().equals(Request.INVITE))
				response.addHeader(registration.getContactHeader(coreService));

			LOGGER.debug("done");

			LOGGER.debug("Sending response: \r\n{"+response+"}");
			if (enableLog)
				sipLog.addResponse(response, false);

			transaction.sendResponse(response);

			if (response.getStatusCode() > 199)
				serverTransactions.remove(key);
		}
	}

	public void sendResponse(Request request, Response response) throws SipException, InvalidArgumentException
	{
		String key = getTransactionKey(request);
		ServerTransaction transaction = (ServerTransaction) serverTransactions.get(key);

		if (transaction == null)
			return;

		LOGGER.debug("Sending response: \r\n{"+response+"}");
		if (enableLog)
			sipLog.addResponse(response, false);

		transaction.sendResponse(response);

		serverTransactions.remove(key);
	}

	// / End sending

	private class Match implements Comparable
	{
		private float factor;
		public CoreServiceImpl coreService;

		public Match(CoreServiceImpl coreService, float factor)
		{
			this.factor = factor;
			this.coreService = coreService;
		}

		public int compareTo(Object o)
		{
			return (int) ((((Match) o).factor - factor) * 100);
		}
	}

	/**
	 * Evaluates the given request and route it to the related core services
	 */
	private void dispatchRequest(Request request)
	{
		LOGGER.debug("Dispatching request...");
		AcceptContactHeader acceptContact = (AcceptContactHeader) request.getHeader("Accept-Contact");

		if (acceptContact == null)
		{
			LOGGER.debug("No Accept-Contact header found");
			// No accept contact here - route to default			
			for (int i = 0; i < coreServiceList.length; i++)
			{
				FeatureTag[] tags = registration.getFeatureTags(coreServiceList[i]);								
				if (tags == null || tags.length == 0)
				{
					coreServiceList[i].request(request);
					return;
				}
				else if(coreServiceList[i].isDefaultCoreService())
				{
					coreServiceList[i].request(request);
				}
			}
		} else
		{
			LOGGER.debug("Using Header: {"+acceptContact+"}");
			FeatureTag[] temp = FeatureTag.parse(acceptContact);

			FeatureTagSet acceptSet = new FeatureTagSet(temp);
			Match[] matches = new Match[coreServiceList.length];
			for (int i = 0; i < coreServiceList.length; i++)
			{
				matches[i] = new Match(coreServiceList[i], acceptSet.match(coreServiceList[i].getFeatureSet()));
			}

			Arrays.sort(matches);

			for (int i = 0; i < matches.length; i++)
			{
				System.out.println(matches[i].factor + ": " + matches[i].coreService.getId());
			}

			matches[0].coreService.request(request);
		}
	}
	

	public void start(Vertx instance) throws ParseException, InvalidArgumentException, PeerUnavailableException, ImsException
	{
		this.vertx = instance;
		
		initiateSipStack();
		initiateCachedHeaders();		
		this.authenticator = new Authenticator(this.headerFactory, this.addressFactory, domain, privateUserId, secretKey, isimAuth);

		registration = new Registration(this, domain, new InetSocketAddress(localAddress, localPort), publicUserId[0], expireTime, authenticator,
				sendRegistration);

		registration.setDisplayName(displayName);
		registration.setGRUU(gruu);
		registration.setSubscribeToReg(subscribeToReg);

		String[] appIds = Configuration.getConfiguration().getLocalAppIds();
		for (int i = 0; i < appIds.length; i++)
		{
			String[][] registry = Configuration.getConfiguration().getRegistry(appIds[i]);
			registration.add(registry);
		}

		isRunning = true;
		unregisterAll = false;
	}

	public void stop()
	{
		try
		{
			CoreServiceImpl[] services = null;
			synchronized (coreServices)
			{
				services = (CoreServiceImpl[]) coreServices.values().toArray(new CoreServiceImpl[coreServices.size()]);
			}

			for (int i = services.length - 1; i >= 0; i--)
			{
				services[i].close();
			}

			destroySipStack();
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		} finally
		{
			isRunning = false;
		}

		//clear list of providers
		sipProviders.clear();
	}

	//version 0.9.25 clearing all coreServices 
	public void abort()
	{
		coreServices.clear();
		this.coreServiceList = (CoreServiceImpl[]) coreServices.values().toArray(new CoreServiceImpl[coreServices.size()]);
		ConnectionState.getConnectionState().setPublicIdentities(new String[0]);
		ConnectionState.getConnectionState().setConnectionStatus(coreServices.size() > 0);

		destroySipStack();
	}

	public void stopWithUnregisterAll()
	{
		this.unregisterAll = true;

		try
		{
			CoreServiceImpl[] services = null;
			synchronized (coreServices)
			{
				services = (CoreServiceImpl[]) coreServices.values().toArray(new CoreServiceImpl[coreServices.size()]);
			}

			for (int i = services.length - 1; i >= 0; i--)
			{
				services[i].close();
			}			
			registration.unregisterAll();
			registeredCoreServices--;
			destroySipStack();
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		} finally
		{
			isRunning = false;
		}
	}

	public boolean isRunning()
	{
		return isRunning;
	}

	public boolean isSessionTimerEnabled()
	{
		return enableSessionTimer;
	}

	public void configurationAdded(String[][] registry)
	{
		if (!isRunning)
			return;

		try
		{

			registration.add(registry);
			if (registeredCoreServices > 0)
				registration.reregister();
		} catch (Exception e1)
		{
			LOGGER.error(e1.getMessage(), e1);
			return;
		}
	};

	public void configurationRemoved(String[][] registry)
	{
		if (!isRunning)
			return;

		try
		{
			registration.remove(registry);

			if (registeredCoreServices > 0)
				registration.reregister();
		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void initiateSipStack() throws PeerUnavailableException
	{
		String pathName = "gov.nist";

		SipFactory sipFactory = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName(pathName);

		headerFactory = sipFactory.createHeaderFactory();
		addressFactory = sipFactory.createAddressFactory();
		messageFactory = sipFactory.createMessageFactory();

		Properties properties = new Properties();

		properties.setProperty("javax.sip.IP_ADDRESS", localAddressString);
		properties.setProperty("javax.sip.OUTBOUND_PROXY", remoteIP + ":" + remotePort + "/" + transport);
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "modules/lib_sip/log/sipstack.log");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");
		properties.setProperty("javax.sip.ROUTER_PATH", "de.fhg.fokus.ims.core.IMSRouter");
		properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
		properties.setProperty("javax.sip.STACK_NAME", SIP_STACK_NAME);
		
        properties.setProperty("org.openxsp.stack.HOST_IP", localAddressString);
        properties.setProperty("org.openxsp.stack.PORT", Integer.toString(remotePort));
        properties.setProperty("org.openxsp.stack.TRANSPORT", transport);
        properties.setProperty("org.openxsp.stack.DOMAIN", domain);
        properties.setProperty("org.openxsp.stack.IMSPCSCF", Integer.toString(remotePort));
        properties.setProperty("org.openxsp.stack.IMSSCSCF", "6060");
        properties.setProperty("org.openxsp.stack.IMSICSCF", "5060");
        properties.setProperty("org.openxsp.stack.IMSIP", remoteIP);
        properties.setProperty("org.openxsp.stack.IMSDOMAIN", domain);
        properties.setProperty("org.openxsp.stack.use.server.socket", "true");

        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");
        properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "65536");
        properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "2");
        properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
        properties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "5000");
        properties.setProperty("gov.nist.javax.sip.STRIP_ROUTE_HEADER", "false");
        properties.setProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "true");

		properties.setProperty("org.openxsp.stack.HOST_IP", localAddressString);
        properties.setProperty("org.openxsp.stack.PORT", Integer.toString(remotePort));
        properties.setProperty("org.openxsp.stack.TRANSPORT", transport);
        properties.setProperty("org.openxsp.stack.DOMAIN", domain);
        properties.setProperty("org.openxsp.stack.IMSPCSCF", Integer.toString(remotePort));
        properties.setProperty("org.openxsp.stack.IMSSCSCF", "6060");
        properties.setProperty("org.openxsp.stack.IMSICSCF", "5060");
        properties.setProperty("org.openxsp.stack.IMSIP", remoteIP);
        properties.setProperty("org.openxsp.stack.IMSDOMAIN", domain);
        properties.setProperty("org.openxsp.stack.use.server.socket", "true");
        
		if (pathName.equals("gov.nist"))
		{
			properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");
			properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "65536");
			properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "2");
			properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
			properties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "5000");
			properties.setProperty("gov.nist.javax.sip.STRIP_ROUTE_HEADER", "false");
			properties.setProperty("gov.nist.javax.sip.LOG_STACK_TRACE_ON_MESSAGE_SEND", "true");
		}

		LOGGER.debug("Creating sip stack");
		this.sipStack = sipFactory.createSipStack(properties, vertx);

		int count = 0;
		do
		{
			try
			{
				LOGGER.info("SIP Stack listening on :"+localAddressString+"-" + localPort + " for udp SIP traffic");
				udpListeningPoint = sipStack.createListeningPoint(localAddressString, localPort, "udp");
//				tcpListeningPoint = sipStack.createListeningPoint(localAddressString, localPort, "tcp");
				LOGGER.info("SIP Stack listening on : " + localPort + " for udp/tcp SIP traffic");
				break;
			} catch (TransportNotSupportedException e)
			{
				LOGGER.info("transport not supported exception");
				e.printStackTrace();
				return;
			} catch (InvalidArgumentException e)
			{
				count++;
				localPort += 2;
				LOGGER.info("UDP or TCP Port " + localPort + " is in use... will try another one");
			}
		} while (count < 10);

		try
		{
			sipProvider = sipStack.createSipProvider(udpListeningPoint);
//			sipProvider.addListeningPoint(tcpListeningPoint);
		} catch (ObjectInUseException e)
		{
			LOGGER.info("CoreServiceImpl: initSipStackUser() - object in use exception");
			LOGGER.error(e.getMessage(), e);
		} 
//		catch (TransportAlreadySupportedException e)
//		{
//			LOGGER.error(e.getMessage(), e);
//		}

		try
		{
			sipProvider.addSipListener(this);
		} catch (TooManyListenersException e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void destroySipStack()
	{
		supported100Rel = null;
		supportedTimer = null;
		sessionExpiresHeader = null;
		SipFactory.getInstance().destroySipStack(SIP_STACK_NAME);
	}

	private void initiateCachedHeaders() throws ParseException, InvalidArgumentException
	{
		String temp = localAddress.getHostAddress();
		if (localAddress instanceof Inet6Address)
			temp = "[" + temp + "]";

		viaHeader = new ArrayList();

		String transport = udpListeningPoint.getTransport();
		String ip = udpListeningPoint.getIPAddress();
		int port = udpListeningPoint.getPort();
		ViaHeader viaHeaderUDP = headerFactory.createViaHeader(ip, port, transport, null);
		viaHeader.add(viaHeaderUDP);

		maxForwardsHeader = headerFactory.createMaxForwardsHeader(20);

		allowHeaders = new ArrayList();
		allowHeaders.add(headerFactory.createAllowHeader("INVITE"));
		allowHeaders.add(headerFactory.createAllowHeader("ACK"));
		allowHeaders.add(headerFactory.createAllowHeader("CANCEL"));
		allowHeaders.add(headerFactory.createAllowHeader("BYE"));
		allowHeaders.add(headerFactory.createAllowHeader("MESSAGE"));
		allowHeaders.add(headerFactory.createAllowHeader("NOTIFY"));
		allowHeaders.add(headerFactory.createAllowHeader("REFER"));

		List products = new ArrayList();

//		Properties properties = new Properties();
//		try
//		{
//			properties.load(getClass().getResourceAsStream("/imscore.properties"));
//		} catch (IOException e)
//		{
//			LOGGER.error(e.getMessage(), e);
//		}

		String name = "openXSP SipStack Version: 1.0";

		products.add(name);
		userAgentHeader = headerFactory.createUserAgentHeader(products);

		if(prackSupport || enableSessionTimer)
		{
			if(prackSupport)
				supported100Rel = headerFactory.createSupportedHeader("100rel");
			if(enableSessionTimer)
			{
				supportedTimer = headerFactory.createSupportedHeader("timer");
				sessionExpiresHeader = (ExtensionHeader) headerFactory.createHeader(SIPHeaderNames.SESSION_EXPIRES, String.valueOf(sessionExpireTimer));
			}			
		}

	}

	private synchronized long getNextCSeq()
	{
		return cseq++;
	}

	private synchronized int getNextTag()
	{
		return tag++;
	}

	private void unregisterCoreService(CoreServiceImpl coreService) throws ParseException, InvalidArgumentException, SipException, RegistrationException
	{
		registeredCoreServices--;

		if (registeredCoreServices == 0)
		{
			try
			{
				registration.unregister();
			} finally
			{

			}
		}
	}

	private void addServiceRoutes(Request request)
	{
		List serviceRoutes = registration.getServiceRoutes();
		for (int i = 0; i < serviceRoutes.size(); i++)
		{
			//System.err.println("************************************* addding service route to request : "+((RouteHeader) serviceRoutes.get(i)));
			request.addHeader(((RouteHeader) serviceRoutes.get(i)));
		}
	}

	private void setDefaultHeaders(CoreServiceImpl coreService, Request request)
	{
		if (request.getMethod().equals(Request.REGISTER) || request.getMethod().equals(Request.INVITE) || request.getMethod().equals(Request.SUBSCRIBE)
				|| request.getMethod().equals(Request.REFER) || request.getMethod().equals(Request.OPTIONS))
		{
			if (request.getHeader("Contact") == null)
				request.setHeader(registration.getContactHeader(coreService));
			if(request.getMethod().equals(Request.INVITE))
			{
				if(supported100Rel !=null)
					request.addHeader(supported100Rel);
				if(supportedTimer !=null)
					request.addHeader(supportedTimer);
				if(sessionExpiresHeader !=null)
					request.addHeader(sessionExpiresHeader);				
			}
		}

		request.addHeader(userAgentHeader);		

		request.removeHeader("P-Asserted-Identity");

		FeatureTag[] featureTags = registration.getFeatureTags(coreService);

		if (featureTags != null && featureTags.length > 0)
		{
			try
			{
				AcceptContact ach = new AcceptContact();
				for (int i = 0; i < featureTags.length; i++)
				{
					ach.setParameter(featureTags[i].getName(), featureTags[i].getValue());
				}
				request.addHeader(ach);
			} catch (Exception e)
			{
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	private void setDefaultHeaders(CoreServiceImpl coreService, Request request, Response response) throws ParseException
	{
		String callId = ((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId();

		Dialog dialog = (Dialog) dialogs.get(callId);

		ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
		if (toHeader.getTag() == null || toHeader.getTag().equals(""))
		{
			if (dialog == null || dialog.getLocalTag() == null)
				toHeader.setTag(String.valueOf(getNextTag()));
			else
				toHeader.setTag(dialog.getLocalTag());
		}

		//For OPTIONS response, include the contact header and the accept contact header
		if (request.getMethod().equals(Request.OPTIONS) && coreService !=null)
		{
			if (response.getHeader("Contact") == null)
				response.setHeader(registration.getContactHeader(coreService));

			FeatureTag[] featureTags = registration.getFeatureTags(coreService);

			if (featureTags != null && featureTags.length > 0)
			{
				try
				{
					AcceptContact ach = new AcceptContact();
					for (int i = 0; i < featureTags.length; i++)
					{
						ach.setParameter(featureTags[i].getName(), featureTags[i].getValue());
					}
					response.addHeader(ach);
				} catch (Exception e)
				{
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}

	public void sendAck(CoreServiceImpl coreService, Request request) throws InvalidArgumentException, SipException
	{
		String key = ((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId();
		Dialog dialog = (Dialog) dialogs.get(key);

		if (dialog.getState() == DialogState.CONFIRMED)
		{
			Request ack = dialog.createAck(((CSeqHeader) request.getHeader("CSeq")).getSeqNumber());// dialog.getLocalSeqNumber());
			setDefaultHeaders(coreService, ack);
			LOGGER.debug("Sending request: \r\n{"+ack+"}");
			dialog.sendAck(ack); 

		}
	}

	private class RequestData
	{
		public CoreServiceImpl coreService;
		public Request request;
		public ClientTransaction transaction;

		public RequestData(CoreServiceImpl coreService, Request request, ClientTransaction transaction)
		{
			this.coreService = coreService;
			this.request = request;
			this.transaction = transaction;
		}
	}

	private String getTransactionKey(Request request)
	{
		return request.getMethod() + "#" + ((CSeqHeader) request.getHeader("CSeq")).getSeqNumber() + "#"
				+ ((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId();
	}

	private static String getId(Response response)
	{
		return ((CSeqHeader) response.getHeader("CSeq")).getMethod() + "#" + ((CallIdHeader) response.getHeader(SIPHeaderNames.CALL_ID)).getCallId();
	}



	public void localEndpointChanged(NetworkService networkService) 
	{

	}

	public SipProvider getSipProvider(){
		return sipProvider;
	}




	public DigestAKAResponse getRespAKA()
	{
		return authenticator.getRespAKA();
	}



}