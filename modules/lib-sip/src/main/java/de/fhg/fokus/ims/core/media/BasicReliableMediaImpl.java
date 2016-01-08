package de.fhg.fokus.ims.core.media;


import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import javax.ims.core.Session;
import javax.ims.core.media.BasicReliableMedia;
import javax.ims.core.media.BasicReliableMediaListener;
import javax.ims.core.media.Media;

import de.fhg.fokus.ims.core.SessionDescriptorImpl;
import de.fhg.fokus.ims.core.SessionImpl;
import de.fhg.fokus.ims.core.sdp.MediaField;

/**
 * Implementation of the BasicReliableMedia Interface.
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public class BasicReliableMediaImpl extends MediaImpl implements BasicReliableMedia 
{

	private static StackLogger logger = CommonLogger.getLogger(BasicReliableMediaImpl.class);
	
	private static String MEDIAERROR = "Can't create media.";
	private String contentType;
	private InputStream mediaInputStream;
	private OutputStream mediaOutputStream;
	private WaitingSocketThread waitingSocketThread;
	private BasicReliableMediaListener listener;
	private ServerSocket serverSocket;
	private Socket socket;
	
	
	public BasicReliableMediaImpl(SessionImpl session) 
	{
		super(session);
		logger.logDebug("BasicReliableMedia initialized.");
	}

	public String getContentType() 
	{
		return contentType;
	}

	
	
	public InputStream getInputStream() throws IllegalStateException,
			IOException 
	{		
		logger.logDebug("Getting input stream.");
		if((getState() == Media.STATE_ACTIVE) 
				&& (getDirection() != Media.DIRECTION_SEND))  {
			return this.mediaInputStream;
		} else  {
			throw new IllegalStateException();
		}
		
	}

	public OutputStream getOutputStream() throws IllegalStateException,
			IOException 
	{	
		logger.logDebug("Getting output stream.");
		if((getState() == Media.STATE_ACTIVE) 
				&& (getDirection() != Media.DIRECTION_RECEIVE))  {
			return this.mediaOutputStream;
		} else  {
			throw new IllegalStateException();
		}
	}

	   /**
     * Sets the content type of this Media.
     * 
     * @param contentType - the content type
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE
     * @throws IllegalArgumentException - if the syntax of the contentType argument is invalid
     */
	public synchronized void setContentType(String contentType)
			throws IllegalStateException, IllegalArgumentException 
	{
		if(getState() != Media.STATE_INACTIVE)
			throw new IllegalStateException("BasicReliableMediaImpl.setContentType(): Media not in STATE_INACTIVE.");
		
		this.contentType = contentType;
	}

	public void setListener(BasicReliableMediaListener listener) 
	{
		this.listener = listener;
		
	}	
	
	//public MediaField(String media, int port, int num, String transport, String formats)
	
	protected void onPrepare() throws MediaPreparationException {
		logger.logDebug("Preparing basic reliable media.");
		if (session.getState() == Session.STATE_NEGOTIATING) {
			try {
				socket = new Socket();
				socket.bind(null);
				
				MediaDescriptorImpl md = new MediaDescriptorImpl(
						(SessionDescriptorImpl) session.getSessionDescriptor(),
						this);
				MediaField mediaField = new MediaField("connection",
						socket.getLocalPort(), 0, "TCP", "*");
				md.setMediaField(mediaField);
				setDescriptor(md);
			} catch (IOException e) {
				logger.logDebug(e.toString());
			}
		} else if (session.getState() == Session.STATE_ESTABLISHING) {
			try {
				serverSocket = new ServerSocket();
				serverSocket.bind(null);
				
				MediaDescriptorImpl md = new MediaDescriptorImpl(
						(SessionDescriptorImpl) session.getSessionDescriptor(),
						this);
				MediaField mediaField = new MediaField("connection",
						serverSocket.getLocalPort(), 0, "TCP", "*");
				md.setMediaField(mediaField);
				setDescriptor(md);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	protected void onUnprepare() {
		System.out.println("unprepare?");
	}
	
	protected void onPrepareUpdate() {
		System.out.println("prepare update?");
	}
	
	protected void onCompleteUpdate() {
		System.out.println("update complete.");
	}
	
	
	
	
	protected void onComplete() throws MediaCompletionException
	{
		if(session.getState() == Session.STATE_NEGOTIATING) {
			try {
				MediaDescriptorImpl rmd = (MediaDescriptorImpl) this
						.getRemoteMediaDescriptor();
				int remoteport = rmd.getPort();
				String remoteaddress = rmd.getConnectionField().getAddress();
				SocketAddress socketAddress = new InetSocketAddress(
						remoteaddress, remoteport);
				socket.connect(socketAddress);
				setStreams();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new MediaCompletionException(MEDIAERROR);
			} catch (NullPointerException e) {
				throw new MediaCompletionException(MEDIAERROR);
			}
		} else if(session.getState() == Session.STATE_ESTABLISHING) {
			waitingSocketThread = new WaitingSocketThread(serverSocket);
			waitingSocketThread.start();
		}
	}
	
	protected void onClose()
	{
		logger.logDebug("On close.");
		try {
			/* In signaling mode and never initialized. */
			if(socket!=null) {
				socket.close();
				mediaInputStream = null;
				mediaOutputStream = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setStreams() {
		try {
			mediaInputStream = socket.getInputStream();
			mediaOutputStream = socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private class WaitingSocketThread extends Thread  {
		ServerSocket serverSocket;
		
		public WaitingSocketThread(ServerSocket serverSocket)  {
			this.serverSocket = serverSocket;
		}
		
		public void run() {
			try {
				socket = serverSocket.accept();
				setStreams();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
