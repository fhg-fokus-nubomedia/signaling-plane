package de.fhg.fokus.ims.core;

import javax.ims.core.Subscription;

public interface Subscription2 extends Subscription
{
	int STATE_RECEIVED = 4;
	
	
	void notify(byte[] content, String contentType);
	
	void terminate(byte[] content, String contentType);
}
