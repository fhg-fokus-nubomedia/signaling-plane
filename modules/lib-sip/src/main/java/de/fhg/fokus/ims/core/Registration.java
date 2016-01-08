package de.fhg.fokus.ims.core;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.Contact;

import java.io.ByteArrayInputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ExtensionHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.RouteHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.swing.event.EventListenerList;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import de.fhg.fokus.ims.core.auth.AuthenticationException;
import de.fhg.fokus.ims.core.auth.Authenticator;
import de.fhg.fokus.ims.core.event.reg.Contact.UnknownParam;
import de.fhg.fokus.ims.core.event.reg.Reginfo;
import de.fhg.fokus.ims.core.event.reg.Reginfo.State;

/**
 * Holds the state of a registration.
 * 
 * @author Andreas Bachmann (andreas.bachmann@fokus.fraunhofer.de)
 */
public class Registration
{	
	private static Logger LOGGER = LoggerFactory.getLogger(Registration.class);
	
	/**
	 * holds all feature tags registered by the services. Key: Service ID
	 */
	private HashMap featureTags = new HashMap();

	/**
	 * Holds the list of feature tags, merged by all core service instances
	 */
	private ArrayList mergedFeatureTags = new ArrayList();

	/**
	 * holds all headers registered by the core service instances. Key: Service
	 * ID
	 */
	private HashMap regHeaders = new HashMap();

	/**
	 * Holds the manager the associated with this registration
	 */
	private IMSManager manager;

	/**
	 * The public user id associated with this registration
	 */
	private String publicUserId;

	/**
	 * The IMS domain associated with this registration
	 */
	private String domain;

	private int expire;

	private Authenticator authenticator;

	private ArrayList serviceRoutes = new ArrayList();

	private Timer registrationTimer;

	private AddressFactory addressFactory;

	private HeaderFactory headerFactory;

	private Address contactAddress;

	private Contact contactHeader;

	private HashMap contactHeaders = new HashMap();

	private ContactHeader defaultContactHeader;

	private boolean sendRegistration = true;

	private String gruu = null;

	private Reginfo reginfo;

	private String displayName = null;

	private boolean subscribeToReg = false;

	private String callId;
	
	private EventListenerList listeners = new EventListenerList();

	public Registration(IMSManager manager, String domain, InetSocketAddress contactAddress, String publicUserId, int expire, Authenticator authenticator,
			boolean sendRegistration) throws ParseException
			{
		this.manager = manager;
		this.domain = domain;
		this.publicUserId = publicUserId;
		this.expire = expire;
		this.authenticator = authenticator;
		headerFactory = manager.getHeaderFactory();
		addressFactory = manager.getAddressFactory();
		
		SipURI publicIdentityURI = (SipURI) addressFactory.createURI(publicUserId);

		SipURI contactUri = (SipURI) addressFactory.createURI("sip:" + publicIdentityURI.getUser() + "@" + getIPAddressString(contactAddress));

		Iterator iter = publicIdentityURI.getParameterNames();

		for (Iterator iterator = iter; iterator.hasNext();)
		{
			String name = (String) iterator.next();
			contactUri.setParameter(name, publicIdentityURI.getParameter(name));
		}

		this.contactAddress = addressFactory.createAddress(contactUri);
		this.contactHeader = (Contact) headerFactory.createContactHeader(this.contactAddress);
		this.defaultContactHeader = (Contact) headerFactory.createContactHeader(this.contactAddress);
		this.sendRegistration = sendRegistration;
			}

