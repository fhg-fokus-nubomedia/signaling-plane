package de.fhg.fokus.ims.core.sdp;

import de.fhg.fokus.ims.core.utils.Parser;


/** 
 * time-fields = 1*( "t=" start-time SP stop-time *(CRLF repeat-fields) CRLF) [zone-adjustments CRLF]
 *
 */
public class TimeField extends SdpField
{  

	public TimeField(String time_field)
	{  
		super('t',time_field);
	}

	public TimeField(String start, String stop)
	{  
		super('t',start+" "+stop);
	}

	public TimeField()
	{  
		super('t',"0 0");
	}

	public TimeField(SdpField sf)
	{  
		super(sf);
	}

	public String getStartTime()
	{  
		return (new Parser(value)).getString();
	}

	public String getStopTime()
	{  
		return (new Parser(value)).skipString().getString();
	}

	public Object clone()
	{
		return new TimeField(this);
	}
}
