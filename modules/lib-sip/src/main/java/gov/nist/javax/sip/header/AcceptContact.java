package gov.nist.javax.sip.header;

import gov.nist.core.InternalErrorHandler;
import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;

import javax.sip.header.AcceptContactHeader;

/**
 * The generic AcceptContact header
 */
public class AcceptContact extends ParametersHeader implements AcceptContactHeader
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Handle for class. */
	public static Class clazz;

	static
	{
		clazz = AcceptContact.class;
	}

	/**
	 * Default constructor.
	 */
	public AcceptContact()
	{
		super(NAME);
	}

	/**
	 * Sets the specified parameter.
	 * 
	 * @param nv
	 *            parameter's name/value pair
	 */
	public void setParameter(NameValue nv)
	{
		Object val = nv.getValue();
		setParameter(nv.getName(), (val == null) ? null : val.toString());
	}

	/**
	 * Sets the specified parameter.
	 * 
	 * @param name
	 *            name of the parameter
	 * @param value
	 *            value of the parameter.
	 */
	public void setParameter(String name, String value) throws IllegalArgumentException
	{
		NameValue nv = super.parameters.getNameValue(name.toLowerCase());

		if (value != null)
		{

			boolean quoteStart = value.startsWith(Separators.DOUBLE_QUOTE);
			boolean quoteEnd = value.endsWith(Separators.DOUBLE_QUOTE);

			if ((quoteStart && !quoteEnd) || (!quoteStart && quoteEnd))
			{
				throw new IllegalArgumentException(value + " : Unexpected DOUBLE_QUOTE");
			}

			if (quoteStart)
			{ // quoteEnd is true in this case
				value = value.substring(1, value.length() - 1);
			}
		}
		
		if (nv == null)
		{
			nv = new NameValue(name.toLowerCase(), value);

			nv.setQuotedValue();
			super.setParameter(nv);

		} else
		{
			nv.setValue(value);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nist.javax.sip.header.AcceptContactHeader#setType(java.lang.String)
	 */
	public void setType(String value)
	{
		// setParameter(SIPConstants.GENERAL_TYPE, value);
		setParameter(GENERAL_TYPE, value);
	}

	/**
	 * Returns the value of the named parameter, or null if it is not set. A
	 * zero-length String indicates flag parameter.
	 * 
	 * @param name
	 *            name of parameter to retrieve
	 * @return the value of specified parameter
	 */
	public String getParameter(String name)
	{
		String returnValue = super.getParameter(name);
		if (returnValue != null)
		{ // remove quotes
			returnValue = returnValue.substring(1, returnValue.length() - 1);
		}
		return returnValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gov.nist.javax.sip.header.AcceptContactHeader#getType()
	 */
	public String getType()
	{
		return getParameter(GENERAL_TYPE);
	}

	/**
	 * Encodes in canonical form.
	 * 
	 * @return canonical string.
	 */
	public String encodeBody()
	{
		return Separators.STAR + encodeWithSep();
	}

	/**
	 * Encodes the parameters as a string.
	 * 
	 * @return encoded string of object contents.
	 */
	protected String encodeWithSep()
	{
		if (parameters == null)
		{
			return "";
		} else
		{
			return encodeWithSep(parameters);
		}
	}

	protected String encodeWithSep(NameValueList parameters)
	{
		String retVal = parameters.encode();
		if (retVal.length() > 0)
		{
			retVal = ";" + retVal;
		}

		return retVal;
	}

	/**
	 * Clone - do a deep copy.
	 * 
	 * @return Object AcceptContactHeader
	 */
	public Object clone()
	{
		try
		{
			AcceptContact retval = (AcceptContact) this.getClass().newInstance();
			if (this.parameters != null)
				retval.parameters = (NameValueList) parameters.clone();
			return retval;
		} catch (Exception ex)
		{
			InternalErrorHandler.handleException(ex);
			return null;
		}
	}

	/**
	 * Compares for equivalence.
	 * 
	 * @param that
	 *            object to compare
	 * @return true if object matches
	 */
	public boolean equals(Object that)
	{
		if (!that.getClass().equals(this.getClass()))
		{
			return false;
		} else
		{
			AcceptContact other = (AcceptContact) that;
			return this.parameters.equals(other.parameters);
		}
	}

	/**
	 * Gets the header value.
	 * 
	 * @return the content type
	 */
	// public Object getValue()
	public String getValue()
	{
		return Separators.STAR;
	}

	@Override
	protected StringBuilder encodeBody(StringBuilder buffer) {
		// TODO Auto-generated method stub
		return null;
	}

}