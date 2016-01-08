package de.fhg.fokus.ims.core.sdp;

import java.net.Inet6Address;
import java.net.InetAddress;

import de.fhg.fokus.ims.core.utils.Parser;

/**
 * SDP connection field.
 * <p>
 * <BLOCKQUOTE>
 * 
 * <PRE>
 *    connection-field = &quot;c=&quot; nettype SP addrtype SP connection-address CRLF
 *                       ;a connection field must be present
 *                       ;in every media description or at the
 *                       ;session-level
 * </PRE>
 * 
 * </BLOCKQUOTE>
 */
public class ConnectionField extends SdpField
{
	public ConnectionField(String connectionField)
	{
		super('c', connectionField);
	}

	public ConnectionField(InetAddress address, int ttl, int num)
	{
		super('c', null);
		String address_type = address instanceof Inet6Address ? "IP6" : "IP4";

		value = "IN " + address_type + " " + address.getHostAddress();
		if (ttl > 0)
			value += "/" + ttl;
		if (num > 0)
			value += "/" + num;
	}

	public ConnectionField(InetAddress address)
	{  
		super('c',null);
		String address_type = null;
		if(address instanceof Inet6Address)  {
			address_type = "IP6";
			value =  "IN "+address_type+" ["+address.getHostAddress()+"]";
		} else {
			address_type = "IP4";
			value = "IN "+address_type+" "+address.getHostAddress();
		}
	}

	public ConnectionField(SdpField sf)
	{
		super(sf);
	}

	public String getAddressType()
	{
		String type = (new Parser(value)).skipString().getString();
		return type;
	}

	public String getAddress()
	{
		String address = (new Parser(value)).skipString().skipString().getString();
		int i = address.indexOf('/');
		if (i < 0)
			return address;
		else
			return address.substring(0, i);
	}

	public int getTTL()
	{
		String address = (new Parser(value)).skipString().skipString().getString();
		int i = address.indexOf('/');
		if (i < 0)
			return 0;
		int j = address.indexOf('/', i);
		if (j < 0)
			return Integer.parseInt(address.substring(i));
		else
			return Integer.parseInt(address.substring(i, j));
	}

	public int getNum()
	{
		String address = (new Parser(value)).skipString().skipString().getString();
		int i = address.indexOf('/');
		if (i < 0)
			return 0;
		int j = address.indexOf('/', i);
		if (j < 0)
			return 0;
		return Integer.parseInt(address.substring(j));
	}

	public Object clone()
	{
		return new ConnectionField(this);
	}
}
