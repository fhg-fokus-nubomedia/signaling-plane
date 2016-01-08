package de.fhg.fokus.ims.core.utils.mime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class MimeMessageHandler
{
	public Vector headers;

	private MimeHeader tempheader;

	private MimeHeader lastheader;

	public void notifyIsMimeMultipart()
	{

	}

	public void notifyBeginMailParsing() throws MimeParserException, IOException
	{
	}

	public void notifyEndMailParsing() throws MimeParserException, IOException
	{
	}

	public void notifyBeginPartParsing( int partno ) throws MimeParserException, IOException
	{

	}

	public void notifyEndPartParsing( int partno ) throws MimeParserException, IOException
	{

	}

	public void notifyBeginBodyParsing( int partno ) throws MimeParserException, IOException
	{

	}

	public void notifyEndBodyParsing( int partno ) throws MimeParserException, IOException
	{

	}

	public void notifyBeginHeaderParsing( int partno ) throws MimeParserException, IOException
	{
		tempheader = new MimeHeader( );
	}

	public void notifyEndHeaderParsing( int partno ) throws MimeParserException, IOException
	{
		if ( headers == null )
			headers = new Vector( );
		headers.add( tempheader );
		lastheader = tempheader;
	}

	public void notifyHeader( int partno, String name, byte buf[], int off, int len ) throws MimeParserException
	{
		tempheader.putHeader( name, new String( buf, off, len ) );
	}

	public void notifyBodyData( int partno, InputStream input ) throws MimeParserException, IOException
	{

	}

	public final MimeHeader getMimeHeader( int index )
	{
		return (MimeHeader)this.headers.get( index );
	}

	public final MimeHeader getLastMimeHeader()
	{
		return this.lastheader;
	}

	public final int getMimeHeaderCount()
	{
		return this.headers.size( );
	}
}