package de.fhg.fokus.ims.core.matching;

public class BooleanValue extends Value
{
	private boolean value;

	public BooleanValue(boolean value)
	{
	this.value = value;	
	}
	
	public boolean isTrue()
	{
		return value;
	}
	
	public String toString()
	{
		return value ? "TRUE" : "FALSE";
	}
	
	public Object getValue()
	{
		return new Boolean(value);
	}
}
