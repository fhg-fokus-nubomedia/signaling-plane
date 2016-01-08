package de.fhg.fokus.ims.core.matching;

import java.util.ArrayList;

public class Disjunction
{
	private ArrayList elements = new ArrayList(2);
	private String name;
	
	public void add(Disjunction disjunction)
	{
		
	}
	
	public void add(Conjunction conjunction)
	{
		elements.add(conjunction);
	}
	
	public void add(Negation n1)
	{
		if (this.name == null)
			this.name = n1.getFeature().getName();

		elements.add(n1);
	}

	public void add(Feature feature)
	{	
		if (this.name == null)
			this.name = feature.getName();
		
		elements.add(feature);
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(name);
		buffer.append("=\"");
		for (int i = 0; i < elements.size(); i++)
		{
			if (i > 0)
				buffer.append(",");
			
			Object o = elements.get(i);
			if (o instanceof Feature)
				buffer.append(((Feature)o).getValue());
			else if (o instanceof Negation)
			{
				buffer.append("!");
				buffer.append(((Negation)o).getFeature().getValue());
			}
		}
		
		buffer.append("\"");
		
		return buffer.toString();
	}
}
