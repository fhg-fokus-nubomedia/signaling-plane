package de.fhg.fokus.ims.core.utils.mime;

import java.util.Enumeration;
import java.util.Properties;

public class MimeHeader
{
	private Properties header;

	/**
	 * Erzeugt einen neuen MimeHeader
	 */
	public MimeHeader()
	{
		header = new Properties( );
	}

	/**
	 * Gibt an ob der Header im aktuellen Headerstring enthalten ist!
	 * 
	 * @param header
	 *            Name des Headers (z.b. "Subject");
	 * @returns <code>true</code> wenn der Header enthalten ist sonst
	 *          <code>false</code>
	 */
	public boolean containsHeader( String header )
	{
		return this.header.containsKey( header );
	}

	/**
	 * Liefert den Wert des gegeben Headers
	 * 
	 * @param header
	 *            Name des Headers
	 * @return Wert des Headers oder null wenn der Header nicht existiert
	 */
	public String getHeader( String header )
	{
		return (String)this.header.get( header );
	}

	/**
	 * F�gt dem Headerstring einen neuen Header hinzu
	 * 
	 * @param name
	 *            Name des Headers
	 * @param value
	 *            Wert des Headers
	 */
	public void putHeader( String header, String value )
	{
		this.header.put( header, value );
	}

	/**
	 * Liefert den Content Type des Parts zur�ck, zu dem Header geh�rt!
	 * 
	 * @return String mit dem Contenttype oder null falls dieser nicht angegeben
	 *         ist!
	 */
	public String getContentType()
	{
		String ct = (String)this.header.get( "Content-Type" );
		if ( ct != null )
		{
			return ct.substring( 0, ct.indexOf( ";" ) );
		} else
			return null;
	}

	public boolean isMultipart()
	{
		String ct = getContentType( );
		if ( ct != null )
			return ct.indexOf( "multipart" ) > -1;
		else
			return false;
	}

	public String getBoundary()
	{
		String ct = (String)this.header.get( "Content-Type" );
		if ( ct != null )
			return ct.substring( ct.indexOf( "\"" ) + 1, ct.lastIndexOf( "\"" ) );
		else
			return null;
	}

	public String getDisposition()
	{
		String dp = (String)this.header.get( "Content-Disposition" );
		if ( dp != null )
			return dp.substring( 0, dp.indexOf( ";" ) );
		else
			return null;
	}

	public String getFileName()
	{
		String dp = (String)this.header.get( "Content-Disposition" );
		if ( dp != null )
			return dp.substring( dp.indexOf( "\"" ) + 1, dp.lastIndexOf( "\"" ) );
		else
			return null;
	}

	public String getEncoding()
	{
		return (String)this.header.get( "Content-Transfer-Encoding" );
	}

	public Enumeration getHeadersNames()
	{
		return header.keys( );
	}

	public Enumeration getHeaderValues()
	{
		return header.elements( );
	}
}