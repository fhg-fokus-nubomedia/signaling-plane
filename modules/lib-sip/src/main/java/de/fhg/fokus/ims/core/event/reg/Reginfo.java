package de.fhg.fokus.ims.core.event.reg;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.kxml2.kdom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import de.fhg.fokus.ims.core.utils.xml.XmlSerializable;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}anyType&quot;&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref=&quot;{urn:ietf:params:xml:ns:reginfo}registration&quot; maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *         &lt;any processContents='lax' namespace='##other' maxOccurs=&quot;unbounded&quot; minOccurs=&quot;0&quot;/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name=&quot;version&quot; use=&quot;required&quot; type=&quot;{http://www.w3.org/2001/XMLSchema}nonNegativeInteger&quot; /&gt;
 *       &lt;attribute name=&quot;state&quot; use=&quot;required&quot;&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base=&quot;{http://www.w3.org/2001/XMLSchema}string&quot;&gt;
 *             &lt;enumeration value=&quot;full&quot;/&gt;
 *             &lt;enumeration value=&quot;partial&quot;/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
public class Reginfo implements XmlSerializable
{
	public static final class State extends de.fhg.fokus.ims.core.utils.Enum
	{
		public static final State FULL = new State("full");
		
		public static final State PARTIAL = new State("partial");
		
		private State(String value)
		{
			super(value);			
		}
	}

	public static final String NAMESPACE = "urn:ietf:params:xml:ns:reginfo";
	
	private List registration;

	private List any;

	protected int version;

	protected State state;

	/**
	 * Gets the value of the registration property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present
	 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the registration property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getRegistration().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Registration }
	 * 
	 * 
	 */
	public List getRegistration()
	{
		if (registration == null)
		{
			registration = new ArrayList();
		}
		return this.registration;
	}

	/**
	 * Gets the value of the any property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present
	 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the any property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getAny().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Element } {@link Object }
	 * 
	 * 
	 */
	public List getAny()
	{
		if (any == null)
		{
			any = new ArrayList();
		}
		return this.any;
	}

	/**
	 * Gets the value of the version property.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	public int getVersion()
	{
		return version;
	}

	/**
	 * Sets the value of the version property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setVersion(int value)
	{
		this.version = value;
	}

	/**
	 * Gets the value of the state property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public State getState()
	{
		return state;
	}

	/**
	 * Sets the value of the state property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setState(State value)
	{
		this.state = value;
	}

	public void deserialize(XmlPullParser reader) throws XmlPullParserException, IOException
	{
		version = Integer.decode(reader.getAttributeValue(XmlPullParser.NO_NAMESPACE, "version")).intValue();
		state = (State) de.fhg.fokus.ims.core.utils.Enum.parse(State.class, reader.getAttributeValue(XmlPullParser.NO_NAMESPACE, "state"));
	
		String startTagName = reader.getName();

		int eventType = reader.nextTag();
		String namespace = reader.getNamespace();
		String name = reader.getName();

		while (!(eventType == XmlPullParser.END_TAG & name.equals(startTagName)))
		{
			if (Reginfo.NAMESPACE.equals(namespace))
			{
				if (name.equals("registration"))
				{
					Registration reg = new Registration();
					reg.deserialize(reader);
					getRegistration().add(reg);
				}
			} 
			else
			{
				Element element = new Element();
				element.parse(reader);
				getAny().add(element);
			}
			
			eventType = reader.nextTag();
			namespace = reader.getNamespace();
			name = reader.getName();
		}
	}

	public void serialize(XmlSerializer writer) throws IOException
	{
		writer.startTag(NAMESPACE, "reginfo");
		
		writer.attribute(XmlPullParser.NO_NAMESPACE, "version", String.valueOf(version));
		
		writer.attribute(XmlPullParser.NO_NAMESPACE, "state", state.toString());

		if (registration != null && registration.size() > 0)
		{
			
			for(int i = 0; i < registration.size(); i++)
			{
				writer.startTag(NAMESPACE, "registration");
				((Registration)registration.get(i)).serialize(writer);
				writer.endTag(NAMESPACE, "registration");
			}
		}
		
		writer.endTag(NAMESPACE, "reginfo");
	}

}
