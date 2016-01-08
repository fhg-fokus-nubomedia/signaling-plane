package de.fhg.fokus.ims.core;

import javax.ims.ImsException;
import javax.ims.core.Session;
import javax.ims.core.Subscription;


/**
 * Extension of the {@link Session} interface. 
 * 
 * This allows applications to get addional functionalities and information to be retrieved 
 * from the session.
 * 
 * The default implementation of the Session interface will also implement Session2
 * 
 * @author Andreas Bachmann (andreas.bachmann@fokus.fraunhofer.de)
 */
public interface Session2 extends Session
{
	int UPDATE_UNCHANGED = 1;
    int UPDATE_MODIFIED = 2;
    
	/**
	 * Returns the Call-Id as session id from the underlying dialog
	 * @return The Call-Id
	 */
	String getSessionId();
	
	/**
	 * Creates a subscription within the dialog of the underlying session.
	 * 
	 * @param event The name of the event package
	 * @return	The created subscription
	 * @throws ImsException
	 */
	Subscription createSubscription(String event) throws ImsException;
	
	void sendProvisionalResponse(int statusCode, String message);

	String getFromTag();

	String getToTag();
	
	void setAutomaticMediaHandling(boolean param);
	
	int getUpdateState();
}
