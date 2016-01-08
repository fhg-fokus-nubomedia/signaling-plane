package de.fhg.fokus.ims.core.media;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import javax.ims.core.media.Media;
import javax.ims.core.media.MediaDescriptor;

import de.fhg.fokus.ims.core.SessionImpl;

/**
 * Implementation of the {@link Media} Interface.
 * 
 * This class forms the base class of all media implementations. It provides a
 * set of methods derived classes can override to perform actions on receiving
 * modifications.
 * 
 * There are three important methods prepare, unprepare and complete. These
 * methods are called by the session and the implementation are responsible for
 * preparation according to the local system state and for modification of the
 * SDP content which will be send to the remote party.
 * 
 * The grafic shows a typical session setup
 * 
 * <pre>
 * -----						-----
 * | A |						| B |
 * -----						-----
 * 	 |							  |
 * prepare 					  |
 *  |        --&gt; INVITE --&gt;      |
 *   | 						   prepare
 *   |						   complete
 *   |      &lt;-- 200 OK &lt;--        |
 *  complete					  |
 *   |      --&gt;   ACK   --&gt;       |
 *   |							  |
 * </pre>
 * 
 * 
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @author Andreas Bachmann <andreas.bachmann@fraunhofer.fokus.de>
 */
public class MediaImpl implements Media
{
	private static StackLogger logger = CommonLogger.getLogger(MediaImpl.class);


	/**
	 * Reference to the session this media object belongs to
	 */
	protected SessionImpl session;

	/**
	 * Holds the current state of the media object
	 */
	private int state;

	/**
	 * Holds the current update state
	 */
	private int updateState;

	protected int direction;
	protected int direction_back;

	// Local media descriptor - the current state
	protected MediaDescriptorImpl localMD;

	// Backup of the local state - refreshed if session is established
	protected MediaDescriptorImpl localMD_back;

	protected MediaDescriptorImpl localUpdateMD;

	// The initial remote media descriptor
	protected MediaDescriptorImpl remoteMD;

	// The current update descriptor from the remote side
	protected MediaDescriptorImpl remoteUpdateMD;

	protected MediaImpl mediaProposal;

	public MediaImpl(SessionImpl session)
	{
		mediaProposal = null;
		state = Media.STATE_INACTIVE;
		direction = Media.DIRECTION_SEND_RECEIVE;
		updateState = Media.UPDATE_UNCHANGED;
		this.session = session;
	}

	// ----
	// Interface methods
	// ---
	public int getDirection()
	{
		return direction;
	}

	public void setDirection(int direction)
	{
		if (state != Media.STATE_INACTIVE && state != Media.STATE_ACTIVE && state != Media.STATE_PROPOSAL)
			throw new IllegalStateException("setDirection(): Media state is wrong (" + state + ")! Must be INACTIVE, ACTIVE or PROPOSAL.");

		this.direction = direction;
		updateState = UPDATE_MODIFIED;
	}

	public MediaDescriptor[] getMediaDescriptors()
	{
		return new MediaDescriptor[]
		{ localMD };
	}

	public Media getProposal() throws IllegalStateException
	{
		return mediaProposal;
	}

	public void setProposal(MediaImpl prop)
	{
		this.mediaProposal = prop;
	}

	public int getState()
	{
		return state;
	}

	public int getUpdateState() throws IllegalStateException
	{
		return updateState;
	}

	public void setUpdateState(int updateModified)
	{
		this.updateState = updateModified;
	}

	// ---
	// end interface methods
	// ----

	/**
	 * Returns the media descriptor for this media object.
	 */
	public MediaDescriptor getDescriptor()
	{
		return localMD;
	}

	/**
	 * Sets the media descriptor for this object
	 * 
	 * @param localMD
	 */
	public void setDescriptor(MediaDescriptorImpl localMD)
	{
		this.localMD = localMD;
	}

	/**
	 * Returns the remote descriptor this media object was created with
	 * 
	 * @return
	 */
	public MediaDescriptorImpl getRemoteMediaDescriptor()
	{
		return remoteMD;
	}

	public void setRemoteMediaDescriptor(MediaDescriptorImpl remoteMD)
	{
		this.remoteMD = remoteMD;
	}

	public void setRemoteUpdateMediaDescriptor(MediaDescriptorImpl remoteMD)
	{
		this.remoteUpdateMD = remoteMD;
	}

	/**
	 * Called by session if to initialize the media for the local environment
	 * This will typically init devices, reserve ports and so on.
	 * 
	 * @throws MediaPreparationException
	 */
	public final void prepare() throws MediaPreparationException
	{
		// Delegate preparation
		onPrepare();

		setState(Media.STATE_PENDING);

		if (logger.isLoggingEnabled())
		{
			logger.logDebug("Preparing media with local media descriptor:");
			logger.logDebug("Media: {"+localMD.getMediaField()+"}");
			logger.logDebug("Connection: {"+localMD.getConnectionField()+"}");
			String[] attrs = localMD.getAttributes();

			if (attrs != null)
			{
				for (int i = 0; i < attrs.length; i++)
				{
					logger.logDebug("{"+attrs[i]+"}" );
				}
			}
		}
	}

