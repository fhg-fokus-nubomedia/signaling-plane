package de.fhg.fokus.ims.core.auth;

public class AuthenticationException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AuthenticationException()
	{
		super();
	}

	public AuthenticationException(String s, Throwable throwable)
	{
		super(s, throwable);
	}

	public AuthenticationException(String s)
	{
		super(s);
	}

	public AuthenticationException(Throwable throwable)
	{
		super(throwable);
	}
}
