package de.fhg.fokus.ims.core;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;

import javax.ims.core.Session;
import javax.ims.core.SessionDescriptor;
import javax.ims.core.media.Media;
import javax.ims.core.media.MediaDescriptor;

import de.fhg.fokus.ims.core.media.MediaDescriptorImpl;
import de.fhg.fokus.ims.core.sdp.AttributeField;
import de.fhg.fokus.ims.core.sdp.ConnectionField;
import de.fhg.fokus.ims.core.sdp.MediaField;
import de.fhg.fokus.ims.core.sdp.OriginField;
import de.fhg.fokus.ims.core.sdp.SdpField;
import de.fhg.fokus.ims.core.sdp.SdpParser;
import de.fhg.fokus.ims.core.sdp.SessionNameField;
import de.fhg.fokus.ims.core.sdp.TimeField;
import de.fhg.fokus.ims.core.utils.SDPUtils;
import de.fhg.fokus.ims.core.utils.SIPUtils;

/**
 * Implementation of the SessionDescriptor Interface.
 * 
 * <blockquote> v=0 o=alice 2890844526 2890844526 IN IP4
 * host.atlanta.example.com s= c=IN IP4 host.atlanta.example.com t=0 0
 * </blockquote>
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @author Andreas Bachmann (andreas.bachmann@fokus.fraunhofer.de)
 */
public class SessionDescriptorImpl implements SessionDescriptor
{
	private static String ADDRESSTYPEIP4 = "IP4", ADDRESSTYPEIP6 ="IP6";
	private Session session;
	private SdpField v;
	private OriginField o;
	private SessionNameField s;
	private ConnectionField c;
	private TimeField t;
	private ArrayList attributeList;
	private ArrayList mediaList; /* vector of media descriptors */
	private boolean isFromRemote;

	private SessionDescriptorImpl()
	{

	}

	public SessionDescriptorImpl(Session session)
	{
		SessionImpl sessionImpl = (SessionImpl) session;
		InetAddress address = sessionImpl.getCoreService().getLocalEndpoint();
		
		String addressType = ADDRESSTYPEIP4;
		String originFieldAddress = IMSManager.getInstance().getLocalAddress().getHostAddress();
		if(address instanceof Inet6Address) {
			addressType = ADDRESSTYPEIP6;
			originFieldAddress = "["+originFieldAddress+"]";
		}
			
		String identifier = (new Long(SDPUtils.getNtpTime(new Date()))).toString();
		
		v = new SdpField('v', "0");
		o = new OriginField(SIPUtils.getUsername(IMSManager.getInstance().getPublicUserId()[0]), identifier, identifier, 
				addressType , originFieldAddress);
		s = new SessionNameField("A Funky MONSTER Stream");
		
		c = new ConnectionField(address);
		t = new TimeField();
		attributeList = new ArrayList();
		mediaList = new ArrayList();
		this.session = session;
		isFromRemote = false;
	}

