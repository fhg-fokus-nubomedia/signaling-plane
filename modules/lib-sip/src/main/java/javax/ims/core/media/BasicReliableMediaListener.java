package javax.ims.core.media;

/**
 * A listener type for receiving notification of when a connection error has occured on the stream(s).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface BasicReliableMediaListener
{

    /**
     * Notifies the application if the connection could not be established.
     * 
     * @param media - the concerned BasicReliableMedia
     */
	public abstract void connectionError(BasicReliableMedia media);
}
