package de.fhg.fokus.ims.core.matching;

public class Feature
{
	private String name;
	private Operation op;
	private Value value;

	public Feature(String name, Operation op, Value value)
	{
		this.name = name;
		this.op = op;
		this.value = value;
	}
	
	public String getName()
	{
		return name;
	}

	public Object getValue()
	{
		return value.getValue();
	}
	
	public String toString()
	{
		boolean noValue = false;
		if (value instanceof BooleanValue)
		{
			if (!((BooleanValue)value).isTrue())
				return "";
			
			noValue = true;
		}
		
		StringBuffer result = new StringBuffer();
		
		String t = name;
		if (name.startsWith("sip."))
		{
			result.append(name.substring(4));
		}
		else
		{
			result.append("+");
			result.append(name);
		}
		if (noValue)
			return result.toString();
		
		result.append(op);
		result.append(value);
		
		return result.toString();
	}

	
}

