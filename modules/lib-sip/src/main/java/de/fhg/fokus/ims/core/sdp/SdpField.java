package de.fhg.fokus.ims.core.sdp;

/**
 * <p>
 * SdpField rapresents a SDP line field. It is formed by a 'type' (char) and a
 * 'value' (String).
 * <p>
 * A SDP line field is of the form &lt;type&gt; = &lt;value&gt;
 */
public class SdpField
{
	char type;
	String value;

	/**
	 * Creates a new SdpField.
	 * 
	 * @param s_type
	 *            the field type
	 * @param s_value
	 *            the field value
	 */
	public SdpField(char s_type, String s_value)
	{
		type = s_type;
		value = s_value;
	}

	/**
	 * Creates a new SdpField.
	 * 
	 * @param sf
	 *            the SdpField clone
	 */
	public SdpField(SdpField sf)
	{
		type = sf.type;
		value = sf.value;
	}

	/**
	 * Creates a new SdpField based on a SDP line of the form <type>=<value>.
	 * The SDP value terminats with the end of the String or with the first CR
	 * or LF char.
	 * 
	 * @param str
	 *            the &lt;type&gt; = &lt;value&gt; line
	 */
	public SdpField(String str)
	{
		SdpParser par = new SdpParser(str);
		SdpField sf = par.parseSdpField();
		type = sf.type;
		value = sf.value;
	}

	/**
	 * Creates and returns a copy of the SdpField.
	 * 
	 * @return a SdpField clone
	 */
	public Object clone()
	{
		return new SdpField(this);
	}

	/**
	 * Whether the SdpField is equal to Object <i>obj</i>
	 * 
	 * @return true if equal
	 */
	public boolean equals(Object obj)
	{
		try
		{
			SdpField sf = (SdpField) obj;
			if (type != sf.type)
				return false;
			if (value != sf.value)
				return false;
			return true;
		} catch (Exception e)
		{
			return false;
		}
	}
	
	public int hashCode()
	{
		return 17 ^ type ^ value.hashCode();
	}

	/**
	 * Gets the type of field
	 * 
	 * @return the field type
	 */
	public char getType()
	{
		return type;
	}

	/**
	 * Gets the value
	 * 
	 * @return the field value
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Gets string representation of the SdpField
	 * 
	 * @return the string representation
	 */
	public String toString()
	{
		return type + "=" + value + "\r\n";
	}
}
