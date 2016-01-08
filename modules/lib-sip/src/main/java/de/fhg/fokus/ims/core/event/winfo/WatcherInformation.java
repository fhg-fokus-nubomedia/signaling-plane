package de.fhg.fokus.ims.core.event.winfo;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import de.fhg.fokus.ims.core.utils.xml.XmlSerializable;

public class WatcherInformation implements XmlSerializable
{
	public static final String NAMESPACE = "urn:ietf:params:xml:ns:watcherinfo";
	
	public final static class State extends de.fhg.fokus.ims.core.utils.Enum
	{
		public static final State FULL = new State("full");
		public static final State PARTIAL = new State("partial");
		
		private State(String value)
		{
			super(value);
		}
	}
	
	private WatcherList[] watcherLists;
	
	private int version;
	
	private State state;

	public void deserialize(XmlPullParser reader) throws XmlPullParserException, IOException
	{
		// TODO Auto-generated method stub
		
	}

	public void serialize(XmlSerializer writer) throws IOException
	{
		// TODO Auto-generated method stub
		
	}
	
  
  
}
