package de.fhg.fokus.ims.core;

import javax.ims.core.CoreService;
import javax.ims.core.CoreServiceListener;
import javax.ims.core.Subscription;

public interface CoreServiceListener2 extends CoreServiceListener
{
	void subscriptionReceived(CoreService coreservice, Subscription subscription);
}
