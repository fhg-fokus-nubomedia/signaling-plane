package de.fhg.fokus.ims.core.matching;

public class RangeValue extends Value
{
	private Value to;
	private Value from;

	public RangeValue(Value from, Value to)
	{
		this.from = from;
		this.to = to;
	}
	
	public String toString()
	{
		return from.toString().concat(":").concat(to.toString());
	}

	public Object getValue()
	{
		return toString();
	}
}
