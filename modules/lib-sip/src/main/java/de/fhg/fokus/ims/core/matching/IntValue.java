package de.fhg.fokus.ims.core.matching;

public class IntValue extends Value
{
	private int value;

	public IntValue(int value)
	{
		this.value = value;
	}
	
	public String toString()
	{
		return String.valueOf(value);
	}

	public Object getValue()
	{
		return new Integer(value);
	}
}
