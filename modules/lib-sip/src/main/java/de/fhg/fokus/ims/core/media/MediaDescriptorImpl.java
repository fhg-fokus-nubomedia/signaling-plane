package de.fhg.fokus.ims.core.media;

import java.util.ArrayList;
import java.util.List;

import javax.ims.core.media.Media;
import javax.ims.core.media.MediaDescriptor;

import de.fhg.fokus.ims.core.SessionDescriptorImpl;
import de.fhg.fokus.ims.core.sdp.AttributeField;
import de.fhg.fokus.ims.core.sdp.BandwidthField;
import de.fhg.fokus.ims.core.sdp.ConnectionField;
import de.fhg.fokus.ims.core.sdp.MediaField;

/**
 * Implementation of the MediaDescriptor Interface.
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * 
 */
public class MediaDescriptorImpl implements Cloneable, MediaDescriptor
{
	private SessionDescriptorImpl session;
	private MediaImpl media;
	private MediaField m;
	private BandwidthField b;
	private ConnectionField c;
	private ArrayList attributeList = new ArrayList();

	public MediaDescriptorImpl()
	{

	}

	public MediaDescriptorImpl(SessionDescriptorImpl session, MediaImpl media)
	{
		this.session = session;
		this.media = media;
	}

	public MediaDescriptorImpl(SessionDescriptorImpl session, MediaField media)
	{
		this.session = session;
		this.m = media;
	}

	/**
	 * Creates a new MediaDescriptor with m mediaField and c connectionField. No
	 * attribute is set by default.
	 * 
	 * @param media
	 *            the Media this media descriptor belongs to
	 * @param mediaField
	 *            the MediaField
	 * @param connection
	 *            the ConnectionField, or null if no ConnectionField is present
	 *            in the MediaDescriptor
	 */
	public MediaDescriptorImpl(MediaImpl media, MediaField mediaField, ConnectionField connection)
	{
		this.media = media;
		m = mediaField;
		c = connection;
	}

	/**
	 * Creates a new MediaDescriptor with m=mediaField and c=connectionField,
	 * with attributes 'a' equals to <i>attributes</i> (Vector of
	 * AttributeField).
	 * 
	 * @param mediaField
	 *            the MediaField
	 * @param connection
	 *            the ConnectionField, or null if no ConnectionField is present
	 *            in the MediaDescriptor
	 * @param attributes
	 *            the Vector of AttributeField
	 */
	public MediaDescriptorImpl(MediaField mediaField, ConnectionField connection, List attributes)
	{
		m = mediaField;
		c = connection;
		attributeList.addAll(attributes);
	}

	/**
	 * Creates a new MediaDescriptor with m mediaField, c connectionField, and a
	 * <i>attribute</i>.
	 * 
	 * @param media
	 *            the Media this media descriptor belongs to
	 * @param mediaField
	 *            the media field value
	 * @param connection
	 *            the connection field value, or null if no connection field is
	 *            present in the MediaDescriptor
	 * @param attribute
	 *            the first media attribute value
	 */
	public MediaDescriptorImpl(String mediaField, String connection, String attribute)
	{
		m = new MediaField(mediaField);
		if (connection != null)
			c = new ConnectionField(connection);
		if (attribute != null)
			attributeList.add(new AttributeField(attribute));
	}

	/**
	 * Creates a new MediaDescriptor. Acts as a clone
	 * 
	 * @param md
	 *            the cloned MediaDescriptor
	 */
	public MediaDescriptorImpl(MediaDescriptorImpl md)
	{
		m = new MediaField(md.m);

		if (md.c != null)
			c = new ConnectionField(md.c);
		else
			c = null;

		if (md.b != null)
			b = new BandwidthField(md.b);

		for (int i = 0; i < md.attributeList.size(); i++)
			attributeList.add(new AttributeField((AttributeField) md.attributeList.get(i)));
	}

	public SessionDescriptorImpl getSessionDescriptor()
	{
		return session;
	}

	public void setSessionDescriptor(SessionDescriptorImpl session)
	{
		this.session = session;
	}

	public void addAttribute(String attribute) throws IllegalArgumentException
	{
		if (!(media.getState() == Media.STATE_INACTIVE | media.getState() == Media.STATE_ACTIVE))
			throw new IllegalStateException("addAttribute(): Media is not in state INACTIVE OR ACTIVE! cannot add attribute.");

		if (attribute == null)
			return;// throw new

		addAttribute(new AttributeField(attribute));
	}

	public void addAttribute(AttributeField attribute)
	{
		attributeList.add(attribute);
	}

