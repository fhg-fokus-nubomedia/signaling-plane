package de.fhg.fokus.ims.core;

import gov.nist.javax.sip.address.AddressFactoryImpl;
import gov.nist.javax.sip.header.Contact;
import gov.nist.javax.sip.header.HeaderFactoryImpl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sip.address.Address;
import javax.sip.header.Header;
import javax.sip.header.Parameters;

public class FeatureTag
{
	public static final int BOOLEAN = 0;
	public static final int QUOTED_SENSITIV = 1;
	public static final int QUOTED_INSENSITIV = 2;
	public static final int NUMBER = 3;
	public static final int TOKEN = 4;
	
	private int type;
	private static int NAME = 1;
	private static int VALUE = 2;
	private static int IN_TEXT = 3;

	private String name;
	private String value;

	private FeatureTag()
	{
		
	}
	
	public FeatureTag(String name)
	{
		this(name, null);
	}
	
	public FeatureTag(String name, String value)
	{
		this.name = name;
		if ("TRUE".equals(value))
			this.value = null;
		else
			this.value = value;
	}
	

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}
	
	public int getType() 
	{
		return type;
	}

	public void setType(int type) 
	{
		this.type = type;
	}

	public static FeatureTag[] parse(String value)
	{
		if (value == null)
			throw new NullPointerException("value");

		if (value.length() == 0)
			throw new IllegalArgumentException("value is empty");

		ArrayList result = new ArrayList();
		int state = NAME;
		StringBuffer buffer = new StringBuffer();

		FeatureTag currentTag = new FeatureTag();

		for (int i = 0; i < value.length(); i++)
		{
			char c = value.charAt(i);

			switch (c)
			{
			case ';':
				if (state == IN_TEXT)
				{
					buffer.append(c);
					break;
				} else if (state == NAME)
				{
					currentTag.name = buffer.toString();
					buffer.delete(0, buffer.length());
				} else
				{
					currentTag.setValue(buffer.toString());
					state = NAME;
					buffer.delete(0, buffer.length());
				}
				result.add(currentTag);
				currentTag = new FeatureTag();
				break;
			case '"':
				if (state == IN_TEXT)
				{
					state = VALUE;
				} else if (state == VALUE)
				{
					state = IN_TEXT;
				}
				break;
			case '=':
				if (state == IN_TEXT)
				{
					buffer.append(c);
				} else
				{
					currentTag.name = buffer.toString();
					state = VALUE;
					buffer.delete(0, buffer.length());
				}
				break;
			default:
				buffer.append(c);
				break;
			}
		}

		if (state == NAME)
			currentTag.name = buffer.toString();
		else
			currentTag.setValue(buffer.toString());

		result.add(currentTag);

		return (FeatureTag[]) result.toArray(new FeatureTag[result.size()]);
	}

	public static FeatureTag[] parse(Parameters params)
	{
		ArrayList result = new ArrayList();
		
		for (Iterator iterator = params.getParameterNames(); iterator.hasNext();)
		{
			String name = (String)iterator.next();
			String value = params.getParameter(name);
			
			result.add(new FeatureTag(name, value));
		}
		
		return (FeatureTag[]) result.toArray(new FeatureTag[result.size()]);
	}
	
	private void setValue(String value)
	{
		if ("TRUE".equals(value) || value == null)
		{
			type = BOOLEAN;
			value = null;
		}
		else
		{
			if (value.startsWith("<") && value.endsWith(">"))
				type = QUOTED_SENSITIV;
			else
			{
				boolean number = true;
				for (int i = 0; i < value.length(); i++)
				{
					if (!Character.isDigit(value.charAt(i)))
					{
						number = false;
						break;
					}
				}
				if (number)
					type = NUMBER;
				else
					type = QUOTED_INSENSITIV;
			}
			this.value = value;
		}
	}

	public boolean match(FeatureTag tag)
	{
		if (tag == null)
			return false;
		
		if (!tag.name.equals(name))
			return false;
		
		if (value == null & tag.value == null)
			return true;
		
		if ((value == null & tag.value != null) | (value != null & tag.value == null))
			return false;
		
		return value.equals(tag.value);
	}
	
	
	public boolean equals(Object obj)
	{
		if (!(obj instanceof FeatureTag))
			return false;
			
		return name.equals(((FeatureTag)obj).name);
	}
	
	public int hashCode()
	{
		return name.hashCode();
	}
	
	
	public static void main(String[] args) throws ParseException
	{
//		FeatureTag[] tags = FeatureTag.parse("audio;video;mobility=\"fixed\";" + "+sip.message=\"TRUE\";other-param=66372;"
//				+ "methods=\"INVITE,OPTIONS,BYE,CANCEL,ACK\";schemes=\"sip,http\"");
		FeatureTag[] tags = FeatureTag.parse("+g.3gpp.cs-voice;+g.3gpp.iari-ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.im,urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.ft,urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-is,urn%3Aurn-7%3A3gpp-application.ims.iari.fhg.voip\"");

		AddressFactoryImpl xf = new AddressFactoryImpl();
		Address a = xf.createAddress("sip:test@193.175.123asd");
		HeaderFactoryImpl x = new HeaderFactoryImpl();
		Contact ch = (Contact) x.createContactHeader(a);

		for (int i = 0; i < tags.length; i++)
		{
			System.out.println(tags[i].name + ": " + tags[i].value+" : " + tags[i].type);
			if (tags[i].type == QUOTED_INSENSITIV || tags[i].type == QUOTED_SENSITIV)
				ch.setQuotedParameter(tags[i].name, tags[i].value);
			else
				ch.setParameter(tags[i].name, tags[i].value);
		}

		Header h = x.createHeader("Accept-Contact", "*;test;tes2=sadas;testen=\"dasd\"");
		
		
		
		System.out.println(ch);
		System.out.println(h);

	}

}
