package javax.ims.core;

import javax.ims.ServiceClosedException;

/**
 * The Reference is used for referring a remote endpoint to a third party user or service. The Reference can be created and received both inside and outside of a session.
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface Reference extends ServiceMethod
{
	public static final int STATE_INITIATED = 1;
    public static final int STATE_PROCEEDING = 2;
    public static final int STATE_REFERRING = 3;
    public static final int STATE_TERMINATED = 4;
    
    /**
     * Returns the current state of this Reference.
     * @return the current state of this Reference
     */
    public int getState();


    /**
     * Accepts an incoming reference request. The Reference will transit to STATE_REFERRING after calling this method. 
     * 
     * @throws ServiceClosedException - if the Reference is not in STATE_PROCEEDING
     * @throws IllegalStateException - if the Service is closed
     */
    public abstract void accept() throws ServiceClosedException, IllegalStateException;

    /**
     * This method connects a service method with this reference and allows the IMS engine to send notifications regarding 
     * this reference to the endpoint that initiated this reference.
     * 
     * @param servicemethod - the ServiceMethod to connect
     * @throws IllegalStateException - if the Reference is not in STATE_REFERRING
     * @throws IllegalArgumentException  - if the serviceMethod argument is null
     */
    public abstract void connectReferMethod(ServiceMethod servicemethod) throws IllegalStateException , IllegalArgumentException ;

    /**
     * Returns the reference method to be used.
     * @return the reference method
     */
    public abstract String getReferMethod();
    
    /**
     * Returns the user identity to refer to
     * @return the user identity
     */
    public abstract String getReferToUserId();
    
    /**
     * 
     * @throws ServiceClosedException - if the Service is closed
     * @throws IllegalStateException - if the Reference is not in STATE_PROCEEDING
     */
    public abstract void refer() throws ServiceClosedException, IllegalStateException;

   /**
    * Rejects an incoming reference request.
    * The Reference will transit to STATE_TERMINATED after calling this method. 
    * 
    * @throws ServiceClosedException - if the Service is closed
    * @throws IllegalStateException - if the Reference is not in STATE_PROCEEDING
    */
    public abstract void reject() throws ServiceClosedException, IllegalStateException;
    
    /**
     * Sets a listener for this Reference, replacing any previous ReferenceListener. 
     * A null value is allowed and has the effect of removing any existing listener.
     * 
     * @param referencelistener the listener to set, or null
     */
    public abstract void setListener(ReferenceListener referencelistener);
   
}
