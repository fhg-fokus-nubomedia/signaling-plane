package de.fhg.fokus.ims.core.utils;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Enum
{
	private static HashMap values = new HashMap();

	private final String value;

	protected Enum(String value)
	{
		this.value = value;
		add(this);
	}

	public String toString()
	{
		return value;
	}

	protected static void add(Enum value)
	{
		ArrayList list = (ArrayList) values.get(value.getClass());

		if (list == null)
		{
			list = new ArrayList(5);
			values.put(value.getClass(), list);
		}
		list.add(value);
	}

	public static Enum parse(Class type, String value)
	{
		return parse(type, value, false);
	}

	public static Enum parse(Class type, String value, boolean ignoreCase)
	{
		ArrayList list = (ArrayList) values.get(type);
		if (list == null)
			return null;

		for (int i = 0; i < list.size(); i++)
		{
			Enum element = (Enum) list.get(i);
			if (ignoreCase)
			{
				if (element.value.equalsIgnoreCase(value))
					return element;
			} else
			{
				if (element.value.equals(value))
					return element;
			}
		}

		return null;
	}

	public static Enum[] getValues(Class type)
	{
		ArrayList list = (ArrayList) values.get(type);
		if (list == null)
			return null;
		
		return (Enum[]) list.toArray( new Enum[list.size()]);
	}
}
