package de.fhg.fokus.ims.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.ims.ImsException;
import javax.ims.core.Message;
import javax.ims.core.MessageBodyPart;

/**
 * Implementation of the MessageBodyPart Interface.
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public class MessageBodyPartImpl implements MessageBodyPart 
{
	private ByteArrayInputStream inputStream;
	private ByteArrayOutputStream outputStream;
	private Hashtable headers;
	private MessageImpl message;
	private byte[] content;			
	
	public MessageBodyPartImpl(MessageImpl msg)
	{
		headers = new Hashtable();		
		message = msg;
	}
		
	public MessageBodyPartImpl(MessageImpl msg, ByteArrayInputStream inStream, String key, String value)
	{
		headers = new Hashtable();
		if(key != null && value != null)
			headers.put(key, value);
		inputStream = inStream;
		message = msg;
	}
	
	public MessageBodyPartImpl(MessageImpl msg, ByteArrayOutputStream outStream)
	{
		headers = new Hashtable();
		outputStream = outStream;		
		message = msg;
	}
	
	public String getHeader(String key) throws IllegalArgumentException 
	{
		return (String)headers.get(key);
	}
	
	public InputStream openContentInputStream() throws IOException 
	{
		return inputStream;
	}

	public OutputStream openContentOutputStream() throws IOException,
			IllegalStateException 
	{
		if(outputStream == null)
			outputStream = new ByteArrayOutputStream();
		
		return outputStream;
	}

	public void setHeader(String key, String value)
	{
		if(key == null || value == null)
			throw new IllegalArgumentException("setHeader() - key or value cannot be empty");
	
		if(message.getState() != Message.STATE_UNSENT)
			throw new IllegalStateException("MessasageBodyPartImpl: setHeader() - message must be in an unsent state");		
		
		try {
			headers.put(key, value);
			message.addHeader(key, value);
		} 
		catch (ImsException e) 
		{
			e.printStackTrace();
		}				
	}

	public void putHeader(String key, String value)
	{
		this.headers.put(key, value);
	}
	
	public void setContent(byte[] byteArray)
	{
		inputStream = new ByteArrayInputStream(byteArray);
		this.content = byteArray;
	}

	public byte[] getRawContent()
	{
		if (content == null)
			return outputStream.toByteArray();
		return content;
	}
	
	public ByteArrayOutputStream getOutputContent()
	{
		return outputStream;
	}
}
