package de.fhg.fokus.ims.core.matching;

import java.util.StringTokenizer;

public class Parser
{
	private static final String SEMICOLON = ";";
	public Conjunction parse(String value)
	{
		Conjunction cn = new Conjunction();
		
		StringTokenizer tn = new StringTokenizer(value, ";\"!,=", true);
		
		while(tn.hasMoreTokens())
		{
			readFeature(tn);
		}
		
		return null;
	}

	private void readFeature(StringTokenizer tn)
	{
		String name = tn.nextToken();
		
		if (tn.hasMoreTokens())
		{
			String token = tn.nextToken();
			if (token.equals(SEMICOLON))
			{
				
			}
			expect(tn, "=");	
		}
		else
		{
			Feature f = new Feature(name, Operation.EQUAL, new BooleanValue(true));
		}
	}

	private void expect(StringTokenizer tn, String string)
	{
		String token = tn.nextToken(); 
		if (token.equals(string))
			return;
		
		
		throw new IllegalStateException("Expected: " + string + " found: "+ token);
	}
}
