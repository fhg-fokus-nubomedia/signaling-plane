package de.fhg.fokus.ims.core;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.SIPHeaderNames;
import gov.nist.javax.sip.header.extensions.SessionExpiresHeader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimerTask;

import javax.ims.ImsException;
import javax.ims.core.Capabilities;
import javax.ims.core.Message;
import javax.ims.core.MessageBodyPart;
import javax.ims.core.Reference;
import javax.ims.core.Session;
import javax.ims.core.SessionDescriptor;
import javax.ims.core.SessionListener;
import javax.ims.core.Subscription;
import javax.ims.core.media.Media;
import javax.ims.core.media.MediaDescriptor;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import de.fhg.fokus.ims.core.media.MediaCompletionException;
import de.fhg.fokus.ims.core.media.MediaDescriptorImpl;
import de.fhg.fokus.ims.core.media.MediaImpl;
import de.fhg.fokus.ims.core.media.MediaPreparationException;
import de.fhg.fokus.ims.core.sdp.ConnectionField;
import de.fhg.fokus.ims.core.sdp.OriginField;
import de.fhg.fokus.ims.core.utils.SDPUtils;
import de.fhg.fokus.ims.core.utils.Utils;

/**
 * Implementation of the Session Interface.
 * 
 * Added Session Timer Support (RFC 4028)
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @author Andreas Bachmann (andreas.bachmann@fraunhofer.fokus.de)
 */
public class SessionImpl extends ServiceMethodImpl implements Session, Session2
{
	private static final String CONTENT_TYPE_SDP = "application/sdp";

	private static final String DEFAULT_ENCODING = "utf-8";

	private static final String[] STATES = new String[]
	{ "INITIATED", "NEGOTIATING", "ESTABLISHING", "ESTABLISHED", "RENEGOTIATING", "REESTABLISHING", "TERMINATING", "TERMINATED" };
	
	private static StackLogger LOGGER = CommonLogger.getLogger(SessionImpl.class);


	public static final int ACK = 10;

	public static final int PRACK = 11;

	/**
	 * The session state
	 */
	private int state;

	/**
	 * Listener reference
	 */
	private SessionListener listener;

	/**
	 * The list of media objects
	 */
	private ArrayList mediaList = new ArrayList();

	private ArrayList mediaList_back = new ArrayList();
	/**
	 * The remote session descriptor
	 */
	private SessionDescriptorImpl localSD;

	/**
	 * A backup of the local session descriptor
	 */
	private SessionDescriptorImpl localSD_back;

	/**
	 * The local session descriptor
	 */
	private SessionDescriptorImpl remoteSD;

	private String sessionId;

	private int sessionExpireTimer = 0;

	private boolean isEmptyInvite = false;
	
	private boolean gotLastRequestTimeout = false;

	private Object syncRoot = new Object();
	/**
	 * Used for session timer
	 */
	protected TimerTask timerTask;

	private ReferenceImpl incomingRefer;

	private ReferenceImpl outgoingReference;

	private SubscriptionContainer subscriptions = new SubscriptionContainer();

	private String fromTag;

	private String toTag;

	private int updateState = UPDATE_UNCHANGED;

	private SessionDescriptorImpl remoteUpdateSD;

	private boolean isEmptyReInvite = false;
	
	
	private boolean automaticMediaHandling = true;

	private class SessionRefreshTask extends TimerTask
	{
		private final SessionImpl session;

		public SessionRefreshTask(SessionImpl session)
		{
			this.session = session;
		}

		public void run()
		{
			try
			{
				session.updateSessionTimer();
			} catch (IllegalStateException illegalstateexception)
			{
				LOGGER.logInfo("refreshing session failed");
			} catch (ImsException e)
			{
				LOGGER.logError("refreshing session failed", e);
			}
		}
	}

	/**
	 * Creates a new Session base on the remote URI.
	 * 
	 * This constructor will be used for outgoing sessions.
	 * 
	 * @param remoteUri
	 * @throws ImsException
	 */
	public SessionImpl(CoreServiceImpl coreService, SessionContainer container, String remoteUri, String localUri) throws ImsException
	{
		super(coreService, container, remoteUri, localUri);
		state = STATE_INITIATED;
		localSD = new SessionDescriptorImpl(this);
		sessionExpireTimer = IMSManager.getInstance().getSessionExpireTimer();
		setNextRequest(new MessageImpl(Message.SESSION_START, Request.INVITE));
	}

	/**
	 * Creates a new Session base on a request.
	 * 
	 * This constructor will be used for incoming sessions.
	 * 
	 * @param request
	 * @param serverTransaction
	 * @throws ImsException
	 */
	public SessionImpl(CoreServiceImpl coreService, SessionContainer container, Request request, String localUri) throws ImsException
	{
		super(coreService, container, request, localUri);

		MessageImpl message = new MessageImpl(Message.SESSION_START, request);
		setPreviousRequest(message);

		setState(STATE_NEGOTIATING);

		this.sessionId = ((CallIdHeader) request.getHeader(SIPHeaderNames.CALL_ID)).getCallId();
		this.fromTag = ((FromHeader) request.getHeader("From")).getTag();
		container.put(this);

		if (request.getContentLength().getContentLength() == 0)
		{
			isEmptyInvite = true;
		} else
		{
			remoteSD = createOrUpdateMediaList(message, mediaList);
		}
	}

	/**
	 * Returns the media list objects
	 */
	public Media[] getMedia() throws IllegalStateException
	{
		return (Media[]) mediaList.toArray(new Media[mediaList.size()]);
	}

	/**
	 * Returns the state of the session
	 */
	public int getState()
	{
		return state;
	}

	public int getUpdateState()
	{
		return updateState;
	}

