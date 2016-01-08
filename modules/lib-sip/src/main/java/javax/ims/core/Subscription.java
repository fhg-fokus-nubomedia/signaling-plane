package javax.ims.core;

import javax.ims.ServiceClosedException;

/**
 * <p>A Subscription is used for subscribing to event state from a remote endpoint. The subscription will be refreshed periodically until unsubscribe is called.
 * 
 * <p>The life cycle consist of three states, STATE_INACTIVE, STATE_PENDING and STATE_ACTIVE.
 *
 * <p>A new Subscription starts in STATE_INACTIVE and when subscribe is called the state transits to STATE_PENDING and remains there until a response arrives from the remote endpoint.
 *
 * <p>In STATE_ACTIVE, unsubscribe can be called to cancel the subscription and the state will then transit to STATE_PENDING and remain there until a response arrives from the remote endpoint. 
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @version 1.0
 */

public interface Subscription  extends ServiceMethod
{
	/** The Subscription is active.	 */
	public static final int STATE_ACTIVE = 3; 
	/** The Subscription is not active. */
	public static final int STATE_INACTIVE = 1;
	/** A Subscription request is sent and the IMS engine is waiting for a response. */
	public static final int STATE_PENDING = 2;

	/**
	 * Returns the event package corresponding to this Subscription.
	 * 
	 * @return - the event package
	 */
	public String getEvent();

	/**
	 * Returns the current state of the state machine of the Subscription.
	 * 
	 * @return - the current state
	 */
	public int getState();

	/**
	 * Sets a listener for this Subscription, replacing any previous
	 * SubscriptionListener.
	 * 
	 * @param listener - the listener to set, or null
	 */
	public void setListener(SubscriptionListener listener);

	/**
	 * Sends a subscription request. 
	 * The Subscription will transit to STATE_PENDING  after calling this method.
	 * 
     * @throws IllegalStateException - if the Subscription is not in STATE_INACTIVE 
     * @throws ServiceClosedException - if the Service is closed
	 */
	public void subscribe()throws IllegalStateException, ServiceClosedException;

	/**
	 * Terminates this subscription. 
	 * The Subscription will transit to STATE_PENDING  after calling this method.
	 * 
	 * @throws ServiceClosedException  - if the Service is closed
	 */
	public void unsubscribe() throws ServiceClosedException;

}
