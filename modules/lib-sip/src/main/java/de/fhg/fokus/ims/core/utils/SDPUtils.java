package de.fhg.fokus.ims.core.utils;

import java.util.Date;

public class SDPUtils 
{
	public static final long NTP_CONST=2208988800L;
	
	public static long getNtpTime(Date d)
	  {
	    if (d == null) return -1;
	    return ((d.getTime() / 1000) + NTP_CONST);
	  }
}
