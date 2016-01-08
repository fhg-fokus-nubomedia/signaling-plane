package de.fhg.fokus.ims.core.matching;

public class TokenValue extends Value
{
	private String value;

	public TokenValue(String value)
	{
		this.value = value;
	}
	
	public String toString()
	{
		return "\"".concat(value).concat("\"");
	}

	public Object getValue()
	{
		return value;
	}
}
