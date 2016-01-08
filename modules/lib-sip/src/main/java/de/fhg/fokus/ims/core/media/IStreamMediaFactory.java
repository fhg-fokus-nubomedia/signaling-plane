package de.fhg.fokus.ims.core.media;

import javax.ims.core.media.StreamMedia;

import de.fhg.fokus.ims.core.SessionImpl;

public interface IStreamMediaFactory
{
	StreamMedia createMedia(SessionImpl session);
}