	public String[] getAttributes()
	{
		ArrayList res = new ArrayList();

		for (int i = 0; i < attributeList.size(); i++)
		{
			AttributeField attributeField = (AttributeField) attributeList.get(i);
			if (attributeField.getAttributeValue() == null)
				res.add(attributeField.getAttributeName());
			else
				res.add(attributeField.getAttributeName() + ":" + attributeField.getAttributeValue());
		}

		return (String[]) res.toArray(new String[res.size()]);
	}

	public List getAttributeFields()
	{
		return attributeList;
	}

	public String getMediaDescription() throws IllegalStateException
	{
		return m.getValue();
	}

	public String getMediaTitle()
	{
		return m.getMedia();
	}

	public void removeAttribute(String attribute) throws IllegalArgumentException
	{
		if (attribute == null)
			return;
		if (!(media.getState() == Media.STATE_INACTIVE | media.getState() == Media.STATE_ACTIVE))
			throw new IllegalStateException("removeAttribute(): Media is not in state INACTIVE OR ACTIVE! cannot add attribute.");

		boolean flag = false;

		for (int i = attributeList.size() - 1; i >= 0; i--)
		{
			AttributeField af = (AttributeField) attributeList.get(i);
			if (attribute.equalsIgnoreCase(af.getAttributeValue()))
			{
				attributeList.remove(i);
				flag = true;
			}
		}

		if (!flag)
			throw new IllegalArgumentException("removeAttribute(): Cannot remove attribute ('" + attribute + ")! It is not part  of attribute set.");
	}

	public void setMediaTitle(String title) throws IllegalStateException, IllegalArgumentException
	{
		if (title == null)
			throw new IllegalArgumentException("MediaDescriptionImpl.setMediaTitle(): title argument is null.");
		if (media != null && media.getState() != 1)
		{
			throw new IllegalStateException("MediaDescriptionImpl.setMediaTitle(): unable to change the title, because the media is not in STATE_INACTIVE.");
		} else
		{
			MediaField m2 = new MediaField(title, m.getPort(), 0, m.getTransport(), m.getFormatList());
			m = m2;
			return;
		}

	}

	/**
	 * Whether it has a particular attribute
	 * 
	 * @param aName
	 *            the attribute name
	 * @return true if found, otherwise returns null
	 */
	public boolean hasAttribute(String aName)
	{
		for (int i = 0; i < attributeList.size(); i++)
		{
			if (((AttributeField) attributeList.get(i)).getAttributeName().equals(aName))
				return true;
		}
		return false;
	}

	/**
	 * Gets a particular attribute
	 * 
	 * @param aName
	 *            the attribute name
	 * @return the AttributeField, or null if not found
	 */
	public AttributeField getAttribute(String aName)
	{
		for (int i = 0; i < attributeList.size(); i++)
		{
			AttributeField a = (AttributeField) attributeList.get(i);
			if (a.getAttributeName().equals(aName))
				return a;
		}
		return null;
	}

	/**
	 * Gets media.
	 * 
	 * @return the MediaField
	 */
	public MediaField getMediaField()
	{
		return m;
	}

	public void setMediaField(MediaField mediaField)
	{
		this.m = mediaField;
	}

	/**
	 * Gets connection information.
	 * 
	 * @return the ConnectionField
	 */
	public ConnectionField getConnectionField()
	{
		return c;
	}

	public void setConnectionField(ConnectionField c)
	{
		this.c = c;
	}

	public String toString()
	{
		StringBuffer result = new StringBuffer();
		result.append(m.toString());
		if (c != null)
			result.append(c.toString());

		for (int i = 0; i < attributeList.size(); i++)
			result.append(((AttributeField) attributeList.get(i)).toString());
		return result.toString();
	}

	public String[] getBandwidthInfo()
	{
		if (b != null)
			return new String[]
			{ b.getValue() };
		return null;
	}

	public void setBandwidthInfo(String[] info) throws IllegalStateException, IllegalArgumentException
	{
		if (!(media.getState() == Media.STATE_INACTIVE | media.getState() == Media.STATE_ACTIVE))
			throw new IllegalStateException("setBandwidthInfo(): Media is not in state INACTIVE OR ACTIVE! cannot add attribute.");

		if (info == null)
			return;

		b = new BandwidthField(info[0]);
	}

	public void setMedia(MediaImpl media)
	{
		this.media = media;
	}

	public int getPort()
	{
		return m == null ? 0 : m.getPort();
	}

	public void setPort(int port)
	{
		if (m == null)
			return;
		
		m = new MediaField(m.getMedia(), port, 0, m.getTransport(), m.getFormats());
	}

	public String getTransport()
	{
		return m.getTransport();
	}

	public String getMediaType()
	{
		return m.getMedia();
	}

	public String getFormats()
	{
		return m.getFormats();
	}

