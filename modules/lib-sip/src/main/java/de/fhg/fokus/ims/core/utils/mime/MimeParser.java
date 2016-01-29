package de.fhg.fokus.ims.core.utils.mime;

import java.io.IOException;
import java.io.InputStream;

public class MimeParser
{
	private String boundary = null;

	private int partno = 0;

	protected int ch = -1;

	protected MultipartInputStream minput;

	protected InputStream input = null;

	protected byte buffer[] = new byte[128];

	protected int bsize = 0;

	/**
	 * The factory used to create new MIME header holders.
	 */
	protected MimeMessageHandler holder = null;

	protected void expect( int car ) throws MimeParserException, IOException
	{
		if ( car != ch )
		{
			String sc = ( new Character( (char)car ) ).toString( );
			String se = ( new Character( (char)ch ) ).toString( );
			throw new MimeParserException( "expecting " + sc + "(" + car + ")" + " got " + se + "(" + ch + ")\n"
					+ "context: " + new String( buffer, 0, bsize ) + "\n" );
		}
		ch = input.read( );
	}

	protected void skipSpaces() throws MimeParserException, IOException
	{
		while ( ( ch == ' ' ) || ( ch == '\t' ) )
			ch = input.read( );
	}

	protected final void append( int c )
	{
		if ( bsize + 1 >= buffer.length )
		{
			byte nb[] = new byte[buffer.length * 2];
			System.arraycopy( buffer, 0, nb, 0, buffer.length );
			buffer = nb;
		}
		buffer[bsize++] = (byte)c;
	}

	/*
	 * Get the header name:
	 */

	protected String parse822HeaderName() throws MimeParserException, IOException
	{
		bsize = 0;
		while ( ( ch >= 32 ) && ( ch != ':' ) )
		{
			append( (char)ch );
			ch = input.read( );
		}
		expect( ':' );
		if ( bsize <= 0 )
			throw new MimeParserException( "expected a header name." );
		return new String( buffer, 0, bsize );
	}

	/*
	 * Get the header body, still trying to be 822 compliant *and* HTTP robust,
	 * which is unfortunatelly a contrdiction.
	 */

	protected void parse822HeaderBody() throws MimeParserException, IOException
	{
		bsize = 0;
		skipSpaces( );
		loop: while ( true )
		{
			switch ( ch ) {
			case -1:
				break loop;
			case '\r':
				if ( ( ch = input.read( ) ) != '\n' )
				{
					append( '\r' );
					continue;
				}
			// no break intentional
			case '\n':
				// do as if '\r' had been received. This defeats 822, but
				// makes HTTP more "robust". I wish HTTP were a binary
				// protocol.
				switch ( ch = input.read( ) ) {
				case ' ':
				case '\t':
					do
					{
						ch = input.read( );
					} while ( ( ch == ' ' ) || ( ch == '\t' ) );
					append( ch );
					break;
				default:
					break loop;
				}
				break;
			default:
				append( (char)ch );
				break;
			}
			ch = input.read( );
		}
		return;
	}

	/*
	 * Parse the given input stream for an HTTP 1.1 token.
	 */

	protected String parseToken( boolean lower ) throws MimeParserException, IOException
	{
		bsize = 0;
		while ( true )
		{
			switch ( ch ) {
			// CTLs
			case -1:
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			case 21:
			case 22:
			case 23:
			case 24:
			case 25:
			case 26:
			case 27:
			case 28:
			case 29:
			case 30:
			case 31:
			// tspecials
			case '(':
			case ')':
			case '<':
			case '>':
			case '@':
			case ',':
			case ';':
			case ':':
			case '\\':
			case '\"':
			case '/':
			case '[':
			case ']':
			case '?':
			case '=':
			case '{':
			case '}':
			case ' ':
				return new String( buffer, 0, bsize );
			default:
				append( (char) ( lower ? Character.toLowerCase( (char)ch ) : ch ) );
			}
			ch = input.read( );
		}
	}

	protected void parse822Headers( MimeMessageHandler msg ) throws MimeParserException, IOException
	{
		while ( true )
		{
			if ( ch == '\r' )
			{
				if ( ( ch = input.read( ) ) == '\n' )
					return;
			} else if ( ch == '\n' )
			{
				return;
			}
			String name = parse822HeaderName( );
			skipSpaces( );
			parse822HeaderBody( );
			msg.notifyHeader( partno, name, buffer, 0, bsize );
			if ( name.toUpperCase( ).equals( "CONTENT-TYPE" ) )
			{
				String bound = new String( buffer, 0, bsize );
				int pos = bound.toUpperCase( ).indexOf( "BOUNDARY" );
				if ( pos > -1 )
				{
					this.boundary = bound.substring( pos + 9 );
					if ( boundary.startsWith( "\"" ) )
						boundary = boundary.substring( 1 );
					if ( boundary.endsWith( "\"" ) )
						boundary = boundary.substring( 0, boundary.length( ) - 1 );
				}
			}
		}
	}

