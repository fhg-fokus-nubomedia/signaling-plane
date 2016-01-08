package de.fhg.fokus.ims.core.event.winfo;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import de.fhg.fokus.ims.core.utils.xml.XmlSerializable;

public class Watcher implements XmlSerializable
{
	public static final class Status extends de.fhg.fokus.ims.core.utils.Enum
	{
		public static final Status PENDING = new Status("pending");
		public static final Status ACTIVE = new Status("active");
		public static final Status WAITING = new Status("waiting");
		public static final Status TERMINATED = new Status("terminated");

		private Status(String value)
		{
			super(value);
		}
	}

	public static final class Event extends de.fhg.fokus.ims.core.utils.Enum
	{

		public static final Event SUBSCRIBE = new Event("subscribe");
		public static final Event APPROVED = new Event("approved");
		public static final Event DEACTIVATED = new Event("deactivated");
		public static final Event PROBATION = new Event("probation");
		public static final Event REJECTED = new Event("rejected");
		public static final Event TIMEOUT = new Event("timeout");
		public static final Event GIVEUP = new Event("giveup");
		public static final Event NORESOURCE = new Event("noresource");

		private Event(String value)
		{
			super(value);

		}
	}

	private Status status;

	private Event event;

	private int expiration;

	private String id;

	private int durationSubscribed;

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public Event getEvent()
	{
		return event;
	}

	public void setEvent(Event event)
	{
		this.event = event;
	}

	public int getExpiration()
	{
		return expiration;
	}

	public void setExpiration(int expiration)
	{
		this.expiration = expiration;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public int getDurationSubscribed()
	{
		return durationSubscribed;
	}

	public void setDurationSubscribed(int durationSubscribed)
	{
		this.durationSubscribed = durationSubscribed;
	}

	public void deserialize(XmlPullParser reader) throws XmlPullParserException, IOException
	{
		// TODO Auto-generated method stub

	}

	public void serialize(XmlSerializer writer) throws IOException
	{

	}

	/*
	 * <xs:attribute name="expiration" type="xs:unsignedLong"/> <xs:attribute name="id" type="xs:string" use="required"/> <xs:attribute
	 * name="duration-subscribed" type="xs:unsignedLong"/>
	 */

}
