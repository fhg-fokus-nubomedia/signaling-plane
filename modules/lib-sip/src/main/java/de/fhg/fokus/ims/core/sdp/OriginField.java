package de.fhg.fokus.ims.core.sdp;

import de.fhg.fokus.ims.core.utils.Parser;



/** 
 * 
 * <BLOCKQUOTE>
 *    origin-field = "o=" username SP sess-id SP sess-version SP
 *                        nettype SP addrtype SP unicast-address CRLF
 * </BLOCKQUOTE>
 */
public class OriginField extends SdpField
{  

	public OriginField(String origin)
	{  
		super('o',origin);
	}


	public OriginField(String username, String sess_id, String sess_version, String addrtype, String address)
	{  
		super('o',username+" "+sess_id+" "+sess_version+" IN "+addrtype+" "+address);
	}


	public OriginField(String username, String sess_id, String sess_version, String address)
	{  	   
		super('o',username+" "+sess_id+" "+sess_version+" IN IP4 "+address);
	}


	public OriginField(SdpField sf)
	{  
		super(sf);
	}


	public String getUserName()
	{  
		return (new Parser(value)).getString();
	}


	public String getSessionId()
	{  
		return (new Parser(value)).skipString().getString();
	}


	public String getSessionVersion()
	{  
		return (new Parser(value)).skipString().skipString().getString();
	}


	public String getAddressType()
	{  
		return (new Parser(value)).skipString().skipString().skipString().skipString().getString();
	}


	public String getAddress()
	{
		return (new Parser(value)).skipString().skipString().skipString().skipString().skipString().getString();
	}

	public Object clone()
	{
		return new OriginField(this);
	}
}
