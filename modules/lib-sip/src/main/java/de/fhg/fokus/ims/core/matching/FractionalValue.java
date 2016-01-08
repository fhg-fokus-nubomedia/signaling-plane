package de.fhg.fokus.ims.core.matching;

public class FractionalValue extends Value
{
	private int b;
	private int a;

	public FractionalValue(int a, int b)
	{
		this.a = a;
		this.b = b;
		
	}
	
	public String toString()
	{
		String res = "";
		if (a < 0 || b < 0)
			res = "-";
		
		return res.concat(String.valueOf(Math.abs(a))).concat("/").concat(String.valueOf(Math.abs(b)));
	}

	public Object getValue()
	{
		return toString();
	}
	
	
}
