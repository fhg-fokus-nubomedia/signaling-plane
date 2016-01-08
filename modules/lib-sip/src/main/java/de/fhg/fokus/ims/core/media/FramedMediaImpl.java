package de.fhg.fokus.ims.core.media;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.ims.core.Session;
import javax.ims.core.media.FramedMedia;
import javax.ims.core.media.FramedMediaListener;
import javax.ims.core.media.Media;

import de.fhg.fokus.ims.core.IMSManager;
import de.fhg.fokus.ims.core.SessionImpl;
import de.fhg.fokus.ims.core.sdp.AttributeField;
import de.fhg.fokus.ims.core.sdp.MediaField;
import de.fhg.fokus.ngni.proto.msrp.MsrpClientSession;
import de.fhg.fokus.ngni.proto.msrp.MsrpClientSessionListener;
import de.fhg.fokus.ngni.proto.msrp.MsrpException;
import de.fhg.fokus.ngni.proto.msrp.MsrpFactory;
import de.fhg.fokus.ngni.proto.msrp.MsrpRequest;
import de.fhg.fokus.ngni.proto.msrp.MsrpResponse;
import de.fhg.fokus.ngni.proto.msrp.MsrpSession;

/**
 * Implementation of the FramedMedia Interface.
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * 
 */
public class FramedMediaImpl extends MediaImpl implements FramedMedia, MsrpClientSessionListener
{	
	private static StackLogger logger = CommonLogger.getLogger(FramedMediaImpl.class);
	
	private MsrpClientSession messageSession;

	private FramedMediaListener listener;

	private boolean inactive = false;
	
	private String[] acceptedContentTypes = new String[]
	{ "text/plain" };

	public FramedMediaImpl(SessionImpl session)
	{
		super(session);

		localMD = new MediaDescriptorImpl(this, new MediaField("message", 0, 0, "TCP/MSRP", "*"), null);
	}

	public void cancel(String messageId)
	{
		messageSession.cancel(messageId);
	}

	public String[] getAcceptedContentTypes()
	{
		return acceptedContentTypes;
	}

	public void setAcceptedContentTypes(String[] acceptedContentTypes)
	{
		this.acceptedContentTypes = acceptedContentTypes;
	}

	public String getContentType(String messageId) throws IllegalArgumentException
	{
		return messageSession.getContentType(messageId);
	}

	public String[] getHeader(String messageId, String key) throws IllegalArgumentException
	{
		return new String[]
		{ messageSession.getHeader(messageId, key) };
	}

	public byte[] receiveBytes(String messageId) throws IOException
	{
		return messageSession.receiveBytes(messageId);
	}

	public void receiveFile(String messageId, String locator) throws IOException, SecurityException, IllegalArgumentException
	{
		messageSession.receiveFile(messageId, locator);
	}

	public String sendBytes(byte[] content, String contentType, String[][] headers) throws IOException
	{
		return messageSession.sendBytes(content, contentType, headers);
	}

	public String sendFile(String locator, String contentType, String[][] headers) throws IOException
	{
		return messageSession.sendFile(locator, contentType, headers);
	}

	public void setListener(FramedMediaListener listener)
	{
		this.listener = listener;
	}

	protected void onPrepare()
	{
		if (session.getState() == Session.STATE_NEGOTIATING || session.getState() == Session.STATE_RENEGOTIATING)
			init(MsrpSession.CONNECTION_ACTIVE);
		else if (session.getState() == Session.STATE_ESTABLISHING || session.getState() == Session.STATE_REESTABLISHING)
		{
			AttributeField attribute = remoteMD.getAttribute("setup");
			if (attribute != null)
			{
				String value = attribute.getAttributeValue();
				if (value.equals("actpass") || value.equals("passive"))
				{
					init(MsrpSession.CONNECTION_ACTIVE);
					localMD.addAttribute(new AttributeField("setup", "active"));
				} else
				{
					init(MsrpSession.CONNECTION_PASSIVE);
					localMD.addAttribute(new AttributeField("setup", "passive"));
				}
			} else
				init(MsrpSession.CONNECTION_PASSIVE);

			AttributeField attributeField = remoteMD.getAttribute("accept-types");
			String value = attributeField.getAttributeValue();
			StringTokenizer tokenizer = new StringTokenizer(value, " ");
			ArrayList temp = new ArrayList(3);
			while (tokenizer.hasMoreTokens())
			{
				temp.add(tokenizer.nextToken());
			}
			acceptedContentTypes = (String[]) temp.toArray(new String[temp.size()]);
		}

		int port = messageSession.getFrom().getPort();

		localMD.setMediaField(new MediaField("message", port, 0, "TCP/MSRP", "*"));
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < acceptedContentTypes.length; i++)
		{
			String ct = acceptedContentTypes[i];
			if (i > 0)
				buffer.append(" ");
			buffer.append(ct);
		}

