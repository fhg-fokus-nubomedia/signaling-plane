package de.fhg.fokus.ims.core;

import gov.nist.javax.sip.header.extensions.ReplacesHeader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import javax.ims.ImsException;
import javax.ims.core.Message;
import javax.ims.core.MessageBodyPart;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.Header;
import javax.sip.message.Request;
import javax.sip.message.Response;

import de.fhg.fokus.ims.core.utils.mime.MimeMessageHandler;
import de.fhg.fokus.ims.core.utils.mime.MimeParser;
import de.fhg.fokus.ims.core.utils.mime.MimeParserException;
/**
 * Implementation of the Message Interface.
 * 
 * This clas
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @author Andreas Bachmann (andreas.bachmann@fokus.fraunhofer.de)
 */
public class MessageImpl implements Message
{
	private static ArrayList inaccessibleHeaders = new ArrayList();

	static
	{
		inaccessibleHeaders.add("Accept-Contact");
		inaccessibleHeaders.add("Authentication-Info");
		inaccessibleHeaders.add("Authorization");
		inaccessibleHeaders.add("Call-ID");
		inaccessibleHeaders.add("CSeq");
		inaccessibleHeaders.add("Event");
		inaccessibleHeaders.add("Max-Forwards");
		inaccessibleHeaders.add("Min-Expires");
		inaccessibleHeaders.add("Proxy-Authenticate");
		inaccessibleHeaders.add("Proxy-Authorization");
		inaccessibleHeaders.add("P-Associated-URI");
		inaccessibleHeaders.add("RAck");
		inaccessibleHeaders.add("Record-Route");
		inaccessibleHeaders.add("Refer-To");
		inaccessibleHeaders.add("Replaces");
		inaccessibleHeaders.add("RSeq");
		inaccessibleHeaders.add("Security-Client");
		inaccessibleHeaders.add("Security-Server");
		inaccessibleHeaders.add("Security-Verify");
		inaccessibleHeaders.add("Service-Route");
		inaccessibleHeaders.add("SIP-ETag");
		inaccessibleHeaders.add("SIP-If-Match");
		inaccessibleHeaders.add("Via");
		inaccessibleHeaders.add("WWW-Authenticate");
	}

	/**
	 * Identifier for this method
	 */
	private int id;

	/**
	 * Method - only valid if request
	 */
	private String method;

	/**
	 * The reason phrase of the response
	 */
	private String reasonPhrase;

	/**
	 * The status code of the response
	 */
	private int statusCode;

	/**
	 * The body parts
	 */
	private Vector messageBodyParts;

	/**
	 * The message state holder
	 */
	private int state;

	/**
	 * The response if this wraps a response
	 */
	private Response response;

	/**
	 * The request if this wraps a request
	 */
	private Request request;

	/**
	 * The headers if this is a new message which has not been send until now
	 */
	private Hashtable headers;

	public MessageImpl(int identifier, String method)
	{
		headers = new Hashtable();
		this.id = identifier;
		this.method = method;
		this.state = STATE_UNSENT;
	}

