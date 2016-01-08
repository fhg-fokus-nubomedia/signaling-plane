package javax.ims.core.media;

/**
 * <p>The StreamMedia represents a streaming media that can be rendered in realtime. The streaming type can be audio, video or both.
 *
 * <p>Setting up the media
 * <p>The following setter must be set in order to use the Media, otherwise an ImsException will be thrown in Session.start() or Session.update().
 * <blockquote>setSource</blockquote>

 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface StreamMedia extends Media
{
	public static final int 	QUALITY_HIGH 	= 3;
	public static final int 	QUALITY_LOW 	= 1;
	public static final int 	QUALITY_MEDIUM 	= 2;
	public static final int 	STREAM_TYPE_AUDIO = 1;
	public static final int 	STREAM_TYPE_AUDIO_VIDEO = 3;
	public static final int 	STREAM_TYPE_VIDEO = 2;
	
	
	
	/**
	 * Sets the source to capture media from.
	 * @param source - the source locator URI of media to be streamed 
	 * @throws IllegalStateException - if the Media is not in STATE_INACTIVE
	 * @throws IllegalArgumentException - if the source argument is invalid
	 */
	public void setSource(String source) throws IllegalStateException, IllegalArgumentException;

    /**
     * <p>Sets the stream type for the Media. This is used as an indication for the IMS engine regarding which stream type to use for this media.
     * <p>If the stream type has not been set, it will default to STREAM_TYPE_AUDIO_VIDEO.
     * <p>Note: The use case for this method is when the application adds a StreamMedia to a session with direction set to DIRECTION_RECEIVE. In this case, the IMS engine does not know which kind of StreamMedia to use in the negotiation. 
     * If the setSource method conflicts with the stream type, the setSource method has priotity.
     * 
     * @param type - the stream type
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE 
     * @throws IllegalArgumentException - if the type argument is not a valid identifier
     */
	public void setStreamType(int type) throws IllegalStateException, IllegalArgumentException;

    /**
     * <p>Returns the stream type for the Media.
     * <p>If the stream type has not been set, it will default to STREAM_TYPE_AUDIO_VIDEO.
     * 
     * @return the stream type
     */
	public int getStreamType();

	/**
	 * Returns a Player initiated to render the receiving part of this Media. The returned Player is in the REALIZED state.
	 * 
	 * @return a Player 
	 * @throws IllegalStateException - if the media's direction is DIRECTION_SEND or DIRECTION_INACTIVE 
     * @throws IllegalStateException - if the Media is not in STATE_ACTIVE
	 */
	public Object getReceivingPlayer() throws IllegalStateException;

    /**
     * Returns a Player initiated to source the sending part of this Media. The returned Player is in the REALIZED state.
     * 
     * @return a Player
     * @throws IllegalStateException - if the media's direction is DIRECTION_RECEIVE or DIRECTION_INACTIVE 
     * @throws IllegalStateException - if the Media is not in STATE_ACTIVE
     */
	public Object getSendingPlayer()throws IllegalStateException;

    /**
     * <p>Sets a preferred quality of the media stream. 
     * This method does not guarantee the desired quality but enables the platform during the media negotiation to select a propriate quality of service if possible.
	 * <p>Defaults to QUALITY_MEDIUM.
     * @param quality - the preferred quality
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE
     * @throws IllegalArgumentException - if the quality argument is invalid
     */
	public void setPreferredQuality(int quality) throws IllegalStateException,IllegalArgumentException;

}
