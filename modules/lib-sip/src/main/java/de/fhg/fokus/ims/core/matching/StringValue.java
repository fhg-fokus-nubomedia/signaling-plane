package de.fhg.fokus.ims.core.matching;

public class StringValue extends Value
{
	private String value;

	public StringValue(String value)
	{
		this.value = value;
	}
	
	public String toString()
	{
		return "\"<".concat(value).concat(">\"");
	}

	public Object getValue()
	{
		return value;
	}
}