	public void setDirection(int direction)
	{

		for (int i = attributeList.size() - 1; i >= 0; i--)
		{
			AttributeField af = (AttributeField) attributeList.get(i);
			String an = af.getAttributeName();
			if (an.equals("sendonly") || an.equals("sendrecv") || an.equals("recvonly") || an.equals("inactive"))
			{
				attributeList.remove(i);
			}
		}

		switch (direction)
		{
		case Media.DIRECTION_SEND:
			addAttribute(new AttributeField("sendonly"));
			break;
		case Media.DIRECTION_RECEIVE:
			addAttribute(new AttributeField("recvonly"));
			break;
		case Media.DIRECTION_INACTIVE:
			addAttribute(new AttributeField("inactive"));
			break;
		}
	}

	public void backup(MediaDescriptorImpl md)
	{
		md.media = media;
		md.m = (MediaField) m.clone();
		if (b != null)
			md.b = (BandwidthField) b.clone();

		if (c != null)
			md.c = (ConnectionField) c.clone();

		if (attributeList != null)
		{
			md.attributeList = new ArrayList();
			for (int i = 0; i < attributeList.size(); i++)
			{
				md.attributeList.add(((AttributeField) attributeList.get(i)).clone());
			}
		}
	}

	public void restore(MediaDescriptorImpl md)
	{
		// TODO Auto-generated method stub
	}

	private static final int CHANGED_MEDIA = 1;
	private static final int CHANGED_PORT = 2;
	private static final int CHANGED_TRANSPORT = 4;
	private static final int CHANGED_FORMATS = 8;
	private static final int CHANGED_CONNECTION = 16;
	private static final int CHANGED_DIRECTION = 32;

	public int detectChanges(MediaDescriptorImpl remoteMD)
	{
		int result = 0;
		MediaField mfR = remoteMD.m;

		if (!mfR.getMedia().equals(m.getMedia()))
			result = result | CHANGED_MEDIA;

		if (mfR.getPort() != m.getPort())
			result = result | CHANGED_PORT;

		if (!mfR.getTransport().equals(m.getTransport()))
			result = result | CHANGED_TRANSPORT;

		if (getDirection() != remoteMD.getDirection())
			result = result | CHANGED_DIRECTION;

		return result;
	}

	public void generateSDPContent(StringBuffer stringbuffer)
	{
		stringbuffer.append(m.toString());
		if (c != null)
			stringbuffer.append(c.toString());

		if (b != null)
			stringbuffer.append(b.toString());

		for (int i = 0; i < attributeList.size(); i++)
			stringbuffer.append(((AttributeField) attributeList.get(i)).toString());
	}

	public int getDirection()
	{
		for (int i = 0; i < attributeList.size(); i++)
		{
			AttributeField f = (AttributeField) attributeList.get(i);
			if ("sendonly".equals(f.getAttributeName()))
				return Media.DIRECTION_SEND;
			if ("recvonly".equals(f.getAttributeName()))
				return Media.DIRECTION_RECEIVE;
			if ("inactive".equals(f.getAttributeName()))
				return Media.DIRECTION_INACTIVE;
			if ("sendrecv".equals(f.getAttributeName()))
				return Media.DIRECTION_SEND_RECEIVE;
		}
		return Media.DIRECTION_SEND_RECEIVE;
	}

	public boolean containsAttribute(String string)
	{
		for (int i = 0; i < attributeList.size(); i++)
		{
			if (((AttributeField) attributeList.get(i)).getAttributeName().equals(string))
				return true;
		}
		return false;
	}

	public boolean isInactive()
	{
		if (containsAttribute("inactive") || (session != null && session.containsAttribute("inactive")))
			return true;

		return false;
	}

	public boolean isReceiveOnly()
	{
		if (containsAttribute("recvonly") || (session != null && session.containsAttribute("recvonly")))
			return true;
		return false;
	}

	public boolean isSendOnly()
	{
		if (containsAttribute("sendonly") || (session != null && session.containsAttribute("sendonly")))
			return true;
		return false;
	}

	public Object clone()
	{
		return new MediaDescriptorImpl(this);
	}

	public int getFormat(String string)
	{
		for(int i = 0; i < attributeList.size();i++)
		{
			AttributeField attribute = (AttributeField) attributeList.get(i);
			if (attribute.getAttributeValue()!=null && attribute.getAttributeValue().contains(string))
			{
				String format = attribute.getAttributeValue().substring(0, attribute.getAttributeValue().indexOf(' '));
				return Integer.parseInt(format);
			}
		}
		
		return -1;
	}

	public void addFormat(int format)
	{
		m.setFormats(m.getFormats() + " " + format);
	}
}