		localMD.addAttribute(new AttributeField("accept-types", buffer.toString()));
		localMD.addAttribute(new AttributeField("path", messageSession.getFrom().toString()));
	}

	protected void onComplete()
	{
		String remoteSessionId = remoteMD.getAttribute("path").getAttributeValue();

		logger.logDebug(remoteSessionId);
		try
		{
			messageSession.setTo(MsrpFactory.getInstance().createEndpoint(remoteSessionId));
			messageSession.setAcceptedContentTypes(acceptedContentTypes);

			if (remoteMD.getDirection() == Media.DIRECTION_INACTIVE || 
					session.getRemoteSessionDescriptor().getDirection() == Media.DIRECTION_INACTIVE )
			{
				this.inactive = true;
				logger.logDebug("Session inactive - waiting to for start!");
				return;
			}
			
			messageSession.start();
		} catch (MsrpException e)
		{
			logger.logError(e.getMessage());
		}
	}

	protected void onClose()
	{
		if (messageSession != null)
		{
			messageSession.terminate();
			messageSession = null;
		}
	}

	protected void onCompleteUpdate() throws MediaCompletionException
	{
		if (inactive && (
				getRemoteMediaDescriptor().getDirection() != Media.DIRECTION_INACTIVE || 
				session.getRemoteSessionDescriptor().getDirection() != Media.DIRECTION_INACTIVE))
		{
			inactive = false;
			try
			{
				messageSession.start();
			} catch (MsrpException e)
			{
				logger.logError(e.getMessage(), e);
				throw new MediaCompletionException("Could not start MSRP session: " + e.getMessage(), e);
			}
		}
		super.onCompleteUpdate();
	}
	
	private void init(int role)
	{
		try
		{
			InetAddress address = IMSManager.getInstance().getLocalAddress();
			int port = 7394;

			messageSession = MsrpFactory.getInstance().createMsrpClientSession(session.getRemoteUserId(), address, port, role);
			messageSession.setListener(this);

		} catch (MsrpException e)
		{
			logger.logError(e.getMessage(), e);
		}
	}

	public void connectionError(MsrpSession session)
	{
		if (listener != null)
			listener.connectionError(this);
	}

	public void contentReceiveFailed(MsrpSession session, String messageId)
	{
		if (listener != null)
			listener.contentReceiveFailed(this, messageId);
	}

	public void contentReceived(MsrpSession session, String messageId, int size, String fileName)
	{
		if (listener != null)
			listener.contentReceived(this, messageId, size, fileName);
	}

	public void deliveryFailure(MsrpSession session, String messageId, int statusCode, String reasonPhrase)
	{
		if (listener != null)
			listener.deliveryFailure(this, messageId, statusCode, reasonPhrase);
	}

	public void deliverySuccess(MsrpSession session, String messageId)
	{
		if (listener != null)
			listener.deliverySuccess(this, messageId);
	}

	public void transferProgress(MsrpSession session, String messageId, int bytesTransferred, int bytesTotal)
	{
		if (listener != null)
			listener.transferProgress(this, messageId, bytesTransferred, bytesTotal);
	}

	public void requestReceived(MsrpSession session, MsrpRequest request)
	{
		// TODO Auto-generated method stub

	}

	public void responseReceived(MsrpSession session, MsrpResponse response)
	{
		// TODO Auto-generated method stub

	}
}
