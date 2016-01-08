package de.fhg.fokus.ims.core.media;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import javax.ims.ImsException;
import javax.ims.core.Session;
import javax.ims.core.media.BasicUnreliableMedia;
import javax.ims.core.media.BasicUnreliableMediaListener;
import javax.ims.core.media.Media;

import de.fhg.fokus.ims.core.SessionDescriptorImpl;
import de.fhg.fokus.ims.core.SessionImpl;
import de.fhg.fokus.ims.core.sdp.MediaField;

/**
 * Implementation of the BasicUnreliableMedia Interface.
 * 
 * @version JSR281-PUBLIC-REVIEW (subject to change).
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @author Andreas Bachmann (andreas.bachmann@fokus.fraunhofer.de)
 */
public class BasicUnreliableMediaImpl extends MediaImpl implements BasicUnreliableMedia, Runnable
{
	private static StackLogger LOGGER = CommonLogger.getLogger(BasicUnreliableMediaImpl.class);
	
	private static final int MAX_PACKET_SIZE = 8192;
	private String contentType;
	private BasicUnreliableMediaListener listener;

	private DatagramSocket socket;
	private InetSocketAddress remoteEndpoint;
	private boolean doRun = true;
	private byte[] content;
	private int contentLength;

	
	/**
	 * Constructor of basic unreliable media. Just calls the constructor
	 * of the super class.
	 * 
	 * @param session
	 */
	public BasicUnreliableMediaImpl(SessionImpl session) {
		super(session);
	}

	
	/**
	 * 
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 *  Attention, slightly modified from the interface. 
	 */
	public byte[] receive() throws IOException, IllegalArgumentException,
			IllegalStateException {
		
		/* slightly modified of jsr281 */
		DatagramPacket packet = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
		socket.receive(packet);
		
		/** pure jsr281 implementation
		byte[] result = new byte[contentLength];
		try {
			result = new byte[contentLength];
			System.arraycopy(content, 0, result, 0, contentLength);
		} catch (NullPointerException e) {}
		**/
		
		return packet.getData();
	}

	
	/**
	 * 
	 */
	public void send(byte[] content) throws IOException, ImsException {
		DatagramPacket packet = new DatagramPacket(content, content.length, remoteEndpoint);
		socket.send(packet);
	}

	
	/**
	 * 
	 */
	public void setContentType(String contentType) throws IOException, IllegalStateException
	{
		if (getState() != Media.STATE_INACTIVE)
			throw new IllegalStateException("BasicReliableMediaImpl.setContentType(): Media not in STATE_INACTIVE.");

		this.contentType = contentType;
	}

	/**
	 * 
	 */
	public void setListener(BasicUnreliableMediaListener listener) {
		if (listener == null)
			this.listener = null;
		else
			this.listener = listener;
	}


	/**
	 * Derives information about the remote end point through 
	 * the media descriptor and creates a connection.
	 */
	protected void onComplete() throws MediaCompletionException {
		try {
			/* On invitation side building up the connection. */
			if (session.getState() == session.STATE_NEGOTIATING) {

				MediaDescriptorImpl rmd = (MediaDescriptorImpl) this
						.getRemoteMediaDescriptor();
				int remoteport = rmd.getPort();
				String remoteaddress = rmd.getConnectionField().getAddress();
				remoteEndpoint = new InetSocketAddress(remoteaddress,
						remoteport);
				socket.connect(remoteEndpoint);
			}
			//new Thread(this).start();
		} catch (SocketException e) {
			throw new MediaCompletionException("Can't complete media.");
		}
	}

	
	/**
	 * 
	 */
	protected void onClose() {
		doRun = false;
		if(socket==null) {
			System.out.println("Media onClose: Media was not initialized. Probably non automatic media handling.");
			return;
		}
		socket.close();
	}

	
	/**
	 * 
	 */
	public void run()
	{
		DatagramPacket packet = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
		while (doRun) {
			try {
				socket.receive(packet);
				content = packet.getData();
				contentLength = packet.getLength();
				if (listener != null)
					listener.contentReceived(this);
				content = null;
			} catch (IOException e) {
				LOGGER.logError(e.getMessage(), e);
			}
		}
	}
	
	
	/**
	 * 
	 */
	protected void onPrepare() throws MediaPreparationException {
		if(session.getState() == Session.STATE_NEGOTIATING)  {
			prepareOnNegotiating();
		} else if(session.getState() == Session.STATE_ESTABLISHING) {
			prepareOnEstablishing();
		}
	}	
	
	/**
	 * Binds a random datagram socket and sets the media
	 * descriptor of the current session based on the new 
	 * 
	 * @throws MediaPreparationException
	 */
	private void prepareOnNegotiating() throws MediaPreparationException {
		try {
			socket = new DatagramSocket(null);
			socket.bind(null);

			MediaDescriptorImpl md = new MediaDescriptorImpl(
					(SessionDescriptorImpl) session.getSessionDescriptor(),
					this);
			
		
			MediaField mediaField = new MediaField("connection",
					socket.getLocalPort(), 0, "UDP", "*");
			md.setMediaField(mediaField);

			setDescriptor(md);
		} catch (SocketException e) {
			throw new MediaPreparationException("Socket failure.");
		}
	}
	
	
	/**
	 * Gets the address and port of the remote partner. 
	 * Binds a datagram Socket and sets the media descriptor
	 * of the current session.
	 * 
	 * @throws MediaPreparationException
	 */
	private void prepareOnEstablishing() throws MediaPreparationException {
		try {	
			
			MediaDescriptorImpl remoteMd = (MediaDescriptorImpl) this.getRemoteMediaDescriptor();
			int remoteport = remoteMd.getPort();
			String remoteaddress = remoteMd.getTransport();
			
			if(remoteMd.getConnectionField() != null)  {
				remoteaddress = remoteMd.getConnectionField().getAddress();
			} else {
				throw new MediaPreparationException("No connection field specified.");
			}
			
			remoteEndpoint = new InetSocketAddress(remoteaddress, remoteport);
			socket = new DatagramSocket(null);
			socket.bind(null);

			MediaDescriptorImpl localMd = new MediaDescriptorImpl(
					(SessionDescriptorImpl) session.getSessionDescriptor(),
					this);
			MediaField mediaField = new MediaField("connection",
					socket.getLocalPort(), 0, "UDP", "*");	
			localMd.setMediaField(mediaField);
			setDescriptor(localMd);
		} catch (SocketException e) {
			LOGGER.logError(e.getMessage(), e);
			throw new MediaPreparationException(e);
		}		
	}
}