	public SessionDescriptorImpl(Session session, String sdpRawContent)
	{
		this.session = session;
		isFromRemote = true;

		try
		{
			SdpParser sdpParsedContent = new SdpParser(sdpRawContent);
			MediaDescriptorImpl lastMedia = null;

			while (sdpParsedContent.hasMore())
			{
				if (sdpParsedContent.startsWith("v"))
					v = sdpParsedContent.parseSdpField('v');
				else if (sdpParsedContent.startsWith("o"))
					o = sdpParsedContent.parseOriginField();
				else if (sdpParsedContent.startsWith("s"))
					s = sdpParsedContent.parseSessionNameField();
				else if (sdpParsedContent.startsWith("c"))
				{
					ConnectionField c = sdpParsedContent.parseConnectionField();
					if (lastMedia == null)
						this.c = c;
					else
						lastMedia.setConnectionField(c);
				} else if (sdpParsedContent.startsWith("t"))
					t = sdpParsedContent.parseTimeField();
				else if (sdpParsedContent.startsWith("a="))
				{
					AttributeField attribute = sdpParsedContent.parseAttributeField();
					if (lastMedia == null)
					{
						if (attributeList == null)
							attributeList = new ArrayList();
						attributeList.add(attribute);
					} else
						lastMedia.addAttribute(attribute);
				} else if (sdpParsedContent.startsWith("m="))
				{
					MediaField mediaField = sdpParsedContent.parseMediaField();
					lastMedia = new MediaDescriptorImpl(this, mediaField);
					if (mediaList == null)
						mediaList = new ArrayList();
					mediaList.add(lastMedia);
				} else
					sdpParsedContent.goToNextLine();
			}

			if (t == null)
				t = new TimeField();
			if (o == null)
				o = new OriginField("unknown");
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public SessionDescriptorImpl(SessionDescriptorImpl sd)
	{
		if (sd == null)
			throw new IllegalArgumentException("SessionDescriptor must not be null");
		v = (SdpField) sd.v.clone();
		o = (OriginField) sd.o.clone();
		s = (SessionNameField) sd.s.clone();
		c = (ConnectionField) sd.c.clone();
		t = (TimeField) sd.t.clone();
		attributeList = new ArrayList();
		for (int i = 0; i < sd.attributeList.size(); i++)
		{
			attributeList.add(((AttributeField) sd.attributeList.get(i)).clone());
		}

		mediaList = new ArrayList();
		this.session = sd.session;
		isFromRemote = sd.isFromRemote;
	}

	public SessionDescriptorImpl(SessionImpl sessionImpl, OriginField ro)
	{
		this(sessionImpl);
		this.o = new OriginField(o.getUserName(), ro.getSessionId(), ro.getSessionVersion(), o.getAddressType(), o.getAddress());
	}

	public void addAttribute(String attribute) throws IllegalStateException, IllegalArgumentException
	{
		if (session.getState() != Session.STATE_INITIATED)
			throw new IllegalStateException("SessionDescriptorImpl:addAttribute(): Session is not in state INITIATED! cannot add attribute.");

		if (attribute == null || attributeList.contains(attribute))
			return;// throw new
		// IllegalArgumentException("SessionDescriptorImpl:addAttribute():
		// Cannot add attribute ('" + attribute + ")! It is either
		// null or it is already contained in the list of
		// attributes.");

		addAttribute(new AttributeField(attribute));

	}

	public void addAttribute(AttributeField attribute)
	{
		attributeList.add(new AttributeField(attribute));
	}

	public String[] getAttributes()
	{
		String[] attrs = new String[attributeList.size()];

		for (int i = 0; i < attrs.length; i++)
		{
			attrs[i] = ((AttributeField) attributeList.get(i)).toString();
		}
		return attrs;
	}

	public MediaDescriptor[] getMedia()
	{
		return (MediaDescriptor[]) mediaList.toArray(new MediaDescriptor[mediaList.size()]);
	}

	public String getProtocolVersion()
	{
		return v.getValue();
	}

	public String getSessionId()
	{
		return o.getAddressType();
	}

	public String getSessionInfo() throws IllegalArgumentException, IllegalStateException
	{
		return c.getValue();
	}

	public String getSessionName()
	{
		return s.getSessionName();
	}

	public ConnectionField getConnectionField()
	{
		return c;
	}

	public int getDirection()
	{
		if (containsAttribute("sendonly"))
			return Media.DIRECTION_SEND;
		else if (containsAttribute("recvonly"))
			return Media.DIRECTION_RECEIVE;
		else if (containsAttribute("inactive"))
			return Media.DIRECTION_INACTIVE;
		else
			return Media.DIRECTION_SEND_RECEIVE;
	}

	public boolean containsAttribute(String string)
	{
		if (attributeList == null)
			return false;
		for (int i = 0; i < attributeList.size(); i++)
		{
			if (((AttributeField) attributeList.get(i)).getAttributeName().equals(string))
				return true;
		}
		return false;
	}

	public void removeAttribute(String attribute) throws IllegalStateException, IllegalArgumentException
	{
		if (attribute == null)
			throw new IllegalArgumentException("Attribute must not be null");

		if (session.getState() != Session.STATE_INITIATED)
			throw new IllegalStateException("removeAttribute(): Session is not in state INITIATED! cannot remove attribute.");

		boolean flag = false;

		for (int i = attributeList.size(); i >= 0; i--)
		{
			AttributeField af = (AttributeField) attributeList.get(i);
			if (attribute.equalsIgnoreCase(af.getAttributeName()))
			{
				attributeList.remove(i);
				flag = true;
			}
		}

		if (!flag)
			throw new IllegalArgumentException("removeAttribute(): Cannot remove attribute ('" + attribute + ")! It is not part  of attribute set.");
	}

	public void setSessionInfo(String sessionInfo) throws IllegalStateException, IllegalArgumentException
	{
		if (session.getState() != Session.STATE_INITIATED)
			throw new IllegalStateException("SessionDescriptor:setSessionInfo(): Session is not in state INITIATED! cannot set info.");
		if (sessionInfo == null)
			throw new IllegalArgumentException("SessionDescriptor:setSessionInfo(): Parameter 'info' cannot be null!");

		c = new ConnectionField(sessionInfo);

	}

	public void setSessionName(String name)
	{
		if (session.getState() != Session.STATE_INITIATED)
			throw new IllegalStateException("SessionDescriptorImpl:setSessionName(): Session is not in state INITIATED! cannot set name.");
		if (name == null)
			throw new IllegalArgumentException("SessionDescriptorImpl:setSessionName(): Parameter 'name' cannot be null!");
		s = new SessionNameField(name);
	}

	public boolean isFromRemote()
	{
		return isFromRemote;
	}

	public void setFromRemote(boolean b)
	{
		isFromRemote = b;
	}

	public void setConnectionField(ConnectionField c)
	{
		this.c = c;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		if (v != null)
			sb.append(v.toString());
		if (o != null)
			sb.append(o.toString());
		if (s != null)
			sb.append(s.toString());
		if (c != null)
			sb.append(c.toString());
		if (t != null)
			sb.append(t.toString());
		for (int i = 0; i < attributeList.size(); i++)
			sb.append(((AttributeField) attributeList.get(i)).toString());
		return sb.toString();
	}

	public Object clone()
	{
		SessionDescriptorImpl sdpClone = new SessionDescriptorImpl();
		sdpClone.o = new OriginField(o);
		sdpClone.s = new SessionNameField(s);
		sdpClone.c = new ConnectionField(c);
		sdpClone.t = new TimeField(t);

		return sdpClone;
	}

	/**
	 * Stores copies of all values from this session descriptor in the given
	 * descriptor
	 * 
	 * @param sd
	 */
	public void backup(SessionDescriptorImpl sd)
	{
		if (sd == null)
			throw new IllegalArgumentException("SessionDescriptor must not be null");

		sd.v = (SdpField) v.clone();
		sd.o = (OriginField) o.clone();
		sd.s = (SessionNameField) s.clone();
		sd.c = (ConnectionField) c.clone();
		sd.t = (TimeField) t.clone();
		sd.attributeList = new ArrayList();

		for (int i = 0; i < attributeList.size(); i++)
		{
			sd.attributeList.add(((AttributeField) attributeList.get(i)).clone());
		}

		sd.mediaList = new ArrayList();
		sd.session = session;
		sd.isFromRemote = isFromRemote;
	}

	/**
	 * Restores all values from the given descriptor into this descriptor
	 * 
	 * @param sd
	 */
	public void restore(SessionDescriptorImpl sd)
	{
		if (sd == null)
			throw new IllegalArgumentException("SessionDescriptor must not be null");
		v = (SdpField) sd.v.clone();
		o = (OriginField) sd.o.clone();
		s = (SessionNameField) sd.s.clone();
		c = (ConnectionField) sd.c.clone();
		t = (TimeField) sd.t.clone();
		attributeList = new ArrayList();
		for (int i = 0; i < sd.attributeList.size(); i++)
		{
			attributeList.add(((AttributeField) sd.attributeList.get(i)).clone());
		}

		mediaList = new ArrayList();
		this.session = sd.session;
		isFromRemote = sd.isFromRemote;
	}

	public void setVersion(int i)
	{
		v = new SdpField('v', String.valueOf(i));
	}

	public OriginField getOrigin()
	{
		return o;
	}

	public void setOrigin(OriginField o)
	{
		this.o = o;
	}
}
