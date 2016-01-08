package de.fhg.fokus.ims.core;

import de.fhg.fokus.ims.core.media.StreamMediaBase;


public abstract class StreamMediaFactoryBase
{
	
	public abstract StreamMediaBase createMedia(SessionImpl impl);
	
	
	
}