	/**
	 * parse the stream, and create a MimeHeaderHolder containing all the parsed
	 * headers, note that invalid headers will trigger an exception in strict
	 * mode, and will just be removed in lenient mode
	 * 
	 * @param lenient,
	 *            a boolean, true if we want to be kind with bad people
	 * @return a MimeHeaderHolder instance containing the parsed headers
	 */
	public void parse( boolean lenient ) throws MimeParserException, IOException
	{
		ch = input.read( );
		cached = true;
		holder.notifyBeginHeaderParsing( partno );
		if ( !cached )
			ch = input.read( );
		if ( lenient )
		{
			try
			{
				parse822Headers( holder );
			} catch ( MimeParserException ex )
			{
				// be lenient ;)
			}
		} else
		{
			parse822Headers( holder );
		}
		holder.notifyEndHeaderParsing( partno );
		return;
	}

	/**
	 * parse the stream, and create a MimeHeaderHolder containing all the parsed
	 * headers, in lenient mode Always be lenient by default (general rule is:
	 * be lenient in what you accept conservative with what you generate).
	 */
	public void parse() throws MimeParserException, IOException
	{
		holder.notifyBeginMailParsing( );
		parse( true );
		if ( boundary != null )
		{
			holder.notifyIsMimeMultipart( );
			input = new MultipartInputStream( input, boundary.getBytes( ) );
			minput = (MultipartInputStream)input;
			while ( minput.nextInputStream( ) )
			{
				partno++;
				holder.notifyBeginPartParsing( partno );
				parse( true );
				holder.notifyBeginBodyParsing( partno );
				holder.notifyBodyData( partno, input );
				holder.notifyEndBodyParsing( partno );
				holder.notifyEndPartParsing( partno );
			}
		} else
		{
			partno++;
			holder.notifyBeginPartParsing( partno );
			holder.notifyBeginBodyParsing( partno );
			holder.notifyBodyData( partno, input );
			holder.notifyEndBodyParsing( partno );
			holder.notifyEndPartParsing( partno );
		}
		holder.notifyEndMailParsing( );
	}
	
	public void parse(String boundary) throws MimeParserException, IOException
	{
		holder.notifyBeginMailParsing( );
		this.boundary = boundary;
		if ( boundary == null )
		{
			partno++;
			holder.notifyBeginPartParsing( partno );
			holder.notifyBeginBodyParsing( partno );
			holder.notifyBodyData( partno, input );
			holder.notifyEndBodyParsing( partno );
			holder.notifyEndPartParsing( partno );
		} else
		{
			holder.notifyIsMimeMultipart( );
			input = new MultipartInputStream( input, boundary.getBytes( ) );
			minput = (MultipartInputStream)input;
			while ( minput.nextInputStream( ) )
			{
				partno++;
				holder.notifyBeginPartParsing( partno );
				parse( true );
				holder.notifyBeginBodyParsing( partno );
				holder.notifyBodyData( partno, input );
				holder.notifyEndBodyParsing( partno );
				holder.notifyEndPartParsing( partno );
			}
		}
		holder.notifyEndMailParsing( );		
	}

	boolean cached = false;

	public int read() throws IOException
	{
		if ( cached )
			cached = false;
		else
			ch = input.read( );
		return ch;
	}

	public void unread( int ch )
	{
		if ( cached )
			throw new RuntimeException( "cannot unread more then once !" );
		this.ch = ch;
		cached = true;
	}

	/**
	 * Get the message body, as an input stream.
	 * 
	 * @return The input stream used by the parser to get data, after a call to
	 *         <code>parse</code>, this input stream contains exactly the
	 *         body of the message.
	 */

	public InputStream getInputStream()
	{
		return input;
	}

	/**
	 * Create an instance of the MIMEParser class.
	 * 
	 * @param in
	 *            The input stream to be parsed as a MIME stream.
	 * @param factory
	 *            The factory used to create MIME header holders.
	 */

	public MimeParser(InputStream input, MimeMessageHandler mail)
	{
		this.input = input;
		this.holder = mail;
	}

}