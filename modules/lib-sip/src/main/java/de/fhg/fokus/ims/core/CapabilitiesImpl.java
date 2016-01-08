package de.fhg.fokus.ims.core;

import java.util.ArrayList;

import javax.ims.ImsException;
import javax.ims.core.Capabilities;
import javax.ims.core.CapabilitiesListener;
import javax.ims.core.Message;
import javax.ims.core.Session;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import de.fhg.fokus.ims.core.utils.SIPUtils;

/**
 * Implementation of the Capabilities Interface.
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public class CapabilitiesImpl extends ServiceMethodImpl implements Capabilities
{	
	private static Logger LOGGER = LoggerFactory.getLogger(CapabilitiesImpl.class);
	
	private CapabilitiesListener listener;
	private int state;

	private CapabilityContainer container; 

	private ArrayList supportedFeatures;	


	public CapabilitiesImpl(CoreServiceImpl coreService, CapabilityContainer container,String remoteUri, String localUri) throws ImsException 
	{
		super(coreService, container,remoteUri, localUri);

		listener = null;
		supportedFeatures = null;
		state = Capabilities.STATE_INACTIVE;

		this.container = container;
		container.put(this);
		setNextRequest(new MessageImpl(Message.CAPABILITIES_QUERY, "OPTIONS"));	
	}

	public CapabilitiesImpl(CoreServiceImpl coreService, CapabilityContainer capabilityContainer, Request request, String localUri) throws ImsException 
	{
		super(coreService, capabilityContainer, request, localUri);

		listener = null;
		this.container = capabilityContainer;
		setState(STATE_ACTIVE);
		MessageImpl message = new MessageImpl(Message.CAPABILITIES_QUERY, request);
		setPreviousRequest(message);

		container.put(this);
		
		supportedFeatures = SIPUtils.getFeatureTags(request);				
	}

	public ArrayList getSupportedFeatures() {
		return supportedFeatures;
	}

	public String[] getRemoteAppIds() {
		return new String[0];
	}

	public String[] getRemoteUserIdentities() 
	{
		LOGGER.debug("CapabilitiesImpl.getRemoteUserIdentities");	
		if (state != STATE_ACTIVE) {
			throw new IllegalStateException(
					"getRemoteContacts only allowed when in state " +
							"STATE_ACTIVE (state = " + state + ")");
		}
		return new String[]{remoteUserId};

	}

	public int getState() {
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

	public boolean hasCapabilities(String appId) {
		// TODO Auto-generated method stub
		return false;
	}

	public void queryCapabilities() 
	{
		queryCapabilities(null);
	}

	public void queryCapabilities(Session session)
	{		
		if (state != STATE_INACTIVE) {
			throw new IllegalStateException(
					"queryCapabilities only allowed when in state " +
							"STATE_INACTIVE (state = " + state + ")");
		}

		state = STATE_PENDING;

		setNextRequest(new MessageImpl(Message.CAPABILITIES_QUERY, "OPTIONS"));		
		try 
		{
			sendNextRequest();        	
		} 
		catch (Exception e) 
		{
			LOGGER.error("Exception when sending capabilities request.", e);			
			this.state = Capabilities.STATE_INACTIVE;

			if (listener != null)
				listener.capabilityQueryDeliveryFailed(this);
		}    	
	}

	public CapabilitiesListener getListener()
	{
		return listener;
	}
	
	public void setListener(CapabilitiesListener listener)
	{
		if (listener == null)
			this.listener = null;
		else
			this.listener = listener;
	}

	// ------ Helper Methods ------ //
	public void methodDelivered(MessageImpl responseMessage)
	{
		LOGGER.info("CapabilitiesImpl: method delivered with {"+new Integer(responseMessage.getStatusCode())+"} {"+responseMessage.getMethod()+"} ");			
		this.state = Capabilities.STATE_ACTIVE;
		supportedFeatures = SIPUtils.getFeatureTags(responseMessage.getResponse());
		if (listener != null)
			listener.capabilityQueryDelivered(this);
	}

	public void methodDeliveryFailed(MessageImpl responseMessage)
	{
		LOGGER.info("CapabilitiesImpl: method failed " + responseMessage.getMethod() + " - statuscode " + responseMessage.getStatusCode());
		this.state = Capabilities.STATE_INACTIVE;

		if (listener != null)
			listener.capabilityQueryDeliveryFailed(this);
	}



	protected MessageImpl createRequestMessage(Request request)
	{
		return new MessageImpl(Message.CAPABILITIES_QUERY, request);
	}

	protected MessageImpl createResponseMessage(Response response)
	{
		return new MessageImpl(Message.CAPABILITIES_QUERY, response);
	}	
}
