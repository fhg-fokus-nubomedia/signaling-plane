package de.fhg.fokus.ims.core.media;

public class MediaCompletionException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MediaCompletionException(String s, Throwable throwable)
	{
		super(s, throwable);
	}

	public MediaCompletionException(String s)
	{
		super(s);
	}

	public MediaCompletionException(Throwable throwable)
	{
		super(throwable);
	}
}
