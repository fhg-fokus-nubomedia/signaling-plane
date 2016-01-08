package de.fhg.fokus.ims.core.matching;

public class Operation
{
	public static final Operation EQUAL = new Operation()
	{
		public String toString() {
			return "=";
		}
	};

	public static final Operation LESS = new Operation()
	{
		public String toString()
		{
			return "<=";
		}
	};

	public static final Operation GREATER = new Operation()
	{
		public String toString()
		{
			return ">=";
		}
	};

}