	public String getDisplayName()
	{
		return displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public List getServiceRoutes()
	{
		return serviceRoutes;
	}

	public void setGRUU(String gruu)
	{
		this.gruu = gruu;
	}

	public String getGRUU()
	{
		return gruu;
	}

	public boolean isSubscribeToReg()
	{
		return subscribeToReg;
	}

	public void setSubscribeToReg(boolean subscribeToReg)
	{
		this.subscribeToReg = subscribeToReg;
	}

	public ContactHeader getContactHeader()
	{
		return contactHeader;
	}

	public ContactHeader getContactHeader(CoreServiceImpl coreService)
	{
		if (coreService == null)
			return contactHeader;

		System.err.println("getting contact header for sore service with id of"+ coreService.getId());
		ContactHeader result = (ContactHeader) contactHeaders.get(coreService.getId());
		if (result == null)
			return defaultContactHeader;

		return result;
	}

	public FeatureTag[] getFeatureTags(CoreServiceImpl coreService)
	{
		if (coreService == null)
			return null;

		List list = (List) featureTags.get(coreService.getId());
	
		if(list == null || list.size() == 0)
			return null;
		

		FeatureTag[] arrayList = new FeatureTag[list.size()];
		int i = 0;
		for (Iterator iterator = list.iterator(); iterator.hasNext();) 
		{
			FeatureTag[] ft = (FeatureTag[]) iterator.next();
			arrayList[i] = ft[0];
			i++;
		}				
		return arrayList;
	}

	public void add(String[][] properties) throws ParseException
	{
		for (int i = 0; i < properties.length; i++)
		{
			if ("Reg".equals(properties[i][0]))
			{
				String serviceId = properties[i][1];
				ArrayList list = new ArrayList();
				regHeaders.put(serviceId, list);
				for (int j = 2; j < properties[i].length; j++)
				{
					list.addAll(headerFactory.createHeaders(properties[i][j]));
				}
			} else if ("CoreService".equals(properties[i][0]))
			{
				String serviceId = properties[i][1];
				String features = properties[i].length == 5 ? properties[i][4] : null;
				if (features == null || features.length() == 0)
					continue;

				FeatureTag[] featureTags = FeatureTag.parse(features);
				if ( ! this.featureTags.containsKey( serviceId ) ) {
					List list = new ArrayList( );
					list.add( featureTags);
					this.featureTags.put( serviceId, list);
				}
				else {
					List list = (List) this.featureTags.get(serviceId);
					list.add( featureTags );
				}
				//this.featureTags.put(serviceId, featureTags);

				Contact ch = null;
				if(!contactHeaders.containsKey(serviceId))
				{
					ch = (Contact) headerFactory.createContactHeader(contactAddress);
				}
				else
					ch = (Contact)contactHeaders.get(serviceId);
			for (int j = 0; j < featureTags.length; j++)
				{
					//System.err.println(featureTags[j].getName() + ": " + featureTags[j].getValue()+" : " + featureTags[j].getType());
					if (featureTags[j].getType() == FeatureTag.QUOTED_INSENSITIV || featureTags[j].getType() == FeatureTag.QUOTED_SENSITIV)
						ch.setQuotedParameter(featureTags[j].getName(), featureTags[j].getValue());
					else
						ch.setParameter(featureTags[j].getName(), featureTags[j].getValue());
				}
				//System.err.println("adding contact header serviceId="+ serviceId+ " - ch="+ch);	
				contactHeaders.put(serviceId, ch);
			}
		}
	}

	public void remove(String[][] properties)
	{

	}


	public void register() throws SipException, ParseException, InvalidArgumentException, RegistrationException
	{
		int regCounter = 0;
		if (!sendRegistration) 
			return;

		String tempUserId = publicUserId;

		if (displayName != null)
			tempUserId = "\"" + displayName + "\" <" + publicUserId + ">";

		Request request = manager.createRequest(Request.REGISTER, "sip:" + domain, tempUserId, tempUserId);
		request.setHeader(headerFactory.createExpiresHeader(expire));
		request.setHeader(authenticator.getEmptyAuthenticationHeader());

		setRegisterHeader(request);

		updateContactHeader();
		
		LOGGER.info("sending Registration \n"+ request);
		
		Response response = manager.sendSyncRequest(null, request);


		if ((response.getStatusCode() != 200) && (response.getStatusCode() != 401))
		{
			throw new SipException("Registration failed: " + response.getStatusCode() + " - " + response.getReasonPhrase());
		}

		/* In case of getting multiple 401, probably the SQN is not synchronized 
		 * in that case after RFC3310 the server will send again an registration 
		 * challenge to the client. */
		while (response.getStatusCode() == 401 && regCounter < 1)
		{
			regCounter++;
			AuthorizationHeader authHeader = null;
			try
			{
				authHeader = authenticator.authorize(request, response);
			} catch (AuthenticationException e)
			{
				LOGGER.error(e.getMessage(), e);
				throw new RegistrationException(e.getMessage(), e);
			}

			request = manager.createRequest(Request.REGISTER, "sip:" + domain, tempUserId, tempUserId);
			request.setHeader(authHeader);
			request.setHeader(headerFactory.createExpiresHeader(expire));

			setRegisterHeader(request);
			updateContactHeader();            
			
			response = manager.sendSyncRequest(null, request);
		} 

		if (response.getStatusCode() != 200) {

			throw new SipException("Registration failed: "
					+ response.getStatusCode() + " - "
					+ response.getReasonPhrase());
		
		} 

		serviceRoutes.clear();
		RouteHeader rh;

		ListIterator serviceRouteList = response.getHeaders("Service-Route");
		if (serviceRouteList != null)
		{
			while (serviceRouteList.hasNext())
			{
				ExtensionHeader h = (ExtensionHeader) serviceRouteList.next();
				try
				{
					rh = headerFactory.createRouteHeader(addressFactory.createAddress(h.getValue()));
					serviceRoutes.add(serviceRoutes.size(), rh);
				} catch (ParseException e)
				{
					e.printStackTrace();
				}
			}
		}

		response.getHeader("Service-Route");

		if (registrationTimer == null)
			registrationTimer = new Timer(true);

		registrationTimer.schedule(new ReregisterTask(), (expire / 2) * 1000);

		subscribeToReg();
	}

	/**
	public void register() throws SipException, ParseException, InvalidArgumentException, RegistrationException
	{

		if (!sendRegistration) 
			return;

		String tempUserId = publicUserId;

		if (displayName != null)
			tempUserId = "\"" + displayName + "\" <" + publicUserId + ">";

		Request request = manager.createRequest(Request.REGISTER, "sip:" + domain, tempUserId, tempUserId);
		request.setHeader(headerFactory.createExpiresHeader(expire));
		request.setHeader(authenticator.getEmptyAuthenticationHeader());

		setRegisterHeader(request);

		updateContactHeader();

		Response response = manager.sendSyncRequest(null, request);

		int status = response.getStatusCode();

		if (status == 401)
		{
			AuthorizationHeader authHeader = null;
			try
			{
				authHeader = authenticator.authorize(request, response);
			} catch (AuthenticationException e)
			{
				LOGGER.error(e.getMessage(), e);
				throw new RegistrationException(e.getMessage(), e);
			}

			request = manager.createRequest(Request.REGISTER, "sip:" + domain, tempUserId, tempUserId);
			request.setHeader(authHeader);
			request.setHeader(headerFactory.createExpiresHeader(expire));

			setRegisterHeader(request);
			updateContactHeader();

			response = manager.sendSyncRequest(null, request);

			if (response.getStatusCode() != 200)
			{
				throw new SipException("Authentication failed: " + response.getStatusCode() + " - " + response.getReasonPhrase());
			}
		} else if (status != 200)
		{
			throw new SipException("Registration failed: " + response.getStatusCode() + " - " + response.getReasonPhrase());
		}

		serviceRoutes.clear();
		RouteHeader rh;

		ListIterator serviceRouteList = response.getHeaders("Service-Route");
		if (serviceRouteList != null)
		{
			while (serviceRouteList.hasNext())
			{
				ExtensionHeader h = (ExtensionHeader) serviceRouteList.next();
				try
				{
					rh = headerFactory.createRouteHeader(addressFactory.createAddress(h.getValue()));
					System.err.println("************************************* addding service route : " + rh);
					serviceRoutes.add(serviceRoutes.size(), rh);
				} catch (ParseException e)
				{
					e.printStackTrace();
				}
			}
		}

		response.getHeader("Service-Route");

		if (registrationTimer == null)
			registrationTimer = new Timer(true);

		registrationTimer.schedule(new ReregisterTask(), (expire / 2) * 1000);

		subscribeToReg();
	}
	 **/
	public void reregister() throws SipException, ParseException, InvalidArgumentException, RegistrationException
	{
		updateContactHeader();

		Request request = manager.createRequest(Request.REGISTER, "sip:" + domain, publicUserId, publicUserId);
		request.setHeader(headerFactory.createExpiresHeader(expire));
		request.setHeader(authenticator.getEmptyAuthenticationHeader());
		((CallIdHeader) request.getHeader("Call-ID")).setCallId(this.callId);
		setRegisterHeader(request);
		
		Response response = manager.sendSyncRequest(null, request);

		int status = response.getStatusCode();

		if (status == 401)
		{
			AuthorizationHeader authHeader = null;
			try
			{
				authHeader = authenticator.authorize(request, response);
			} catch (AuthenticationException e)
			{
				LOGGER.error(e.getMessage(), e);
				throw new RegistrationException(e.getMessage(), e);
			}

			request = manager.createRequest(Request.REGISTER, "sip:" + domain, publicUserId, publicUserId);
			request.setHeader(headerFactory.createExpiresHeader(expire));
			((CallIdHeader) request.getHeader("Call-ID")).setCallId(this.callId);
			setRegisterHeader(request);
			updateContactHeader();

			request.setHeader(authHeader);
						
			response = manager.sendSyncRequest(null, request);

			if (response.getStatusCode() != 200) {				
				throw new SipException("Authentication failed: " + response.getStatusCode() + " - " + response.getReasonPhrase());
			}
		} else if (status != 200) {	          
	          throw new SipException("Registration failed: " + response.getStatusCode() + " - " + response.getReasonPhrase());
		} 
	}

	public void unregister() throws ParseException, InvalidArgumentException,
			SipException, RegistrationException {
		try {
			int regCounter = 0; // counter for repeatedly getting and
								// registration challange RFC3310 Chap 3.2
			if (!sendRegistration)
				return;

			// unregister everything
			registrationTimer.cancel();
			registrationTimer = null;
			Request request = manager.createRequest(Request.REGISTER, "sip:" + domain, publicUserId, publicUserId);
			request.setHeader(headerFactory.createExpiresHeader(0));
			request.setHeader(authenticator.getEmptyAuthenticationHeader());
			setRegisterHeader(request);

			Response response = manager.sendSyncRequest(null, request);

			/*
			 * In case of getting multiple 401, probably the SQN is not
			 * synchronized in that case after RFC3310 the server will send
			 * again an registration challenge to the client.
			 */
			while ((response.getStatusCode() == 401) && (regCounter < 10)) {
				regCounter++;

				AuthorizationHeader authHeader = null;
				try {
					authHeader = authenticator.authorize(request, response);
				} catch (AuthenticationException e) {
					LOGGER.error(e.getMessage(), e);
					throw new RegistrationException(e.getMessage(), e);
				}

				request = manager.createRequest(Request.REGISTER, "sip:" + domain, publicUserId, publicUserId);
				request.setHeader(authHeader);
				request.setHeader(headerFactory.createExpiresHeader(0));
				setRegisterHeader(request);

				response = manager.sendSyncRequest(null, request);
			}

			if (response.getStatusCode() != 200)
				throw new SipException("Authentication failed: "
						+ response.getStatusCode() + " - "
						+ response.getReasonPhrase());
		} finally {
			
		}
	}

	public void unregisterAll() throws ParseException,
			InvalidArgumentException, SipException, RegistrationException {

		try {

			if (registrationTimer != null) {
				registrationTimer.cancel();
				registrationTimer = null;
			}

			if (!sendRegistration)
				return;

			Request request = manager.createRequest(Request.REGISTER, "sip:" + domain, publicUserId, publicUserId);
			request.setHeader(headerFactory.createExpiresHeader(0));
			request.setHeader(headerFactory.createContactHeader());
			setRegisterHeader(request);

			Response response = manager.sendSyncRequest(null, request);

			if (response.getStatusCode() == 401) {
				AuthorizationHeader authHeader;
				try {
					authHeader = authenticator.authorize(request, response);
				} catch (AuthenticationException e) {
					LOGGER.error(e.getMessage(), e);
					throw new RegistrationException(e.getMessage(), e);
				}

				request = manager.createRequest(Request.REGISTER, "sip:" + domain, publicUserId, publicUserId);
				request.setHeader(authHeader);
				request.setHeader(headerFactory.createExpiresHeader(0));
				request.setHeader(headerFactory.createContactHeader());
				setRegisterHeader(request);

				response = manager.sendSyncRequest(null, request);

				if (response.getStatusCode() != 200)
					throw new SipException("Authentication failed: "
							+ response.getStatusCode() + " - "
							+ response.getReasonPhrase());
			}

		} finally {
			
		}
	}
	

	/**
	 * 
	 */
	public void processRegNotify(Request request)
	{
		try
		{

			Response response = manager.createResponse(request, 200);
			manager.sendResponse(request, response);

			/*XmlPullParser reader = new KXmlParser();
			reader.setInput(new ByteArrayInputStream(request.getRawContent()), "utf-8");
			reader.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
			reader.nextTag();
			Reginfo temp = new Reginfo();
			temp.deserialize(reader);

			if (reginfo == null || temp.getState() == State.FULL)
			{
				reginfo = temp;
			} else
			{
				for (int i = 0; i < temp.getRegistration().size(); i++)
				{
					de.fhg.fokus.ims.core.event.reg.Registration regNew = (de.fhg.fokus.ims.core.event.reg.Registration) temp.getRegistration().get(i);
					boolean replaced = false;
					for (int j = 0; j < reginfo.getRegistration().size(); j++)
					{
						de.fhg.fokus.ims.core.event.reg.Registration regOld = (de.fhg.fokus.ims.core.event.reg.Registration) reginfo.getRegistration().get(j);
						if (regNew.getId().equals(regOld.getId()))
						{
							reginfo.getRegistration().set(j, regNew); // Replace
							// old
							// info
							// with
							// new
							// one
							replaced = false;
							break;
						}
					}

					if (!replaced)
						reginfo.getRegistration().add(regNew);
				}
			}

			unregisterOrphans();*/

		} catch (Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	};

	private void unregisterOrphans() throws ParseException, InvalidArgumentException
	{
		Request request = manager.createRequest(Request.REGISTER, "sip:" + domain, publicUserId, publicUserId);
		request.setHeader(headerFactory.createExpiresHeader(0));
		List tempContacts = new ArrayList();
		for (Iterator iterator = reginfo.getRegistration().iterator(); iterator.hasNext();)
		{
			de.fhg.fokus.ims.core.event.reg.Registration registration = (de.fhg.fokus.ims.core.event.reg.Registration) iterator.next();

			for (Iterator contacts = registration.getContact().iterator(); contacts.hasNext();)
			{
				de.fhg.fokus.ims.core.event.reg.Contact contact = (de.fhg.fokus.ims.core.event.reg.Contact) contacts.next();

				for (Iterator unknownParams = contact.getUnknownParam().iterator(); unknownParams.hasNext();)
				{
					UnknownParam param = (UnknownParam) unknownParams.next();
					if (param.getName().equals("+sip.instance") && param.getValue().equals(this.gruu))
					{
						Address address = addressFactory.createAddress(contact.getUri());
						if (!address.getURI().equals(this.contactAddress.getURI()))
						{
							ContactHeader header = headerFactory.createContactHeader(addressFactory.createAddress(contact.getUri()));
							request.addHeader(header);
							tempContacts.add(header);
						}
					}
				}
			}
		}

		if (tempContacts.size() > 0)
		{
			try
			{
				Response response = manager.sendSyncRequest(null, request);
				if (response.getStatusCode() == 401)
				{
					AuthorizationHeader authHeader;
					try
					{
						authHeader = authenticator.authorize(request, response);
					} catch (AuthenticationException e)
					{
						LOGGER.error(e.getMessage(), e);
						return;
					}

					request = manager.createRequest(Request.REGISTER, "sip:" + domain, publicUserId, publicUserId);
					request.setHeader(authHeader);
					request.setHeader(headerFactory.createExpiresHeader(0));

					for (Iterator iterator = tempContacts.iterator(); iterator.hasNext();)
					{
						ContactHeader contactHeader = (ContactHeader) iterator.next();
						request.addHeader(contactHeader);
					}

					response = manager.sendSyncRequest(null, request);

					if (response.getStatusCode() != 200)
						throw new SipException("Authentication failed: " + response.getStatusCode() + " - " + response.getReasonPhrase());
				}
			} catch (SipException e)
			{
				LOGGER.error(e.getMessage(), e);
			}
		}
	}

	private void setRegisterHeader(Request request) throws ParseException
	{
		if (this.callId == null)
			this.callId = ((CallIdHeader) request.getHeader("Call-ID")).getCallId();
		else
			((CallIdHeader) request.getHeader("Call-ID")).setCallId(this.callId);

		for (Iterator iterator = regHeaders.values().iterator(); iterator.hasNext();)
		{
			ArrayList list = (ArrayList) iterator.next();
			if (list == null || list.size() == 0)
				continue;

			for (int j = 0; j < list.size(); j++)
			{
				request.addHeader((Header) list.get(j));
			}
		}
	}

	private void subscribeToReg()
	{
		if (!subscribeToReg)
			return;

		try
		{
			Request request = manager.createRequest(Request.SUBSCRIBE, publicUserId, publicUserId, publicUserId);
			request.addHeader(headerFactory.createEventHeader("reg"));
			request.addHeader(headerFactory.createExpiresHeader(this.expire));
			request.addHeader(getContactHeader());
			manager.sendRequest(request);
		} catch (InvalidArgumentException e)
		{
			LOGGER.error(e.getMessage(), e);
		} catch (ParseException e)
		{
			LOGGER.error(e.getMessage(), e);
		} catch (SipException e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void updateContactHeader() throws ParseException
	{
		setFeatureTags();
		if (gruu != null)
			contactHeader.setParameter("+sip.instance", gruu);

		if (displayName != null)
			contactHeader.getAddress().setDisplayName(displayName);
	}

	private void setFeatureTags()
	{
		mergedFeatureTags.clear();

		for (Iterator iter = featureTags.values().iterator(); iter.hasNext();)
		{
			//FeatureTag[] features = (FeatureTag[]) iter.next();
			List featuresList = (List)iter.next();
			if(featuresList != null && featuresList.size() != 0)
			{
				for (Iterator iterator = featuresList.iterator(); iterator.hasNext();) 
				{
					FeatureTag[] features = (FeatureTag[]) iterator.next();

					if (features != null && features.length > 0)
					{
						for (int i = 0; i < features.length; i++)
						{
							FeatureTag newFeature = features[i];

							boolean exists = false;
							for (int j = 0; j < mergedFeatureTags.size(); j++)
							{
								FeatureTag mergedFeature = (FeatureTag) mergedFeatureTags.get(j);

								if (newFeature.getName().equals(mergedFeature.getName()))
								{
									exists = true;
									break;
								}
							}
							if (!exists)
								mergedFeatureTags.add(newFeature);
						}
					}

				}
			}

		}

		contactHeader.getParameters().clear();

		try
		{
			for (int i = 0; i < mergedFeatureTags.size(); i++)
			{
				FeatureTag featureTag = (FeatureTag) mergedFeatureTags.get(i);
				if (featureTag.getType() == FeatureTag.QUOTED_INSENSITIV || featureTag.getType() == FeatureTag.QUOTED_SENSITIV)
					contactHeader.setQuotedParameter(featureTag.getName(), featureTag.getValue());
				else
					contactHeader.setParameter(featureTag.getName(), featureTag.getValue());				
			}
		} catch (ParseException e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}

	private class ReregisterTask extends TimerTask
	{
		public void run()
		{
			try
			{
				reregister();
				Registration.this.registrationTimer.schedule(new ReregisterTask(), (expire / 2) * 1000);
				notifyRegistrationSuccessfull(new RegistrationEvent(this));
			} catch (Exception e)
			{
				LOGGER.error(e.getMessage(), e);
				//System.out.println("ups.. wtf");
				//Registration.this.registrationTimer.schedule(new ReregisterTask(), (expire / 2) * 1000);
				notifyRegistrationFailed(new RegistrationEvent(this));
			}
		}
	}

	private String getIPAddressString(InetSocketAddress address)
	{
		if (address.getAddress() instanceof Inet4Address)
			return address.getAddress().getHostAddress() + ":" + address.getPort();

		return "[" + address.getAddress().getHostAddress() + "]:" + address.getPort();
	}



	/* new listeners for registration*/
	public void addListener(RegistrationListener listener) {
		this.listeners.add(RegistrationListener.class, listener);
	}

	public void removeListener(RegistrationListener listener) {
		this.listeners.remove(RegistrationListener.class, listener);
	}

	private void notifyRegistrationSuccessfull(RegistrationEvent event) {
		RegistrationListener[] listenerArray = (RegistrationListener[]) listeners
				.getListeners(RegistrationListener.class);

		for (int i = 0; i < listenerArray.length; i++) {
			listenerArray[i].notifyRegistrationSuccesfull();
		}
	}

	private void notifyRegistrationFailed(RegistrationEvent event) {
		RegistrationListener[] listenerArray = (RegistrationListener[]) listeners
				.getListeners(RegistrationListener.class);

		for (int i = 0; i < listenerArray.length; i++) {
			listenerArray[i].notifyRegistrationFailed();
		}
	}
}
