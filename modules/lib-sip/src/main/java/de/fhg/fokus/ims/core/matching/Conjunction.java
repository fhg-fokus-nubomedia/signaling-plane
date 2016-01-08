package de.fhg.fokus.ims.core.matching;

import java.util.ArrayList;

public class Conjunction
{
	private ArrayList elements = new ArrayList();
	
	public void add(Disjunction disjunction)
	{
		this.elements.add(disjunction);
	}

	public void add(Conjunction conjunction)
	{
		this.elements.add(conjunction);
	}
	
	public void add(Feature feature)
	{
		this.elements.add(feature);
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		
		for (int i = 0; i < elements.size(); i++)
		{
			if (i > 0)
				buffer.append(";");
			
			buffer.append(elements.get(i));
		}
		
		return buffer.toString();
	}
	
	public float match(Conjunction c)
	{
		return 0.0f;
	}
}
