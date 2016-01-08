package javax.ims.core;

/**
 * This listener type is used to notify an application about events regarding a Reference.
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface ReferenceListener
{

	/**
	 * <p>Notifies the application that the reference was successfully delivered.
     * <p>This method is invoked at the endpoint that sent the reference request.
     * <p>The Reference has transited to STATE_REFERRING.
     * 
	 * @param reference -  the concerned Reference
	 */
	public void referenceDelivered(Reference reference);
	
	
	/**
	 * <p>Notifies the application that the reference was not successfully delivered.</p> 
	 * 
	 * <p>This method is invoked at the endpoint that sent the reference request.</p> 
	 * 
	 * <p>The Reference has transited to STATE_TERMINATED.</p> 
	 *  
	 * @param reference - the concerned Reference
	 */
	public void referenceDeliveryFailed(Reference reference); 
	 
	
	/**
	 * <p>Notifies the application that a reference has been terminated.
	 * <p>This method is invoked at the endpoint that sent the reference request and at the endpoint that received the reference request.
	 * <p>The Reference has transited to STATE_TERMINATED. 
	 * 
	 * @param reference - the concerned Reference
	 */
	public void referenceTerminated(Reference reference);
	
	
	/**
	 * <p>Notifies the application with status reports regarding the Reference.
	 * <p>This method is invoked at the endpoint that sent the reference request. 
	 * 
	 * @param reference - the concerned Reference
	 * @param notify - a status report regarding the Reference
	 */
	public void referenceNotify(Reference reference, Message notify);
}