	public MessageImpl(int identifier, Request request)
	{
		this.id = identifier;
		this.method = request.getMethod();
		this.request = request;
		try
		{
			setContent(request);
		} catch (IllegalStateException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImsException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public MessageImpl(int identifier, Response response)
	{
		this.id = identifier;
		this.method = ((CSeqHeader) response.getHeader("CSeq")).getMethod();
		this.statusCode = response.getStatusCode();
		this.reasonPhrase = response.getReasonPhrase();
		this.response = response;
		try
		{
			setContent(response);
		} catch (IllegalStateException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ImsException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ------ interface methods ------//

	public String getMethod()
	{
		return method;
	}

	public String getReasonPhrase()
	{
		return reasonPhrase;
	}

	public int getState()
	{
		return state;
	}

	public void setState(int state)
	{
		this.state = state;
	}

	public int getStatusCode()
	{
		return statusCode;
	}

	public String[] getHeaders(String key) throws ImsException
	{
		if (inaccessibleHeaders.contains(key))
			throw new ImsException("MessageImpl getHeaders: " + key + " is not accessible. ");

		return getInternalHeaders(key);
	}

	public void addHeader(String key, String value) throws IllegalStateException, ImsException, IllegalArgumentException
	{
		if (inaccessibleHeaders.contains(key))
			throw new ImsException("MessageImpl getHeaders: " + key + "is not accessible. ");

		if (state != STATE_UNSENT)
			throw new IllegalStateException("Message state MUST be UNSENT");

		internalAddHeader(key, value);
	}

	public MessageBodyPart[] getBodyParts()
	{
		if (messageBodyParts == null)
			return new MessageBodyPart[0];
		MessageBodyPart bodyParts[] = new MessageBodyPart[messageBodyParts.size()];
		messageBodyParts.copyInto(bodyParts);
		return bodyParts;
	}

	public MessageBodyPart createBodyPart() throws IllegalStateException, ImsException
	{
		if (state != STATE_UNSENT)
			throw new IllegalStateException("Message state MUST be UNSENT");

		MessageBodyPartImpl bodyPart = new MessageBodyPartImpl(this);
		if (messageBodyParts == null)
			messageBodyParts = new Vector();

		messageBodyParts.add(bodyPart);
		return bodyPart;
	}

	// ------ helper ------//

	public Header getHeader(String key)
	{
		if (response != null)
			return response.getHeader(key);

		if (request != null)
			return request.getHeader(key);

		return (Header) headers.get(key);
	}

	public String[] getInternalHeaders(String key)
	{
		ArrayList result = new ArrayList();
		ListIterator iterator = null;
		if (response != null)
			iterator = response.getHeaders(key);
		else if (request != null)
			iterator = request.getHeaders(key);
		else
		{
			String s = (String) headers.get(key);
			if (s == null)
				return new String[0];
			else
				return new String[]
				{ s };
		}

		while (iterator.hasNext())
		{
			String s = iterator.next().toString();
			result.add(s.substring(s.indexOf(':') + 1).trim());
		}

		return (String[]) result.toArray(new String[result.size()]);
	}
	
	public Header getInternalHeader(String key)
	{
		if (response != null)
			return response.getHeader(key);
		else if (request != null)
			return request.getHeader(key);
		else
			return null;
	}

	public void internalAddHeader(String key, String value)
	{
		if (headers == null)
			headers = new Hashtable();
		headers.put(key, value);
	}

	public void internalAddHeader(String value)
	{
		int i = value.indexOf(":");

		headers.put(value.substring(0, i), value.substring(i + 2));
	}

	public void internalAddHeader(ReplacesHeader rph)
	{
		
	}
	
	public int getIdentifier()
	{
		return id;
	}

	public void setIdentifier(int identifier)
	{
		this.id = identifier;
	}

	public MessageBodyPart createBodyPart(byte[] stream, boolean in, String contentheader, String contentType)
	{
		if (stream == null)
			stream = new byte[0];

		MessageBodyPartImpl bodyPart;
		if (in)
		{
			bodyPart = new MessageBodyPartImpl(this, new ByteArrayInputStream(stream), contentheader, contentType);
			bodyPart.setContent(stream);
		} else
		{
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();

			try
			{
				outStream.write(stream);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			bodyPart = new MessageBodyPartImpl(this, outStream);
			bodyPart.setHeader(contentheader, contentType);
			bodyPart.setContent(stream);
		}
		if (messageBodyParts == null)
			messageBodyParts = new Vector();

		messageBodyParts.add(bodyPart);

		return bodyPart;
	}

	public void clearBodyParts()
	{
		if (messageBodyParts != null)
			messageBodyParts.clear();
	}

	public int getMethodInt(String method) throws IllegalArgumentException
	{
		if ("ACK".equals(method))
			return 1;
		if ("BYE".equals(method))
			return 2;
		if ("CANCEL".equals(method))
			return 3;
		if ("INFO".equals(method))
			return 4;
		if ("INVITE".equals(method))
			return 5;
		if ("MESSAGE".equals(method))
			return 13;
		if ("NOTIFY".equals(method))
			return 6;
		if ("OPTIONS".equals(method))
			return 7;
		if ("PRACK".equals(method))
			return 8;
		if ("PUBLISH".equals(method))
			return 9;
		if ("REFER".equals(method))
			return 14;
		if ("REGISTER".equals(method))
			return 10;
		if ("SUBSCRIBE".equals(method))
			return 11;
		if ("UPDATE".equals(method))
			return 12;
		else
			throw new IllegalArgumentException("Message:getMethodInt(): Unknown method (" + method + ")!");
	}

	public Map getHeaders()
	{
		return headers;
	}

	private void setContent(javax.sip.message.Message sipMessage) throws ImsException
	{
		if (sipMessage == null)
			return;

		byte[] content = sipMessage.getRawContent();

		ContentTypeHeader cType = (ContentTypeHeader) sipMessage.getHeader(ContentTypeHeader.NAME);
		if (cType != null && content != null)
		{

			String contentType = cType.getContentType();
			if ("multipart".equals(contentType))
			{
				try
				{
					messageBodyParts = new Vector();
					parseMultipartMessage(content, cType);
				} catch (Exception e)
				{
					throw new ImsException("Can't parse multipart body! Reason: " + e.getMessage());
				}
			} else
			{
				contentType = contentType + "/" + cType.getContentSubType();
				createBodyPart(content, true, ContentTypeHeader.NAME, contentType);
			}
		}
	}

	public Request getRequest()
	{
		return request;
	}

	public Response getResponse()
	{
		return response;
	}

	private void parseMultipartMessage(byte[] content, ContentTypeHeader contentHeader) throws MimeParserException, IOException
	{
		String boundary = contentHeader.getParameter("boundary");

		MimeParser parser = new MimeParser(new ByteArrayInputStream(content), new MimeMessageHandler()
		{
			MessageBodyPartImpl part = null;

			public void notifyBeginPartParsing(int partno) throws MimeParserException, IOException
			{
				part = new MessageBodyPartImpl(MessageImpl.this);
			}

			public void notifyHeader(int partno, String name, byte[] buf, int off, int len) throws MimeParserException
			{
				part.putHeader(name, new String(buf, off, len));
			}

			public void notifyBodyData(int partno, InputStream input) throws MimeParserException, IOException
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] b = new byte[256];
				int i = input.read(b);
				while (i > -1)
				{
					out.write(b, 0, i);
					i = input.read(b);
				}

				part.setContent(out.toByteArray());
			}

			public void notifyEndPartParsing(int partno) throws MimeParserException, IOException
			{
				messageBodyParts.add(part);
				part = null;
			}
		});

		parser.parse(boundary);
	}

	
}
