package de.fhg.fokus.ims.core;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import javax.ims.ServiceClosedException;
import javax.ims.core.Message;
import javax.ims.core.Subscription;
import javax.ims.core.SubscriptionListener;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

public class Unsubscriber implements Runnable, SubscriptionListener
{	
	private static Logger LOGGER = LoggerFactory.getLogger(Unsubscriber.class);
	
	private Object syncRoot = new Object();
	private SubscriptionContainer subscriptions;
	private int activeUnsubscribings = 0;

	public Unsubscriber(SubscriptionContainer subscriptions)
	{
		this.subscriptions = subscriptions;
	}

	public void run()
	{
		SubscriptionImpl[] subscriptions = (SubscriptionImpl[]) this.subscriptions.getSubscriptions().toArray(new SubscriptionImpl[this.subscriptions.getSubscriptions().size()]);

		for (int i = 0; i < subscriptions.length; i++)
		{
			subscriptions[i].setListener(this);

			if (!subscriptions[i].isIncomingSubscription() && subscriptions[i].getState() == Subscription.STATE_ACTIVE)
			{
				try
				{
					synchronized (syncRoot)
					{
						activeUnsubscribings++;
					}
					subscriptions[i].unsubscribe();
				} catch (ServiceClosedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		synchronized (syncRoot)
		{
			int retryCount = 0;
			while (activeUnsubscribings > 0)
			{
				try
				{
					syncRoot.wait(1000);					
					retryCount++;
					if (retryCount == 15)
						break;
				} catch (InterruptedException e)
				{
					LOGGER.error(e.getMessage(), e);
				}
			}
		}
	}

	public void subscriptionNotify(Subscription subscription, Message notify)
	{
		// TODO Auto-generated method stub

	}

	public void subscriptionStartFailed(Subscription subscription)
	{
		// TODO Auto-generated method stub

	}

	public void subscriptionStarted(Subscription subscription)
	{
		// TODO Auto-generated method stub

	}

	public void subscriptionTerminated(Subscription subscription)
	{
		synchronized (syncRoot)
		{
			activeUnsubscribings--;
		}
	}
}