	/**
	 * Unprepare will be called if an outgoing session was rejected by the
	 * remote party. This will allow implementations to clean up resource
	 * reservations
	 */
	public final void unprepare()
	{
		onUnprepare();

		setState(STATE_DELETED);
	}

	/**
	 * Complete is called to finish media creation. If this method is called
	 * this means the media setup has been accepted by the remote party or we
	 * accepted the offer and are able to create an answer. In any case
	 * {@link #getRemoteMediaDescriptor()} will return a not null value if this
	 * method is called.
	 * 
	 * @throws MediaCompletionException
	 */
	public final void complete() throws MediaCompletionException
	{
		if (logger.isLoggingEnabled())
		{
			logger.logDebug("Completing media with remote media descriptor:");
			logger.logDebug("Media: {"+ remoteMD.getMediaField()+"}");
			
			logger.logDebug("Connection: {"+ remoteMD.getSessionDescriptor().getConnectionField() + remoteMD.getConnectionField()+"}");
			String[] attrs = remoteMD.getAttributes();
			if (attrs != null)
			{
				for (int i = 0; i < attrs.length; i++)
				{
					logger.logDebug("{"+attrs[i]+"}");
				}
			}
		}

		// Setting the direction on base of the remote direction

		int remoteDirection = remoteMD.getDirection();
		direction = getReversedDirection(remoteDirection);
		localMD.setDirection(direction);

		// Delegate completion
		onComplete();

		// Finished
		setState(Media.STATE_ACTIVE);
		updateState = Media.UPDATE_UNCHANGED;

		backup();
	}

	public final void prepareUpdate() throws MediaPreparationException
	{
		onPrepareUpdate();
	}

	public final void unprepareUpdate()
	{
		onUnprepareUpdate();

		restore();

		setState(Media.STATE_ACTIVE);
		setUpdateState(Media.UPDATE_UNCHANGED);
	}

	public final void completeUpdate() throws MediaCompletionException
	{
		if (remoteUpdateMD == null)
		{
			remoteUpdateMD = (MediaDescriptorImpl) remoteMD.clone();
			remoteUpdateMD.setSessionDescriptor(session.getRemoteUpdateSessionDescriptor());
		}
		
		int remoteDirection = remoteUpdateMD.getDirection();
		direction = getReversedDirection(remoteDirection);
		localMD.setDirection(direction);

		onCompleteUpdate();

		remoteMD = remoteUpdateMD;
		remoteUpdateMD = null;

		// Finished
		setState(Media.STATE_ACTIVE);
		updateState = Media.UPDATE_UNCHANGED;

		backup();
	}

	public final void close()
	{
		logger.logDebug("Closing media");
		onClose();
		setState(Media.STATE_INACTIVE);
		if (localMD != null)
			localMD.setPort(0);
		logger.logDebug("Closed media");
	}

	protected void onPrepare() throws MediaPreparationException
	{

	}

	protected void onUnprepare()
	{

	}

	protected void onComplete() throws MediaCompletionException
	{

	}

	protected void onPrepareUpdate() throws MediaPreparationException
	{

	}

	protected void onUnprepareUpdate()
	{

	}

	protected void onCompleteUpdate() throws MediaCompletionException
	{

	}

	protected void onClose()
	{

	}

	public MediaImpl generateUpdateProposal(MediaDescriptor mediaDescriptor)
	{
		return null;
	}

	/**
	 * Revokes all changes to the media object
	 */
	public void restore()
	{
		direction = direction_back;
		localMD_back.restore(localMD);
		updateState = UPDATE_UNCHANGED;
	}

	/**
	 * Creates a backup of the current media settings
	 */
	public void backup()
	{
		direction_back = direction;

		localMD_back = new MediaDescriptorImpl(localMD.getSessionDescriptor(), this);
		localMD.backup(localMD_back);
		updateState = UPDATE_UNCHANGED;
	}

	public void setState(int state)
	{
		this.state = state;

		if (state == STATE_ACTIVE) // Media established - make backup from
			// current description
			backup();
	}

	/**
	 * Generates SDP out the current media settings
	 * 
	 * @param stringbuffer
	 */
	public void generateSDPContent(StringBuffer stringbuffer)
	{
		localMD.setDirection(direction);
		if (updateState == UPDATE_REMOVED || state == STATE_DELETED)
			localMD.setPort(0);
		localMD.generateSDPContent(stringbuffer);
	}

	public boolean hasPendingUpdate()
	{
		return updateState != UPDATE_MODIFIED;
	}

	public String toString()
	{
		return localMD == null ? "[]" : localMD.toString();
	}

	public void prepareRemoval()
	{
		localMD.setPort(0);
	}

	private static final int getReversedDirection(int remoteDirection)
	{
		if (remoteDirection == DIRECTION_SEND)
			return DIRECTION_RECEIVE;
		if (remoteDirection == DIRECTION_RECEIVE)
			return DIRECTION_SEND;
		else if (remoteDirection == DIRECTION_INACTIVE)
			return DIRECTION_INACTIVE;

		return DIRECTION_SEND_RECEIVE;
	}

}
