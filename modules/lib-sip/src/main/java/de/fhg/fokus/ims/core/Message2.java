package de.fhg.fokus.ims.core;

import javax.ims.core.Message;

public interface Message2 extends Message
{
	/**
	 * Incoming subscription started
	 */
	int SUBSCRIPTION_START = 12;
	
	/**
	 * Outgoing notify
	 */
	int SUBSCRIPTION_NOTIFY = 13;
}
