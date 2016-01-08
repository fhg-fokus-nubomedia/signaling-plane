package de.fhg.fokus.ims.core.media;

import javax.ims.core.media.StreamMedia;

public interface StreamMedia2 extends StreamMedia
{
	int HOLDSTATE_NONE = 0;
	int HOLDSTATE_LOCAL = 1;
	int HOLDSTATE_REMOTE = 2;
	
	boolean isTelephoneEventEnabled();
	
	void setTelephoneEventEnabled(boolean enabled);
	
	void startTelephoneEvent(int number, int volume);
	
	void stopTelephoneEvent();
	
	void setHoldState(int state);
}
