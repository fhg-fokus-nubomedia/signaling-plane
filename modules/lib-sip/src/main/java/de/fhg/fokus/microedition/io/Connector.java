package de.fhg.fokus.microedition.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import de.fhg.fokus.ims.core.IMSManager;

public class Connector
{
	public static final int READ = 1;
	public static final int WRITE = 2;
	public static final int READ_WRITE = 3;

	private Connector()
	{

	}

	public static Connection open(String name) throws IOException
	{
		return open(name, READ_WRITE);
	}

	public static Connection open(String name, int mode) throws IOException
	{
		return open(name, mode, false);
	}

	public static Connection open(String name, int mode, boolean timeouts) throws IOException
	{
		int i = name.indexOf(':');
		if (i < 0)
			throw new IOException("scheme not found");

		String scheme = name.substring(0, i);
		String path = name.substring(i + 1);

		if (scheme.equals("imscore"))
		{
			return createCoreservice(path);
		}
		throw new IOException("scheme not supported: " + scheme);
	}

	public static DataInputStream openDataInputStream(String name) throws IOException
	{
		return null;
	}

	public static DataOutputStream openDataOutputStream(String name) throws IOException
	{
		return null;
	}

	public static InputStream openInputStream(String name) throws IOException
	{
		return openDataInputStream(name);
	}

	public static OutputStream openOutputStream(String name) throws IOException
	{
		return openDataOutputStream(name);
	}

	private static Connection createCoreservice(String uri) throws IOException
	{
		StringTokenizer tokenizer = new StringTokenizer(uri, ";");
		String appId = null;
		String serviceId = null;
		String userId = null;
		while (tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken();

			if (appId == null)
			{
				appId = token;
				if (appId.startsWith("//"))
					appId = appId.substring(2);
			} else if (token.startsWith("serviceId="))
			{
				serviceId = token.substring("serviceId=".length());
			} else if (token.startsWith("userId="))
				userId = token.substring("userId=".length());
		}

		return IMSManager.getInstance().openCoreService(appId, serviceId, userId);
	}	
}
