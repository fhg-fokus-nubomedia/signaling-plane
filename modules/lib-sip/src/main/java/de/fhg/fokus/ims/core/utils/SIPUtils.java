package de.fhg.fokus.ims.core.utils;

import gov.nist.javax.sip.header.AcceptContact;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;

import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.RequireHeader;
import javax.sip.header.SupportedHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Message;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * Provides some useful methods for sip string manipulation.
 * 
 * @author Motanga Alice
 */
public class SIPUtils
{
	public static final String SCHEME_SIP = "sip:";
	public static final String SCHEME_SIPS = "sips:";
	public static final String SCHEME_TEL = "tel:";

	public static String getRequestorIdentity(Request request)
	{
//		ExtensionHeader pAssertedIdentityHeader = (ExtensionHeader) request.getHeader("P-Asserted-Identity");
//
//		if (pAssertedIdentityHeader != null)
//		{
//			String pAssertedIdentityURI = pAssertedIdentityHeader.getValue();
//
//			int pos = pAssertedIdentityURI.indexOf('<');
//			if (pos > -1)
//				return pAssertedIdentityURI.substring(pos + 1, pAssertedIdentityURI.lastIndexOf('>'));
//			return pAssertedIdentityURI;
//		} else
//		{
			FromHeader fromHeader = (FromHeader) request.getHeader("From");
			SipURI fromURI = (SipURI) fromHeader.getAddress().getURI();
			return getIdentity(fromURI);
//		}

	}

	public static String getIdentity(SipURI sipURI)
	{
		StringBuffer sBuff = new StringBuffer(SCHEME_SIP);
		if (sipURI.getUser() != null)
		{
			sBuff.append(sipURI.getUser());
			sBuff.append("@");
		}
		sBuff.append(sipURI.getHost());

		return sBuff.toString();
	}

	/**
	 * Gets the content from the SIP message as string.
	 */
	public static String getContentString(Message message)
	{
		Object content = message.getContent();
		String text = "";
		if (content instanceof String)
		{
			text = (String) content;
		} else
		{
			if (content instanceof byte[])
			{
				text = new String((byte[]) content);
			} else
			{
				return null;
			}
		}
		return text.trim();
	}

	/**
	 * Extracts the domain from the URI.
	 * 
	 * @param uri
	 *            the URI string
	 * @return the URIs domain
	 */
	public static String getDomain(String uri)
	{
		if (uri.indexOf('@') == -1)
			return uri;
		else
			return uri.substring(uri.indexOf('@') + 1);
	}

