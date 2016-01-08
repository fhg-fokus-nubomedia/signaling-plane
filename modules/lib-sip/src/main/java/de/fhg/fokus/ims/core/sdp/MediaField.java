package de.fhg.fokus.ims.core.sdp;

import java.util.ArrayList;
import java.util.List;

import de.fhg.fokus.ims.core.utils.Parser;

/**
 * media-field = "m=" media SP port ["/" integer] SP proto 1*(SP fmt) CRLF
 * 
 */
public class MediaField extends SdpField
{
	public MediaField(String media_field)
	{
		super('m', media_field);
	}

	public MediaField(String media, int port, int num, String transport, String formats)
	{
		super('m', null);
		value = media + " " + port;
		if (num > 0)
			value += "/" + num;
		value += " " + transport + " " + formats;
	}

	public MediaField(String media, int port, int num, String transport, List formatlist)
	{
		super('m', null);
		value = media + " " + port;
		if (num > 0)
			value += "/" + num;
		value += " " + transport;
		for (int i = 0; i < formatlist.size(); i++)
			value += " " + formatlist.get(i);
	}

	public MediaField(SdpField sf)
	{
		super(sf);
	}

	public String getMedia()
	{
		return new Parser(value).getString();
	}

	public int getPort()
	{
		String port = (new Parser(value)).skipString().getString();
		int i = port.indexOf('/');
		if (i < 0)
			return Integer.parseInt(port);
		else
			return Integer.parseInt(port.substring(0, i));
	}

	public String getTransport()
	{
		return (new Parser(value)).skipString().skipString().getString();
	}

	public String getFormats()
	{
		return (new Parser(value)).skipString().skipString().skipString().skipWSP().getRemainingString();
	}

	public void setFormats(String formats)
	{
		Parser parser = new Parser(value);
		StringBuffer buffer = new StringBuffer();
		buffer.append(parser.getString()); //Port
		buffer.append(" ");
		buffer.append(parser.getString()); //Media type
		buffer.append(" ");
		buffer.append(parser.getString()); //Protocol
		buffer.append(" ");
		buffer.append(formats);
		value = buffer.toString();
	}
	
	public List getFormatList()
	{
		ArrayList formatlist = new ArrayList();
		Parser par = new Parser(value);
		par.skipString().skipString().skipString();
		while (par.hasMore())
		{
			String fmt = par.getString();
			if (fmt != null && fmt.length() > 0)
				formatlist.add(fmt);
		}
		return formatlist;
	}

	public Object clone()
	{
		return new MediaField(this);
	}
}
