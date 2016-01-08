package de.fhg.fokus.ims.core.media;

public class MediaPreparationException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public MediaPreparationException(Throwable e)
	{
		super(e);
	}
	
	public MediaPreparationException(String message)
	{
		super(message);
	}
	
	public MediaPreparationException(String message, Throwable e)
	{
		super(message, e);
	}
}
