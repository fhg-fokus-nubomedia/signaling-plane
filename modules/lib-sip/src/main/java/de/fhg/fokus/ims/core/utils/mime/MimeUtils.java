package de.fhg.fokus.ims.core.utils.mime;

import java.util.HashMap;
import java.util.StringTokenizer;

public final class MimeUtils
{
	private MimeUtils()
	{
		
	}
	
	public static class MimeHeaderInfo 
	{
		private HashMap parameters = new HashMap();
		private String name;
		private String value;

		public String getName()
		{
			return name;
		}
	
		public String getValue()
		{
			return value;
		}
		
		public String getParameter(String name)
		{
			return (String) parameters.get(name);
		}
	}

	private static final int BEFORE_NAME = 1;
	private static final int AFTER_NAME = 2;
	private static final int BEFORE_VALUE = 3;
	private static final int AFTER_VALUE = 4;
	private static final int IN_VALUE = 5;
	
	
	public static MimeHeaderInfo parseHeader(String value)
	{
		MimeHeaderInfo result = new MimeHeaderInfo();
		StringTokenizer tokenizer = new StringTokenizer(value, ":;\" =", true);
		StringBuffer buffer = new StringBuffer();
		String parameterName = null;
		
		result.name = tokenizer.nextToken();
		
		String v = tokenizer.nextToken();
	
		if (!v.equals(":"))
			throw new IllegalStateException("Expected : after name");
		int state = BEFORE_NAME;
		
		while(tokenizer.hasMoreTokens())
		{
			v = tokenizer.nextToken();
			if (v.equals(" "))
				continue;
			if (v.equals(";"))
				break;
			
			result.value = v;
		}
		
		if (!tokenizer.hasMoreTokens())
			return result;
		
		while (tokenizer.hasMoreTokens())
		{
			v = tokenizer.nextToken();
			
			if (v.equals(" "))
			{
				if (state == BEFORE_NAME || state == AFTER_NAME || state == BEFORE_VALUE || state == AFTER_VALUE )
					continue;

				if (state == IN_VALUE)
					buffer.append(v);
			}
			else if (v.equals(";"))
			{
				if (state == AFTER_VALUE)
					state = BEFORE_NAME;
				else if (state == IN_VALUE)
					buffer.append(";");
			}
			else if (v.equals("\""))
			{
				if (state == BEFORE_VALUE)
					state = IN_VALUE;
				else if (state == IN_VALUE)
				{
					state = AFTER_VALUE;
					result.parameters.put(parameterName, buffer.toString());
					buffer.setLength(0);
					parameterName = null;
				}
			}
			else if (v.equals("="))
			{
				if (state == AFTER_NAME)
					state = BEFORE_VALUE;
				else if (state == IN_VALUE)
					buffer.append("=");
			}
			else if (v.equals(":"))
			{
				if(state == IN_VALUE)
				{
					buffer.append(v);
				}
			}
			else //normal string here 
			{
				if (state == BEFORE_NAME)
				{
					parameterName = v;
					state = AFTER_NAME;
				}
				else if (state == IN_VALUE)
				{
					buffer.append(v);	
				}
				else if (state == BEFORE_VALUE)
				{
					result.parameters.put(parameterName, v);
					parameterName = null;
					
					state = AFTER_VALUE;
				}
			}
		}
		
		if (state == IN_VALUE)
			result.parameters.put(parameterName, buffer.toString());

		return result;
	}
}