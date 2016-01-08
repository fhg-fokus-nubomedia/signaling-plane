package de.fhg.fokus.ims.core;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.sip.header.CSeqHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * In memory log for sip messages
 * 
 * @author Andreas Bachmann (andreas.bachmann@fokus.fraunhofer.de)
 * 
 */
public class SIPLog
{
	public static class LogEntry
	{
		public static final int REQUEST = 1;
		public static final int RESPONSE = 2;

		public static final int REGISTER = 1;
		public static final int OPTIONS = 2;
		public static final int INVITE = 4;
		public static final int CANCEL = 8;
		public static final int ACK = 16;
		public static final int BYE = 32;
		public static final int UPDATE = 64;
		public static final int PRACK = 128;
		public static final int SUBSCRIBE = 256;
		public static final int NOTIFY = 512;
		public static final int PUBLISH = 1024;
		public static final int REFER = 2048;
		public static final int MESSAGE = 4096;
		public static final int INFO = 8192;
		
		private int flags = 0;
		private String timeStamp;
		private String message;
		private boolean incoming;
		private int code;

		public int getType()
		{
			return flags & 3;
		}

		public int getMethod()
		{
			return (flags << 16) >> 18;
		}

		public String getTimeStamp()
		{
			return timeStamp;
		}

		public String getMessage()
		{
			return message;
		}
		
		public boolean isIncoming()
		{
			return incoming;
		}
		
		public int getCode()
		{
			return code;
		}

		private LogEntry(int type, int method, int code, String timeStamp, String message, boolean isIncoming)
		{
			this.flags =  (method << 2) | type;
			this.timeStamp = timeStamp;
			this.message = message;
			this.incoming = isIncoming;
			this.code = code;
		}

		private LogEntry next;

		private void add(LogEntry newEntry)
		{
			next = newEntry;
			newEntry.next = null;
		}
	}

	private LogEntry head = null;
	private LogEntry tail = null;
	private int size;
	private int maxSize = 1000;

	public SIPLog(int maxSize)
	{
		if (maxSize > 0)
			this.maxSize = maxSize;
	}

	public synchronized void addRequest(Request request, boolean isIncoming)
	{
		LogEntry entry = new LogEntry(LogEntry.REQUEST, getMethod(request.getMethod()), -1, DateFormat.getDateTimeInstance().format(new Date()), request.toString(), isIncoming);
		addEntry(entry);
	}

	public synchronized void addResponse(Response response, boolean isIncoming)
	{
		String method = ((CSeqHeader)response.getHeader("CSeq")).getMethod();
		LogEntry entry = new LogEntry(LogEntry.RESPONSE, getMethod(method), response.getStatusCode(), DateFormat.getDateTimeInstance().format(new Date()), response.toString(), isIncoming);
		addEntry(entry);
	}

	public synchronized void clear()
	{
		head = null;
		tail = null;
		size = 0;
	}
	
	public synchronized int size()
	{
		return size;
	}
	
	private void addEntry(LogEntry entry)
	{
		if (head == null)
		{
			head = entry;
			tail = entry;
			size = 1;
		} else
		{
			if (size == maxSize)
			{
				head = head.next; // remove first
				size--;
			}
			tail.add(entry);
			tail = entry;
			size++;
		}
	}

	public Iterator iterator()
	{
		return new Iterator()
		{
			private LogEntry current = head;

			public void remove()
			{
			}

			public Object next()
			{
				current = current.next;
				return current;
			}

			public boolean hasNext()
			{
				return current.next != null;
			}
		};
	}

	public synchronized LogEntry[] toArray()
	{
		LogEntry[] res = new LogEntry[size];

		LogEntry current = head;
		int i = 0;

		while (current != null)
		{
			res[i] = current;
			i++;
			current = current.next;
		}

		return res;
	}
	
	private static int getMethod(String method)
	{	
		if (method.equals(Request.REGISTER))
			return LogEntry.REGISTER;
		if (method.equals(Request.ACK))
			return LogEntry.ACK;
		if (method.equals(Request.BYE))
			return LogEntry.BYE;
		if (method.equals(Request.CANCEL))
			return LogEntry.CANCEL;
		if (method.equals(Request.INFO))
			return LogEntry.INFO;
		if (method.equals(Request.INVITE))
			return LogEntry.INVITE;
		if (method.equals(Request.MESSAGE))
			return LogEntry.MESSAGE;
		if (method.equals(Request.NOTIFY))
			return LogEntry.NOTIFY;
		if (method.equals(Request.OPTIONS))
			return LogEntry.OPTIONS;
		if (method.equals(Request.PRACK))
			return LogEntry.PRACK;
		if (method.equals(Request.PUBLISH))
			return LogEntry.PUBLISH;
		if (method.equals(Request.REFER))
			return LogEntry.REFER;
		if (method.equals(Request.REGISTER))
			return LogEntry.REGISTER;
		if (method.equals(Request.SUBSCRIBE))
			return LogEntry.SUBSCRIBE;
		if (method.equals(Request.UPDATE))
			return LogEntry.UPDATE;
		
		return 0;
	}	
}
