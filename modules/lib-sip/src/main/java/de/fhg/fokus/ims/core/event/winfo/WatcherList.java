package de.fhg.fokus.ims.core.event.winfo;

import java.io.IOException;
import java.util.Arrays;

import org.kxml2.kdom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import de.fhg.fokus.ims.core.utils.xml.XmlSerializable;

public class WatcherList implements XmlSerializable 
{
	private Watcher[] watchers;
	
	private String resource;
	
	private String eventPackage;
	
	private Element[] any;

	public Watcher[] getWatchers()
	{
		return (Watcher[]) Arrays.copyOf(watchers, watchers.length);
	}

	public void setWatchers(Watcher[] watchers)
	{
		this.watchers = (Watcher[]) Arrays.copyOf(watchers, watchers.length);
	}

	public String getResource()
	{
		return resource;
	}

	public void setResource(String resource)
	{
		this.resource = resource;
	}

	public String getEventPackage()
	{
		return eventPackage;
	}

	public void setEventPackage(String eventPackage)
	{
		this.eventPackage = eventPackage;
	}

	public Element[] getAny()
	{
		return any;
	}

	public void setAny(Element[] any)
	{
		this.any = any;
	}

	public void deserialize(XmlPullParser reader) throws XmlPullParserException, IOException
	{
		// TODO Auto-generated method stub
		
	}

	public void serialize(XmlSerializer writer) throws IOException
	{
		// TODO Auto-generated method stub
		
	}
	
//	<xs:complexType>
//    <xs:sequence>
//      <xs:element ref="tns:watcher" minOccurs="0" maxOccurs=
//       "unbounded"/>
//        <xs:any namespace="##other" processContents="lax"
//                minOccurs="0" maxOccurs="unbounded"/>
//    </xs:sequence>
//    <xs:attribute name="resource" type="xs:anyURI" use="required"/>
//    <xs:attribute name="package" type="xs:string" use="required"/>
//  </xs:complexType>
//</xs:element>

}