	/**
	 * Returns the local session descriptor
	 */
	public SessionDescriptor getSessionDescriptor()
	{
		return localSD;
	}

	public SessionDescriptorImpl getRemoteSessionDescriptor()
	{
		return remoteSD;
	}

	public SessionDescriptorImpl getRemoteUpdateSessionDescriptor()
	{
		return remoteUpdateSD;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fhg.fokus.ims.core.Session2#getSessionId()
	 */
	public String getSessionId()
	{
		return sessionId;
	}

	/**
	 * sends a SIP INVITE message to the remote peer
	 */
	public void start() throws ImsException
	{
		if (state != STATE_INITIATED)
			throw new IllegalStateException("start(): state must be in the INITIATED state!");

		// Switch to state negotiation
		setState(STATE_NEGOTIATING);

		MessageImpl message = (MessageImpl) getNextRequest();
		// if (IMSManager.getInstance().isSessionTimerEnabled())
		// {
		// message.internalAddHeader(SIPHeaderNames.SUPPORTED, "timer");
		// message.internalAddHeader(SIPHeaderNames.SESSION_EXPIRES,
		// String.valueOf(sessionExpireTimer));
		// }
		// localSD.addAttribute(new AttributeField("nortpproxy", "yes") );

		OriginField o = localSD.getOrigin();
		localSD.setOrigin(new OriginField(o.getUserName(), o.getSessionId(), (new Long(SDPUtils.getNtpTime(new Date()))).toString(), o.getAddressType(), o
				.getAddress()));
		try
		{
			prepareLocalMedia(message);
		} catch (MediaPreparationException e)
		{
			// We can't prepare media - clean up everything
			if (mediaList.size() > 0)
			{
				for (int i = mediaList.size() - 1; i >= 0; i--)
				{
					MediaImpl media = (MediaImpl) mediaList.get(i);
					media.close();
					mediaList.remove(i);
				}
			}

			// Terminate session - it can't be reused here.
			setState(STATE_TERMINATED);

			// populate the error
			throw new ImsException(e.getMessage(), e);
		}

		// Send the request
		sendNextRequest();

		// Prepare core service for receive responses
		getContainer().put(this);
	}

	/**
	 * Receiver side start. Sends a SIP 200 OK to remote peer
	 */
	public void accept() throws IllegalStateException
	{
		if (state != STATE_NEGOTIATING & state != STATE_RENEGOTIATING)
			throw new IllegalStateException("Method accept() can be called only in the NEGOTIATING or RENEGOTIATING state! (Current state is " + state + ")");

		
		if (isEmptyInvite)
		{
			setState(STATE_ESTABLISHING);

			localSD = new SessionDescriptorImpl(this);

			try
			{
				// This is an incoming session - every media object has a remote
				// media descriptor assigned
				for (int i = 0; i < mediaList.size(); i++)
				{
					MediaImpl media = (MediaImpl) mediaList.get(i);
					// Prepare -> generate a local media descriptor for the
					// offer
					media.prepare();
				}
			} catch (Exception e)
			{
				LOGGER.logError(e.getMessage(), e);
				sendResponse(Response.NOT_ACCEPTABLE_HERE, "Not acceptable here", null, null);
				setState(Session.STATE_TERMINATED);
				return;
			}
		} else if (state == STATE_NEGOTIATING)
		{
			setState(STATE_ESTABLISHING);

			LOGGER.logDebug("accepting invitation");

			/* On automatic media handling send OK. Attention SDP have to be
			 * specified in front.*/
			if(!automaticMediaHandling) {
				
				/* On automatic media handling server and client side 
				 * have to manage their own media components. If any error
				 * happens here 
				 */
				sendResponse(Response.OK, "OK", null, null);
				return;
			}
			if (mediaList == null || mediaList.size() == 0 )
			{
				/* To do another status have to be send. */
				sendResponse(Response.OK, "OK", null, null);
				return;
			} else
			{
				localSD = new SessionDescriptorImpl(this, remoteSD.getOrigin());

				try
				{
					// This is an incoming session - every media object has a
					// remote media descriptor assigned
					for (int i = 0; i < mediaList.size(); i++)
					{
						MediaImpl media = (MediaImpl) mediaList.get(i);
						// Prepare -> generate a local media descriptor for the
						// offer
						media.prepare();
						// We know everything (remote and local) so we can
						// finish initialization
						media.complete();
					}
				} catch (Exception e)
				{
					LOGGER.logError(e.getMessage(), e);
					sendResponse(Response.NOT_ACCEPTABLE_HERE, "Not acceptable here!", null, null);
					setState(Session.STATE_TERMINATED);
					return;
				}
			}
		} else if (state == STATE_RENEGOTIATING)
		{
			setState(STATE_REESTABLISHING);

			LOGGER.logInfo("accepting update");

			if (isEmptyReInvite)
			{
				for (int i = 0; i < mediaList.size(); i++)
				{
					MediaImpl media = (MediaImpl) mediaList.get(i);
					media.setDirection(Media.DIRECTION_SEND_RECEIVE);
				}
			} else
			{
				try
				{
					for (int i = 0; i < mediaList.size(); i++)
					{
						MediaImpl media = (MediaImpl) mediaList.get(i);
						if (media.getState() == Media.STATE_PENDING)
						{
							// Added media
							media.prepare();
							media.complete();
						} else if (media.getState() == Media.STATE_ACTIVE)
						{
							// Changed
							if (media.getUpdateState() == Media.UPDATE_MODIFIED || updateState == Session2.UPDATE_MODIFIED)
							{
								// Prepare -> generate a local media descriptor
								// for
								// the offer
								media.prepareUpdate();
								// We know everything (remote and local) so we
								// can
								// finish initialization
								media.completeUpdate();
							} else if (media.getUpdateState() == Media.UPDATE_REMOVED)
							{
								media.setState(Media.STATE_DELETED);
							}
						} else if (media.getState() == Media.STATE_DELETED)
						{
							media.close();
						}
					}
				} catch (Exception e)
				{
					LOGGER.logError(e.getMessage(), e);
					sendResponse(Response.NOT_ACCEPTABLE_HERE, "Not acceptable here", null, null);
					setState(Session.STATE_ESTABLISHED);
					cleanUpMedia();
					return;
				}
			}
		}

		try
		{

			byte[] content = generateSDPContent().getBytes(DEFAULT_ENCODING);
			sendResponse(Response.OK, "OK", CONTENT_TYPE_SDP, content);
		} catch (UnsupportedEncodingException e)
		{
			LOGGER.logError(e.getMessage(), e);
		}

		cleanUpMedia();
	}

	/**
	 * sends a SIP Busy (486) request to the remote peer
	 */
	public void reject() throws IllegalStateException
	{
		if (state != STATE_NEGOTIATING && state != STATE_RENEGOTIATING)
		{
			throw new IllegalStateException("State must be NEGOTIATING or RENEGOTIATING!");
		}

		sendResponse(Response.BUSY_HERE, "Busy", null, null);

		if (state == STATE_NEGOTIATING)
		{
			this.state = STATE_TERMINATED;
			getContainer().remove(this);
			if (listener != null)
				listener.sessionStartFailed(this);
		} else if (state == STATE_RENEGOTIATING)
		{
			setState(STATE_ESTABLISHED);
			if (listener != null)
				listener.sessionUpdateFailed(this);
		}

		cleanUpMedia();
	}

	public Media createMedia(String type, int direction) throws ImsException
	{
		if (!(state == STATE_INITIATED | state == STATE_ESTABLISHED | (isEmptyInvite & state == STATE_NEGOTIATING)))
			throw new IllegalStateException( // TODO resourcen
					"Session is not in the appropriate state! Cannot create media! State must be INITIATED or ESTABLISHED. Current state is " + state);

		if (direction != Media.DIRECTION_INACTIVE && direction != Media.DIRECTION_RECEIVE && direction != Media.DIRECTION_SEND
				&& direction != Media.DIRECTION_SEND_RECEIVE)
			throw new IllegalArgumentException("Cannot create media. Wrong direction parameter! (" + direction + ")");

		try
		{
			MediaImpl media = coreService.createMedia(this, type);
			if(media == null)
				return null;
			media.setDirection(direction);
			mediaList.add(media);
			return media;
		} catch (Exception e)
		{
			throw new ImsException("Cannot create Media! Reason:" + e.getMessage(), e);
		}
	}

	public ReferenceImpl getOutgoingReference()
	{
		return outgoingReference;
	}

	public Capabilities createCapabilities()
	{
		return null;
	}

	public Reference createReference(String referToUserId, String referMethod)
	{
		if (state != Session.STATE_ESTABLISHED)
			throw new IllegalStateException("update() can be called only in the ESTABLISHED state! (Current state is " + state + ")");

		try
		{
			outgoingReference = new ReferenceImpl(getCoreService(), getCoreService().getReferences(), getDialog(), getRemoteUserId(), referMethod,
					referToUserId, coreService.getLocalUserId());
			return outgoingReference;
		} catch (ImsException e)
		{
			LOGGER.logError(e.getMessage());
			outgoingReference = null;
			return null;
		}
	}

	public Subscription createSubscription(String event) throws ImsException
	{
		SubscriptionImpl subscriptionImpl = new SubscriptionImpl(coreService, this.subscriptions, coreService.getLocalUserId(), getRemoteUserId(), event);

		subscriptionImpl.setDialog(getDialog());

		return subscriptionImpl;
	}

	public boolean hasPendingUpdates()
	{
		if (mediaList.size() != mediaList_back.size())
			return true;

		for (int i = 0; i < mediaList.size(); i++)
		{
			MediaImpl media = (MediaImpl) mediaList.get(i);
			if (media.hasPendingUpdate())
				return true;
		}
		return false;
	}

	public void removeMedia(Media media) throws IllegalArgumentException, IllegalStateException
	{
		if (!(state == STATE_ESTABLISHED | state == STATE_INITIATED))
			throw new IllegalStateException("removeMedia() can be called only in the ESTABLISHED or INITIATED state! (Current state is " + state + ")");
		if (!mediaList.contains(media))
			throw new IllegalArgumentException("Entered media is not a part of this session!");

		if (media.getState() != Media.STATE_ACTIVE && media.getState() != Media.STATE_INACTIVE)
			throw new IllegalStateException("removeMedia() can be called only for media in the ACTIVE or INACTIVE state! (Current state is " + media.getState()
					+ ")");

		if (media.getState() == Media.STATE_ACTIVE)
		{
			((MediaImpl) media).setUpdateState(Media.UPDATE_REMOVED);
		} else if (media.getState() == Media.STATE_INACTIVE)
		{
			((MediaImpl) media).setUpdateState(Media.STATE_DELETED);
			mediaList.remove(media);
		}
	}

	public void restore() throws IllegalStateException
	{
		if (state != Session.STATE_ESTABLISHED)
			throw new IllegalStateException("State must be ESTALBISHED!");

		if (localSD_back != null)
			localSD_back.restore(localSD);

		mediaList.clear();
		mediaList.addAll(mediaList_back);

		for (int i = mediaList.size(); i >= 0; i--)
		{
			MediaImpl media = (MediaImpl) mediaList.get(i);
			if (media.getState() == Media.STATE_INACTIVE)
				mediaList.remove(i);
			else
				media.restore();
		}
	}

	public void backup()
	{
		localSD_back = new SessionDescriptorImpl(this);

		localSD.backup(localSD_back);

		mediaList_back.clear();
		mediaList_back.addAll(mediaList);

		for (int i = 0; i < mediaList_back.size(); i++)
		{
			MediaImpl media = (MediaImpl) mediaList_back.get(i);
			media.backup();
		}
	}

	public void setListener(SessionListener listener)
	{
		if (listener == null)
			this.listener = null;
		else
			this.listener = listener;

	}

	/**
	 * sends a SIP BYE/CANCEL request to the remote peer
	 */
	public void terminate()
	{
		switch (state)
		{
		case STATE_INITIATED:
		case STATE_TERMINATING:
			state = STATE_TERMINATED;
			if (listener != null)
				listener.sessionTerminated(this);
			break;
		case STATE_RENEGOTIATING:
			// Stop and close all initiated media;
			cleanUpMedia();
			// send cancel
			setNextRequest(new MessageImpl(-1, "CANCEL"));
			sendNextRequest();
			break;
		case STATE_NEGOTIATING:
		case STATE_ESTABLISHING:
			// Stop and close all initiated media;
			cleanUpMedia();
			closeMedia();
			// send cancel
			setNextRequest(new MessageImpl(Message.SESSION_TERMINATE, "CANCEL"));
			sendNextRequest();

			setState(STATE_TERMINATED);
			break;
		default:
			try
			{
				// send bye
				setNextRequest(new MessageImpl(Message.SESSION_TERMINATE, "BYE"));
				setState(STATE_TERMINATING);
				sendNextRequest();
			} catch (Exception e)
			{
				LOGGER.logError(e.getMessage(), e);
				setState(STATE_TERMINATED);
			} finally
			{
				closeMedia();
			}
			break;
		}
	}

	/**
	 * Trys synchronized to terminate a session. If session is successfully synchronized closed,
	 * returns true. Otherwise if no synchronization is used or session termination failed return false
	 * @param sync
	 * @return boolean true if session successfully synchronized close, false if not
	 */
	public boolean terminate(boolean sync)
	{
		boolean result = false;
		if (sync)
		{
			syncRoot = new Object();

			terminate();

			if (syncRoot != null)
			{
				synchronized (syncRoot)
				{
					try
					{
						syncRoot.wait(60000);
						
						if(!gotLastRequestTimeout) {
							result = true;
						}
					} catch (InterruptedException e)
					{
					}
				}
			}

		} else 
			terminate();
		
		return result;
	}
	

	/**
	 * sends a SIP UPDATE request to the remote peer
	 */
	public void update() throws IllegalStateException, ImsException
	{
		if (state != STATE_ESTABLISHED)
			throw new IllegalStateException("update() can be called only in the ESTABLISHED state! (Current state is " + state + ")");

		if (!hasPendingUpdates())
			throw new IllegalStateException("update() cannot be called. There are no pending updates!");

		// Check if all sessions are marked to removed
		int nbrOfRemovedMedias = 0;
		for (int i = 0; i < mediaList.size(); i++)
		{
			MediaImpl media = ((MediaImpl) mediaList.get(i));
			if (media.getState() == Media.STATE_ACTIVE && media.getUpdateState() == Media.UPDATE_REMOVED)
				nbrOfRemovedMedias++;
		}

		if (mediaList.size() == nbrOfRemovedMedias)
		{
			restore();
			throw new ImsException("Session contains no media - use terminate to close");
		}

		LOGGER.logDebug("session update");

		setState(STATE_RENEGOTIATING);

		MessageImpl message = (MessageImpl) getNextRequest();

		try
		{
			if (localSD != null && localSD_back != null && localSD.getDirection() != localSD_back.getDirection())
				updateState = UPDATE_MODIFIED;
			if (mediaList != null && mediaList.size() > 0)
			{
				Iterator iter = mediaList.iterator();
				while (iter.hasNext())
				{
					MediaImpl elem = (MediaImpl) iter.next();
					if (elem.getState() == Media.STATE_INACTIVE)
					{
						elem.prepare();
					} else if (elem.getState() == Media.STATE_ACTIVE)
					{
						if (elem.getUpdateState() == Media.UPDATE_MODIFIED)
							elem.prepareUpdate();
						else if (elem.getUpdateState() == Media.UPDATE_REMOVED)
						{
							elem.prepareRemoval();
						}
					}
				}

				OriginField o = localSD.getOrigin();
				localSD.setOrigin(new OriginField(o.getUserName(), o.getSessionId(), (new Long(SDPUtils.getNtpTime(new Date()))).toString(),
						o.getAddressType(), o.getAddress()));

				// Create the offer sdp from the prepared media objects
				byte[] sdp;
				try
				{
					sdp = generateSDPContent().getBytes(DEFAULT_ENCODING);
				} catch (UnsupportedEncodingException e)
				{
					LOGGER.logError(e.getMessage(), e);
					throw new ImsException(e.getMessage(), e);
				}

				message.createBodyPart(sdp, false, "Content-Type", CONTENT_TYPE_SDP);
			}
		} catch (Exception e)
		{
			LOGGER.logError(e.getMessage(), e);
			setState(Session.STATE_ESTABLISHED);
			throw new ImsException(e.getMessage(), e);
		}

		sendNextRequest();
	}

	public void provisionalResponse(MessageImpl responseMessage)
	{
		referenceNotify(ReferenceInformationListener.STATE_REFERENCE_ACTIVE, 0, responseMessage.getStatusCode() + " " + responseMessage.getReasonPhrase());

		int statusCode = responseMessage.getStatusCode();
		LOGGER.logInfo("got response {"+new Integer(statusCode)+"}: {"+ responseMessage.getReasonPhrase()+"}");

		if (statusCode == 100)
			return;

		if (statusCode == 180 && (responseMessage.getBodyParts() == null || responseMessage.getBodyParts().length == 0))
		{
			if (listener != null)
				listener.sessionAlerting(this);
			return;
		}

		if (responseMessage.getBodyParts() == null || responseMessage.getBodyParts().length == 0)
			return;

		LOGGER.logInfo("Incoming response with SDP - starting media");
		startMediaWithResponse(responseMessage);

		if (statusCode == 180 || statusCode == 181 || statusCode == 183)
			if (listener != null)
				listener.sessionAlerting(this);
	}

	public void methodDelivered(MessageImpl responseMessage)
	{
		gotLastRequestTimeout = false;
		int statusCode = responseMessage.getStatusCode();
		String method = responseMessage.getMethod();
		LOGGER.logInfo("session response " + statusCode);

		try
		{

			switch (statusCode)
			{
			case 200:
				if ("INVITE".equals(method))
				{
					// Send out refer notification
					referenceNotify(ReferenceInformationListener.STATE_REFERENCE_ACTIVE, 0, responseMessage.getStatusCode() + " "
							+ responseMessage.getReasonPhrase());

					processInviteResponse200(responseMessage);
				} else if ("CANCEL".equals(method))
					setState(STATE_TERMINATED);
				else if ("UPDATE".equals(method))
				{

				} else if ("BYE".equals(method))
					processByeResponse();
				break;
			default:
			}
		} catch (Exception e)
		{
			LOGGER.logError(e.getMessage(), e);
		} finally
		{
			if (syncRoot != null)
			{
				synchronized (syncRoot)
				{
					syncRoot.notifyAll();
					syncRoot = null;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.fhg.fokus.ims.core.ServiceMethodImpl#methodDeliveryFailed(de.fhg.fokus
	 * .ims.core.MessageImpl)
	 */
	public void methodDeliveryFailed(MessageImpl responseMessage)
	{
		try
		{
			LOGGER.logDebug("Delivery failed: " + responseMessage.getMethod());
			if (state == STATE_NEGOTIATING)
			// Outgoing INVITE failed - session will not establish
			{
				referenceNotify(ReferenceInformationListener.STATE_REFERENCE_TERMINATED, 0, responseMessage.getStatusCode() + " "
						+ responseMessage.getReasonPhrase());

				try
				{
					for (int i = 0; i < mediaList.size(); i++)
					{
						MediaImpl media = (MediaImpl) mediaList.get(i);
						media.close();
					}
					mediaList.clear();
				} finally
				{
					// Set state
					this.state = STATE_TERMINATED;
					getContainer().remove(this);

					if (listener != null)
						listener.sessionStartFailed(this);
				}
			} else if (state == STATE_RENEGOTIATING)
			// Outgoing Re-INVITE failed - session will go back to establish
			{
				try
				{
					for (int i = 0; i < mediaList.size(); i++)
					{
						MediaImpl media = (MediaImpl) mediaList.get(i);
						if (media.getState() == Media.STATE_PENDING)
							media.unprepare();
						else if (media.getState() == Media.STATE_ACTIVE && media.getUpdateState() == Media.UPDATE_MODIFIED)
							media.unprepareUpdate();
					}
				} finally
				{
					// remove all pending and unused media
					cleanUpMedia();

					setState(STATE_ESTABLISHED);

					if (listener != null)
						listener.sessionUpdateFailed(this);
				}
			} else if (state == STATE_TERMINATING)
			{
				setState(STATE_TERMINATED);
			} else
			{
				if (listener != null)
					listener.sessionTerminated(this);
			}
		} finally
		{
			// Notify
			if (syncRoot != null)
			{
				synchronized (syncRoot)
				{
					syncRoot.notifyAll();
					syncRoot = null;
				}
			}
		}
	}

	public void sendProvisionalResponse(int statusCode, String message)
	{
		sendResponse(statusCode, message, null, null);
	}

	protected void methodDeliveryTimeout()
	{
		closeMedia();
		gotLastRequestTimeout = true;
		
		if (syncRoot != null)
		{
			synchronized (syncRoot)
			{
				syncRoot.notifyAll();
				syncRoot = null;
			}
		}
		setState(STATE_TERMINATED);
	}

	private String generateSDPContent()
	{
		ConnectionField c = localSD.getConnectionField();
		localSD.setConnectionField(null);

		StringBuffer stringbuffer = new StringBuffer(localSD.toString());
		Iterator iterator = mediaList.iterator();

		while (iterator.hasNext())
		{
			MediaImpl media = (MediaImpl) iterator.next();
			((MediaDescriptorImpl) media.getMediaDescriptors()[0]).setConnectionField(c);
			media.generateSDPContent(stringbuffer);
		}

		localSD.setConnectionField(c);

		return stringbuffer.toString();
	}

	/**
	 * Sender side / start() / update() - initial state: NEGOTIATING,
	 * RENEGOTIATING
	 * 
	 * @param message
	 */
	private void processInviteResponse200(MessageImpl message)
	{
		setNextRequest(new MessageImpl(0, "ACK"));
		sendNextRequest();

		this.sessionId = ((CallIdHeader) message.getHeader("Call-ID")).getCallId();

		if ((state != STATE_ESTABLISHED) && (this.automaticMediaHandling))
		{
			startMediaWithResponse(message);

			try
			{
				cleanUpMedia();
			} catch (Exception e)
			{
				LOGGER.logError(e.getMessage(), e);
			}

			if (listener == null)
			{
				setState(STATE_ESTABLISHED);
			} else
			{
				if (state == STATE_NEGOTIATING)
				{
					setState(STATE_ESTABLISHED);
					listener.sessionStarted(this);
				} else if (state == STATE_RENEGOTIATING)
				{
					setState(STATE_ESTABLISHED);
					listener.sessionUpdated(this);
				}
			}

			// cleanUpMedia();
			// finish
		} else if(!automaticMediaHandling) {
			if (listener == null)  {
				setState(STATE_ESTABLISHED);
			} else {
				if (state == STATE_NEGOTIATING) {
					setState(STATE_ESTABLISHED);
					listener.sessionStarted(this);
				} else if (state == STATE_RENEGOTIATING) {
					setState(STATE_ESTABLISHED);
					listener.sessionUpdated(this);
				}
			}
		}

		try
		{
			// Session Timer
			SessionExpiresHeader seh = (SessionExpiresHeader) message.getHeader("Session-Expires");
			if (seh != null)
			{
				int expireTime = seh.getExpires();
				String refresher = seh.getRefresher();
				if (refresher != null && "uac".equals(refresher))
				{
					if (expireTime > 0)
					{
						int refreshSeconds = (expireTime * 1000) / 2;
						if (refreshSeconds > 0)
						{
							LOGGER.logInfo("session update active to refresh after " + refreshSeconds);
							if (timerTask != null)
								timerTask.cancel();
							timerTask = new SessionRefreshTask(this);
							getCoreService().getTimer().schedule(timerTask, refreshSeconds);
						}
					}

				}
			}

		} catch (Exception e)
		{
			LOGGER.logError(e.getMessage(), e);
		}
	}

	private void startMediaWithResponse(Message message)
	{
		MessageBodyPart[] bodyParts = message.getBodyParts();

		for (int i = 0; i < bodyParts.length; i++)
		{
			if (!bodyParts[i].getHeader("Content-Type").equals(CONTENT_TYPE_SDP))
				continue;

			byte[] content = ((MessageBodyPartImpl) bodyParts[i]).getRawContent();

			String sdpContent = null;
			try
			{
				sdpContent = new String(content, DEFAULT_ENCODING);

			} catch (IOException e)
			{
				LOGGER.logError(e.getMessage(), e);
			}

			remoteSD = new SessionDescriptorImpl(this, sdpContent);
			MediaDescriptor[] remoteMDs = remoteSD.getMedia();

			for (int j = 0; j < remoteMDs.length; j++)
			{
				MediaDescriptorImpl remoteMD = (MediaDescriptorImpl) remoteMDs[j];
				MediaImpl media = (MediaImpl) mediaList.get(j);
				int remotePort = remoteMD.getPort();

				if (media.getState() == Media.STATE_ACTIVE)
				{
					if (remotePort == 0)
					{
						if (media.getUpdateState() == Media.UPDATE_REMOVED)
						{
							LOGGER.logDebug("update media: removal accepted");
							media.setState(Media.STATE_DELETED);
						} else if (media.getUpdateState() == Media.UPDATE_MODIFIED)
						{
							LOGGER.logDebug("update media: modification rejected");
							media.unprepareUpdate();
						}
					} else
					{
						if (media.getUpdateState() == Media.UPDATE_MODIFIED)
						{
							LOGGER.logDebug("update media: modification accepted");
							try
							{
								media.setRemoteUpdateMediaDescriptor(remoteMD);
								media.completeUpdate();
							} catch (MediaCompletionException e)
							{
								LOGGER.logError(e.getMessage());
							}
						}
					}
				} else if (media.getState() == Media.STATE_PENDING)
				{
					if (remotePort == 0)
					{
						LOGGER.logDebug("adding media: rejected");
						media.unprepare();
					} else
					{
						try
						{
							LOGGER.logDebug("adding media: accepted");
							media.setRemoteMediaDescriptor(remoteMD);
							media.complete();
						} catch (MediaCompletionException e)
						{
							LOGGER.logError(e.getMessage(), e);

						}
					}
				}
			}
			break;
		}
	}

	protected void requestReceived(MessageImpl requestMessage)
	{
		String method = requestMessage.getMethod();

		if (Request.NOTIFY.equals(method))
		{
			if (outgoingReference != null)
			{
				outgoingReference.requestReceived(requestMessage);

				SubscriptionStateHeader header = (SubscriptionStateHeader) requestMessage.getHeader("Subscription-State");
				if (header.getState().equals("terminated"))
					outgoingReference = null;
			}
		} else if (Request.BYE.equals(method))
		{
			setState(STATE_TERMINATING);
			sendResponse(Response.OK, "OK", null, null);
			setState(STATE_TERMINATED);
			closeMedia();
		} else if (Request.ACK.equals(method))
		{
			processAck(requestMessage);
		} else if (Request.INVITE.equals(method))
		{
			LOGGER.logDebug("Incoming session update");
			processReInvite(requestMessage);
		}
	}

	protected void cancelReceived(Request request)
	{
		try
		{
			Response response = IMSManager.getInstance().createResponse(request, 200);
			IMSManager.getInstance().sendResponse(getCoreService(), request, response);
		} catch (Exception e)
		{
			LOGGER.logError(e.getMessage(), e);
		}

		sendResponse(Response.REQUEST_TERMINATED, "Request terminated", null, null);
		if (state == Session.STATE_NEGOTIATING)
		{
			state = STATE_TERMINATED;
			getContainer().remove(this);
			if (listener != null)
				listener.sessionStartFailed(this);
		} else if (state == Session.STATE_RENEGOTIATING)
		{
			setState(Session.STATE_ESTABLISHED);
			cleanUpMedia();
			if (listener != null)
				listener.sessionUpdateFailed(this);
		}

	}

	private void processReInvite(MessageImpl request)
	{
		setState(STATE_RENEGOTIATING);

		if (request.getBodyParts().length == 0)
		{
			isEmptyReInvite = true;

			for (int i = 0; i < mediaList.size(); i++)
			{
				MediaImpl media = (MediaImpl) mediaList.get(i);

				MediaImpl prop = new MediaImpl(this);
				prop.setState(Media.STATE_PROPOSAL);
				prop.setDirection(Media.DIRECTION_SEND_RECEIVE);
				media.setProposal(prop);
				media.setUpdateState(Media.UPDATE_MODIFIED);
			}

			if (listener != null)
				listener.sessionUpdateReceived(this);
		} else
		{

			remoteUpdateSD = createOrUpdateMediaList(request, mediaList);

			if (remoteSD.getDirection() != remoteUpdateSD.getDirection())
				updateState = UPDATE_MODIFIED;

			remoteSD.setOrigin(remoteUpdateSD.getOrigin());

			if (listener != null)
				listener.sessionUpdateReceived(this);
		}
	}

	private void processAck(MessageImpl requestMessage)
	{
		if (isEmptyInvite || isEmptyReInvite)
		// Special case - empty invite, we got the final remote descriptor in
		// the ack
		{
			Request request = requestMessage.getRequest();

			ContentTypeHeader header = (ContentTypeHeader) request.getHeader(SIPHeaderNames.CONTENT_TYPE);
			if (header == null || !("application".equals(header.getContentType()) & "sdp".equals(header.getContentSubType())))
				return;

			// Process remote SDP - for each media descriptor create a media
			// object
			// and add it to the media list.
			if (request.getRawContent() != null)
			{
				String sdpContent = null;
				try
				{
					sdpContent = new String(request.getRawContent(), DEFAULT_ENCODING);
				} catch (UnsupportedEncodingException e)
				{
				}

				if (isEmptyInvite)
				{
					remoteSD = new SessionDescriptorImpl(this, sdpContent);

					try
					{
						MediaDescriptor[] mediaDescriptors = remoteSD.getMedia();
						for (int i = 0; i < mediaDescriptors.length; i++)
						{
							MediaImpl media = (MediaImpl) mediaList.get(i);
							media.setRemoteMediaDescriptor((MediaDescriptorImpl) mediaDescriptors[i]);
							media.complete();
						}
					} catch (Exception e)
					{
						LOGGER.logError(e.getMessage(), e);
					}
				} else if (isEmptyReInvite)
				{
					remoteUpdateSD = new SessionDescriptorImpl(this, sdpContent);
					try
					{
						MediaDescriptor[] mediaDescriptors = remoteUpdateSD.getMedia();
						for (int i = 0; i < mediaDescriptors.length; i++)
						{
							MediaImpl media = (MediaImpl) mediaList.get(i);
							media.setRemoteUpdateMediaDescriptor((MediaDescriptorImpl) mediaDescriptors[i]);
							media.setDirection(Media.DIRECTION_SEND_RECEIVE);
							media.prepareUpdate();
							media.completeUpdate();
						}
					} catch (Exception e)
					{
						LOGGER.logError(e.getMessage(), e);
					}
				}
			}
		}

		if (state == STATE_ESTABLISHING)
		{
			setState(STATE_ESTABLISHED);

			if (listener != null)
				listener.sessionStarted(this);

		} else if (state == STATE_REESTABLISHING)
		{
			setState(STATE_ESTABLISHED);

			if (listener != null)
				listener.sessionUpdated(this);
		} else if (state == STATE_TERMINATING)
		{
			setState(STATE_TERMINATED);

		}
	}

	protected void processByeResponse()
	{
		setState(STATE_TERMINATED);
	}

	private void prepareLocalMedia(MessageImpl message) throws MediaPreparationException, ImsException
	{
		// Prepare every local media (give the chance to check the port and
		// else)
		if (mediaList.size() > 0)
		{
			for (int i = 0; i < mediaList.size(); i++)
			{
				MediaImpl media = (MediaImpl) mediaList.get(i);
				media.prepare();
			}

			// Create the offer sdp from the prepared media objects
			byte[] sdp;
			try
			{
				sdp = generateSDPContent().getBytes(DEFAULT_ENCODING);
			} catch (UnsupportedEncodingException e)
			{
				LOGGER.logError(e.getMessage(), e);
				throw new ImsException(e.getMessage(), e);
			}

			message.createBodyPart(sdp, false, "Content-Type", CONTENT_TYPE_SDP);
		}

	}

	private void updateSessionTimer() throws IllegalStateException, ImsException
	{
		if (state != STATE_ESTABLISHED)
			throw new IllegalStateException("update() can be called only in the ESTABLISHED state! (Current state is " + state + ")");

		if (!hasPendingUpdates())
			throw new IllegalStateException("update() cannot be called. There are no pending updates!");

		LOGGER.logDebug("session timer update");

		// MessageImpl message = new MessageImpl(Message.SESSION_UPDATE,
		// "UPDATE");
		// setNextRequest(message);
		MessageImpl message = (MessageImpl) getNextRequest();
		message.internalAddHeader(SIPHeaderNames.SUPPORTED, "timer");
		message.internalAddHeader(SIPHeaderNames.SESSION_EXPIRES, String.valueOf(sessionExpireTimer) + ";refresher=uac");

		sendNextRequest();
	}

	private void closeMedia()
	{
		Iterator iterator = mediaList.iterator();
		while (iterator.hasNext())
		{
			MediaImpl media = (MediaImpl) iterator.next();
			media.close();
		}
	}

	public void setState(int state)
	{
		this.state = state;
		LOGGER.logDebug("Session goes to state: {"+STATES[state - 1]+"}" );

		if (state == STATE_ESTABLISHED)
		{
			updateState = UPDATE_UNCHANGED;
			setNextRequest(new MessageImpl(Message.SESSION_UPDATE, "INVITE"));
			isEmptyInvite = false;
		} else if (state == STATE_TERMINATED)
		{
			getContainer().remove(this);

			if (syncRoot != null)
			{
				synchronized (syncRoot)
				{
					syncRoot.notifyAll();
				}
			}

			if (listener != null)
				listener.sessionTerminated(this);
		}
	}

	protected MessageImpl createRequestMessage(Request request)
	{
		if (Request.ACK.equals(request.getMethod()))
			return new MessageImpl(-1, request);
		if (Request.INVITE.equals(request.getMethod()))
			return new MessageImpl(Message.SESSION_UPDATE, request);
		if (Request.CANCEL.equals(request.getMethod()))
			return new MessageImpl(Message.SESSION_TERMINATE, request);
		if (Request.BYE.equals(request.getMethod()))
			return new MessageImpl(Message.SESSION_TERMINATE, request);
		if (Request.REFER.equals(request.getMethod()))
			return new MessageImpl(Message.REFERENCE_REFER, request);

		return null;
	}

	protected MessageImpl createResponseMessage(Response response)
	{
		int id = 0;
		switch (state)
		{
		case STATE_ESTABLISHING:
		case STATE_NEGOTIATING:
			id = ((MessageImpl) getPreviousRequest(Message.SESSION_START)).getIdentifier();
			break;
		case STATE_RENEGOTIATING:
			id = ((MessageImpl) getPreviousRequest(Message.SESSION_UPDATE)).getIdentifier();
			break;
		case STATE_TERMINATING:
			id = ((MessageImpl) getPreviousRequest(Message.SESSION_TERMINATE)).getIdentifier();
			break;
		}

		return new MessageImpl(id, response);
	}

	private void cleanUpMedia()
	{
		for (int i = mediaList.size() - 1; i >= 0; i--)
		{
			MediaImpl media = (MediaImpl) mediaList.get(i);
			if (media.getState() == Media.STATE_DELETED || (!isEmptyInvite && media.getState() == Media.STATE_PENDING))
			{
				media.close();
				mediaList.remove(i);
			}
		}
	}

	private SessionDescriptorImpl createOrUpdateMediaList(Message message, ArrayList mediaList)
	{
		MessageBodyPart[] parts = message.getBodyParts();

		if (parts == null || parts.length == 0)
			return null;

		MessageBodyPart part = null;
		for (int i = 0; i < parts.length; i++)
		{
			if ("application/sdp".equals(parts[i].getHeader("Content-Type")))
			{
				part = parts[i];
				break;
			}
		}

		String sdpContent;
		try
		{
			sdpContent = Utils.ConvertByteToString((ByteArrayInputStream) part.openContentInputStream());
		} catch (IOException e)
		{
			LOGGER.logInfo("error in retrieving the sdp content");
			return null;
		}

		SessionDescriptorImpl sessionDescriptor = new SessionDescriptorImpl(this, sdpContent);

		MediaDescriptor[] newDescriptors = sessionDescriptor.getMedia();
		for (int j = 0; j < newDescriptors.length; j++)
		{
			MediaDescriptorImpl newMediaDescriptor = (MediaDescriptorImpl) newDescriptors[j];
			if (j < mediaList.size())
			// Modification of existing streams
			{
				MediaImpl oldMedia = (MediaImpl) mediaList.get(j);
				int changes = oldMedia.getRemoteMediaDescriptor().detectChanges(newMediaDescriptor);
				if (changes == 0)
				{
					oldMedia.setUpdateState(Media.UPDATE_UNCHANGED);
				} else
				{
					// Detection for deletion
					if (newMediaDescriptor.getPort() == 0)
					{
						oldMedia.setUpdateState(Media.UPDATE_REMOVED);
					} else
					{
						oldMedia.setRemoteUpdateMediaDescriptor(newMediaDescriptor);
						oldMedia.setUpdateState(Media.UPDATE_MODIFIED);
						MediaImpl prop = new MediaImpl(this);
						prop.setState(Media.STATE_PROPOSAL);
						prop.setDirection(newMediaDescriptor.getDirection());
						oldMedia.setProposal(prop);
					}
				}
			} else
			// Creation of new streams
			{
				MediaImpl media = null;
				if ("RTP/AVP".equals(newMediaDescriptor.getTransport()))
					media = coreService.createMedia(this, "StreamMedia");
				else if ("TCP/MSRP".equals(newMediaDescriptor.getTransport()))
					media = coreService.createMedia(this, "FramedMedia");
				else if ("TCP".equals(newMediaDescriptor.getTransport()))
					media = coreService.createMedia(this, "BasicReliableMedia");
				else if("UDP".equals(newMediaDescriptor.getTransport()))
					media = coreService.createMedia(this, "BasicUnreliableMedia");
				
				MediaImpl prop = new MediaImpl(this);
				prop.setDirection(newMediaDescriptor.getDirection());
				prop.setDescriptor(newMediaDescriptor);
				media.setProposal(prop);

				media.setState(Media.STATE_PENDING);
				media.setRemoteMediaDescriptor(newMediaDescriptor);
				newMediaDescriptor.setMedia(media);

				mediaList.add(media);
			}
		}

		return sessionDescriptor;
	}

	public void reference(ReferenceImpl reference)
	{
		this.incomingRefer = reference;

		if (listener != null)
			listener.sessionReferenceReceived(this, reference);
	}

	public void subscription(SubscriptionImpl subscription)
	{
		if (listener instanceof SessionListener2)
			((SessionListener2) listener).sessionSubscriptionReceived(this, subscription);
	}

	public SubscriptionContainer getSubscriptions()
	{
		return subscriptions;
	}
	
	public void setAutomaticMediaHandling(boolean param)  {
		automaticMediaHandling = param;
	}

}
