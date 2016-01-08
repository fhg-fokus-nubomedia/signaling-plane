package de.fhg.fokus.ims.core;

import javax.ims.core.Session;
import javax.ims.core.SessionListener;
import javax.ims.core.Subscription;

/**
 * Extension of the {@link SessionListener} for new events
 * 
 * @author Andreas Bachmann (andreas.bachmann@fokus.fraunhofer.de)
 */
public interface SessionListener2 extends SessionListener
{
	/**
	 * Notifies on incoming {@link Subscription} send within the dialog of the session.
	 * 
	 * @param session The concerned session
	 * @param subscription The subscription which was send.
	 */
	void sessionSubscriptionReceived(Session session, Subscription subscription);
}
