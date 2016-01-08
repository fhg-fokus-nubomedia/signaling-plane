package de.fhg.fokus.ims.core.media;

import javax.ims.core.media.StreamMedia;
import de.fhg.fokus.ims.core.SessionImpl;

public abstract class StreamMediaBase extends MediaImpl implements StreamMedia
{
	private int streamType = STREAM_TYPE_AUDIO;
	private int qualityType = QUALITY_HIGH;
	private String source;
	
	public StreamMediaBase(SessionImpl session)
	{
		super(session);	
	}

	public abstract Object getReceivingPlayer();

	public abstract Object getSendingPlayer();

	public final int getStreamType()
	{
		return streamType;
	}
	
	public void setStreamType(int streamType)
	{
		this.streamType  = streamType;
	}

	public final void setPreferredQuality(int quality)
	{
		if (getState() != STATE_INACTIVE)
			throw new IllegalStateException("StreamMediaImpl.setPreferredQuality(): Media not in STATE_INACTIVE.");

		if (quality == 0)
			qualityType = StreamMedia.QUALITY_MEDIUM;
		else
			qualityType = quality;
	}
	
	protected final int getPreferedQuality()
	{
		return qualityType;
	}

	public void setSource(String source)
	{
		this.source = source;
	}
	
	protected final String getSource()
	{
		return source;
	}
	
	public void setRemoteMediaDescriptor(MediaDescriptorImpl remoteMD)
	{
		super.setRemoteMediaDescriptor(remoteMD);
		if (localMD == null)
		{
			if (remoteMD.getMediaField().getMedia().equals("video"))
				streamType = STREAM_TYPE_VIDEO;
			else if (remoteMD.getMediaField().getMedia().equals("audio"))
				streamType = STREAM_TYPE_AUDIO;
		}
	}	
}
