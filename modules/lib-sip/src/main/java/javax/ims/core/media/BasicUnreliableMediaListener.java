package javax.ims.core.media;

/**
 * A listener type for receiving notification of when BasicUnreliableMedia content is available.
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface BasicUnreliableMediaListener
{

    /**
     * Notifies the application when new content is available. 
     * The content can be retrieved by calling receive on the BasicUnreliableMedia.
     * 
     * @param media - the concerned BasicUnreliableMedia
     * 
     */
	public abstract void contentReceived(BasicUnreliableMedia media);
}
