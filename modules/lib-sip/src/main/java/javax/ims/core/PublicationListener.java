package javax.ims.core;

/**
 * This listener type is used to notify the application the status of requested publications.
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface PublicationListener
{

    /**
     * Notifies the application that the publication request was successfully delivered.
     * The Publication has transited to STATE_ACTIVE. 
     * 
     * @param publication - the concerned Publication
     */
	public void publishDelivered(Publication publication);
	
	/**
	 * Notifies the application that the publication request was not successfully delivered. 
	 * The Publication has transited to either STATE_ACTIVE or STATE_INACTIVE. 
	 * 
	 * @param publication - the concerned Publication
	 */
    public void publishDeliveredFailed(Publication publication);

    /**
     * Notifies the application that the publication was terminated.
     * The Publication has transited to STATE_INACTIVE. 
     * 
     * @param publication - the concerned Publication
     */
    public void publicationTerminated(Publication publication);
}
