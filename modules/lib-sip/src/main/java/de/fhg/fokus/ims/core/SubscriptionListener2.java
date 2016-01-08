package de.fhg.fokus.ims.core;

import javax.ims.core.Subscription;
import javax.ims.core.SubscriptionListener;

public interface SubscriptionListener2 extends SubscriptionListener
{	
	void notifyDelivered(Subscription subscription);
	
	void notifyDeliveredFailed(Subscription subscription);
}