	/**
	 * Determines the local IP address. Avoids returning the local host address
	 * (127.0.0.1).
	 * 
	 * @throws UnknownHostException
	 */
	public static String getLocalIPAddress() throws UnknownHostException
	{
		/*
		 * InetAddress[] addresses =
		 * InetAddress.getAllByName(InetAddress.getLocalHost
		 * ().getHostAddress()); String notLocalHost = null; for (int i=0;
		 * i<addresses.length;i++ ) if
		 * (!addresses[i].getHostAddress().startsWith("127")){ notLocalHost =
		 * addresses[i].getHostAddress(); break; } if (notLocalHost != null)
		 * return notLocalHost; else return
		 * InetAddress.getLocalHost().getHostAddress();
		 */
		InetAddress localHost = null;
		try
		{
			Enumeration localIfaces = null;
			try
			{
				localIfaces = NetworkInterface.getNetworkInterfaces();
			} catch (SocketException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			while (localIfaces.hasMoreElements())
			{
				NetworkInterface iFace = (NetworkInterface) localIfaces.nextElement();
				Enumeration addresses = iFace.getInetAddresses();

				// addresses loop
				while (addresses.hasMoreElements())
				{
					InetAddress address = (InetAddress) addresses.nextElement();
					// ignore link local addresses
					if (!address.isAnyLocalAddress() && !address.isLinkLocalAddress() && !address.isLoopbackAddress()
							&& !isWindowsAutoConfiguredIPv4Address(address))
					{
						if (isLinkLocalIPv4Address(address))
						{
							localHost = address;
						}

					}
					continue;
				}// addresses loop
			}// interfaces loop
			if (localHost != null)
			{
				System.out.println("Returning link local address ==" + localHost);
				return (String) localHost.getHostAddress();
			} else
			{
				System.out.println(InetAddress.getLocalHost().getHostAddress());
				return InetAddress.getLocalHost().getHostAddress();
			}
		} finally
		{

		}
	}

	/**
	 * Determines whether the address is the result of windows auto
	 * configuration. (i.e. One that is in the 169.254.0.0 network)
	 * 
	 * @param add
	 *            the address to inspect
	 * @return true if the address is autoconfigured by windows, false
	 *         otherwise.
	 */
	public static boolean isWindowsAutoConfiguredIPv4Address(InetAddress add)
	{
		return (add.getAddress()[0] & 0xFF) == 169 && (add.getAddress()[1] & 0xFF) == 254;
	}

	/**
	 * Determines whether the address is an IPv4 link local address. IPv4 link
	 * local addresses are those in the following networks:
	 * 
	 * 10.0.0.0 to 10.255.255.255 172.16.0.0 to 172.31.255.255 192.168.0.0 to
	 * 192.168.255.255
	 * 
	 * @param add
	 *            the address to inspect
	 * @return true if add is a link local ipv4 address and false if not.
	 */
	public static boolean isLinkLocalIPv4Address(InetAddress add)
	{
		byte address[] = add.getAddress();
		if ((address[0] & 0xFF) == 10)
			return true;
		if ((address[0] & 0xFF) == 172 && (address[1] & 0xFF) >= 16 && address[1] <= 31)
			return true;
		if ((address[0] & 0xFF) == 192 && (address[1] & 0xFF) == 168)
			return true;
		return false;
	}

	/**
	 * Returns the method of the response.
	 * 
	 * @param response
	 * @return SIP method of response
	 */
	public static String getMethod(Response response)
	{
		if (response == null)
			return null;

		CSeqHeader header = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
		if (header == null)
			return null;

		return header.getMethod();
	}

	/**
	 * Extracts the port from the given address (in the form "address:port").
	 * 
	 * @param s
	 *            the address where the port is to extract from
	 * @return the port, if there is one, else -1
	 */
	public static int getPort(String s)
	{
		if (s.indexOf(':') != -1)
		{
			return Integer.parseInt(s.substring(0, s.indexOf(':')));
		} else
		{
			return -1;
		}
	}

	/**
	 * Gets the content subtype the given mime type string (the part behind
	 * "/").
	 * 
	 * @param s
	 *            the string where the content subtype is to extract from
	 * @return the content subtype if there is a "/" in the give string, else
	 *         the string self
	 */
	public static String getSubType(String s)
	{
		if (s.indexOf('/') != -1 && s.indexOf('/') != s.length() - 1)
		{
			return s.substring(s.indexOf('/') + 1, s.length());
		} else
		{
			return s;
		}
	}

	/**
	 * Gets the content type the given mime type string (the part before "/").
	 * 
	 * @param s
	 *            the string where the content type is to extract from
	 * @return the content type if there is a "/" in the give string, else the
	 *         string self
	 */
	public static String getType(String s)
	{
		if (s.indexOf('/') != -1)
		{
			return s.substring(0, s.indexOf('/'));
		} else
		{
			return s;
		}
	}

	/**
	 * Extracts the username from the given URI.
	 * 
	 * @param s
	 *            the URI where the usename is to extract from
	 * @return the username
	 */
	public static String getUsername(String s)
	{
		String result = s;
		if (s.startsWith(SCHEME_SIP))
		{
			result = s.substring(4);
		}
		if (result.indexOf('@') != -1)
		{
			result = result.substring(0, result.indexOf('@'));
		} else
		{
			result = "";
		}
		return result;
	}

	/**
	 * Removes the header name from a header line.
	 * 
	 * @param s
	 *            the header string
	 * @return a header line without the beginning header name and ":"
	 */
	public static String getWithoutHeaderName(String s)
	{
		String result = s;
		if (result.indexOf(':') < result.length())
		{
			result = result.substring(result.indexOf(':') + 1);
		}
		if (result.charAt(0) == ' ')
		{
			result = result.substring(1);
		}
		return result;
	}

	/**
	 * Removes the "sip:" prefix from the beginning of the given URI.
	 * 
	 * @param s
	 *            the URI string
	 * @return the string without the "sip:" prefix
	 */
	public static String getWithoutSipPrefix(String s)
	{
		String result = s;
		if (s.startsWith(SCHEME_SIP))
		{
			result = s.substring(4);
		}
		return result;
	}

	/**
	 * Removes the "tel:" prefix from the beginning of the given URI.
	 * 
	 * @param s
	 *            the URI string
	 * @return the string without the "sip:" prefix
	 */
	public static String getWithoutTelPrefix(String s)
	{
		String result = s;
		if (s.startsWith(SCHEME_TEL))
		{
			result = s.substring(4);
		}
		return result;
	}

	/**
	 * Returns true if the message has a Require-header field of "100rel".
	 * 
	 * @param message
	 */
	public static boolean has100RelRequire(Message message)
	{
		ListIterator li = message.getHeaders(RequireHeader.NAME);
		while (li.hasNext())
		{
			RequireHeader header = (RequireHeader) li.next();
			if (header.getOptionTag().equalsIgnoreCase("100rel"))
				return true;
		}
		return false;
	}

	/**
	 * Returns true if the message has a Require-header field of "100rel".
	 * 
	 * @param message
	 */
	public static boolean has100RelSupported(Message message)
	{
		ListIterator li = message.getHeaders(SupportedHeader.NAME);
		while (li.hasNext())
		{
			SupportedHeader header = (SupportedHeader) li.next();
			if (header.getOptionTag().equalsIgnoreCase("100rel"))
				return true;
		}
		return false;
	}

	/**
	 * Returns true if the message has a content.
	 * 
	 * @param message
	 */
	public static boolean hasContent(Message message)
	{
		ContentLengthHeader contentLength = (ContentLengthHeader) message.getHeader(ContentLengthHeader.NAME);
		if (contentLength != null && contentLength.getContentLength() != 0)
			return true;
		else
			return false;
	}

	/**
	 * Examines if the given request is valide, checks for all mandatory
	 * headers.
	 * 
	 * @param request
	 *            the request to check
	 * @return null if all mandatory headers are available, an error description
	 *         else
	 */
	public static String isValidRequest(Request request)
	{
		String result = null;

		if (request.getHeader(CallIdHeader.NAME) == null)
			if (result == null)
				result = "CallId-Header";
			else
				result += ", CallId-Header";
		if (request.getHeader(CSeqHeader.NAME) == null)
			if (result == null)
				result = "CSeq-Header";
			else
				result += ", CSeq-Header";
		if (request.getHeader(ToHeader.NAME) == null)
			if (result == null)
				result = "To-Header";
			else
				result += ", To-Header";
		if (request.getHeader(FromHeader.NAME) == null)
			if (result == null)
				result = "From-Header";
			else
				result += ", From-Header";
		if (request.getHeader(ViaHeader.NAME) == null)
			if (result == null)
				result = "Via-Header";
			else
				result += ", Via-Header";
		/*
		 * if (request.getHeader(MaxForwardsHeader.NAME) == null) if (result ==
		 * null) result = "MaxForwards-Header"; else result += ",
		 * MaxForwards-Header";
		 */

		// check for correct contenttype header
		if (request.getContent() != null
				&& (request.getHeader(ContentTypeHeader.NAME) == null || ((ContentTypeHeader) request.getHeader(ContentTypeHeader.NAME)).getContentType() == null))
		{
			if (result == null)
				result = "ContentType-Header";
			else
				result += ", ContentType-Header";
		}

		if (result != null)
			result = "request without " + result;

		return result;
	}

	/**
	 * Examines if the given response is valide, checks for all mandatory
	 * headers.
	 * 
	 * @param response
	 *            the response to check
	 * @return null if all mandatory headers are available, an error description
	 *         else
	 */
	public static String isValidResponse(Response response)
	{
		String result = null;

		if (response.getHeader(CallIdHeader.NAME) == null)
			if (result == null)
				result = "CallId-Header";
			else
				result += ", CallId-Header";
		if (response.getHeader(CSeqHeader.NAME) == null)
			if (result == null)
				result = "CSeq-Header";
			else
				result += ", CSeq-Header";
		if (response.getHeader(ToHeader.NAME) == null)
			if (result == null)
				result = "To-Header";
			else
				result += ", To-Header";
		if (response.getHeader(FromHeader.NAME) == null)
			if (result == null)
				result = "From-Header";
			else
				result += ", From-Header";
		if (response.getHeader(ViaHeader.NAME) == null)
			if (result == null)
				result = "Via-Header";
			else
				result += ", Via-Header";

		// check for correct contenttype header
		if (response.getContent() != null
				&& (response.getHeader(ContentTypeHeader.NAME) == null || ((ContentTypeHeader) response.getHeader(ContentTypeHeader.NAME)).getContentType() == null))
		{
			if (result == null)
				result = "ContentType-Header";
			else
				result += ", ContentType-Header";
		}

		if (result != null)
			result = "request without " + result;
		return result;
	}

	/**
	 * Checks if the given URI is a valid SIP URI.
	 * 
	 * @param s
	 *            the URI to check
	 * @return true, if the URI starts with "sip:" and is longer than four
	 *         characters, else false
	 */
	public static boolean isValidSipURI(String s)
	{
		if (s != null && s.startsWith(SCHEME_SIP) && s.length() > 4)
		{
			return true;
		} else
		{
			return false;
		}
	}

	/**
	 * Removes the "pres:" prefix (if there is one) and adds a "sip:" to the
	 * beginning of the given URI.
	 * 
	 * @param s
	 *            the URI string
	 * @return a URI beginning with "sip:"
	 */
	public static String presToSip(String s)
	{
		String result = s;
		if (s.startsWith("pres:"))
		{
			result = SCHEME_SIP + s.substring(5);
		} else if (!s.startsWith("pres:"))
		{
			result = SCHEME_SIP + result;
		}
		return result;
	}

	/**
	 * Removes the "sip:" prefix (if there one) and adds a "pres:" to the
	 * beginning of the given URI.
	 * 
	 * @param s
	 *            the URI string
	 * @return a URI beginning with "pres:"
	 */
	public static String sipToPres(String s)
	{
		String result = s;
		if (s.startsWith(SCHEME_SIP))
		{
			result = "pres:" + s.substring(4);
		} else if (!s.startsWith("pres:"))
		{
			result = "pres:" + result;
		}
		return result;
	}

	/**
	 * Removes carriage return at the end.
	 */
	public static String stripCR(String s)
	{
		if (s.endsWith("\r"))
		{
			return s.substring(0, s.length() - 1);
		} else
			return s;
	}

	/**
	 * Removes the "<" and ">" at the begin and end of the given string.
	 * 
	 * @param s
	 *            the string which is to be changed
	 * @return the string without "<" and ">"
	 */
	public static String withoutLtGt(String s)
	{
		String result = s;
		if (result.indexOf('<') == 0)
		{
			result = result.substring(1);
		}
		if (result.indexOf('>') == result.length() - 1)
		{
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	/**
	 * Returns the given URI without port.
	 * 
	 * @param s
	 *            the URI to check
	 * @return true, if the URI starts with "sip:" and is longer than four
	 *         characters, else false
	 */
	public static String withoutPort(String s)
	{
		if (s.indexOf(':') != -1)
		{
			return s.substring(0, s.indexOf(':'));
		} else
		{
			return s;
		}
	}

	public static String getSipUri(String uri)
	{
		int i = uri.indexOf('<');

		int j = -1;

		if (i > -1)
		{
			j = uri.indexOf('?', i);
			if (j == -1)
				j = uri.indexOf('>');
		}

		if (i > -1)
		{
			if (j > -1)
				return uri.substring(i + 1, j);
			else
				return uri.substring(i + 1);
		} else
		{
			if (j > -1)
				return uri.substring(0, j);
			else
				return uri;
		}
	}
	
	/**
	 * Get the features tags from Contact header
	 * 
	 * @return Array of strings
	 */
	public static ArrayList getFeatureTags(Message request) {
		ArrayList tags = new ArrayList();
		
		// Read Contact header
		ContactHeader contactHeader = ((ContactHeader) request.getHeader("Contact"));
		if (contactHeader != null) {
	        for(Iterator i = contactHeader.getParameterNames(); i.hasNext();) {
	        	String pname = (String)i.next();
	        	String value = contactHeader.getParameter(pname);
        		if ((value == null) || (value.length() == 0)) {
        			tags.add(pname);	        		
	        	} else {
		        	String[] values = value.split(",");
		        	for(int j=0; j < values.length; j++) {
		        		String tag = values[j].trim();
		        		if (!tags.contains(tag)){
		        			tags.add(tag);
		        		}
		        	}
	        	}
	        }
		}
		
		// Read Accept-Contact header
		AcceptContact acceptHeader = (AcceptContact)request.getHeader("Accept-Contact");
		if (acceptHeader == null) {
			// Check contracted form
			acceptHeader = (AcceptContact)request.getHeader("a");
		}
		if (acceptHeader != null) {
			String[] pnames = acceptHeader.getValue().split(";");
			if (pnames.length > 1) {
				// Start at index 1 to bypass the address
				for(int i=1; i < pnames.length; i++) {
					if (!tags.contains(pnames[i])){
						tags.add(pnames[i]);
					}
				}
			}
		}		
		
		return tags;
	}	 
}
