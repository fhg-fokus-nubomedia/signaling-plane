package javax.ims.core;

/**
 * This listener type is used to notify the application about subscription status and event state of the subscribed event package.
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface SubscriptionListener
{   
    /**
     *  Notifies the application that the subscription was successfully started.
     *  The Subscription has transited to STATE_ACTIVE.
     * 
     * @param subscription - the concerned Subscription
     */
    public abstract void subscriptionStarted(Subscription subscription);

    /**
     * Notifies the application that the subscription was not successfully started.
     * The Subscription has transited to STATE_INACTIVE. 
     * 
     * @param subscription - the concerned Subscription
     */
    public abstract void subscriptionStartFailed(Subscription subscription);

    /**
     * 
     * Notifies the application that a subscription was terminated.
     * The Subscription has transited to STATE_INACTIVE. 
     * 
     * @param subscription
     */
    public abstract void subscriptionTerminated(Subscription subscription);
    
    /**
     * Notifies the application of published event state.
     * 
     * @param subscription - the concerned Subscription
     * @param notify - event state of the subscribed event package
     */
    public void subscriptionNotify(Subscription subscription, Message notify);
}
