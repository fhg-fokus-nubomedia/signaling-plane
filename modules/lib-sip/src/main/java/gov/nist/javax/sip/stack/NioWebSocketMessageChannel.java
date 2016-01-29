/*
 * Conditions Of Use
 *
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 *
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 *
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 *
 * .
 *
 */
package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.Host;
import gov.nist.core.HostPort;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.RecordRoute;
import gov.nist.javax.sip.message.SIPMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.text.ParseException;

import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.ContactHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

public class NioWebSocketMessageChannel extends NioTcpMessageChannel{

	private static StackLogger logger = CommonLogger
			.getLogger(NioWebSocketMessageChannel.class);
	
	private WebSocketCodec codec = new WebSocketCodec(true, true);
	
	boolean readingHttp = true;
	String httpInput = "";
	
	public static NioWebSocketMessageChannel create(
			NioWebSocketMessageProcessor nioTcpMessageProcessor,
			SocketChannel socketChannel) throws IOException {
		NioWebSocketMessageChannel retval = (NioWebSocketMessageChannel) channelMap.get(socketChannel);
		if (retval == null) {
			retval = new NioWebSocketMessageChannel(nioTcpMessageProcessor,
					socketChannel);
			channelMap.put(socketChannel, retval);
		}
		return retval;
	}
	
	protected NioWebSocketMessageChannel(NioTcpMessageProcessor nioTcpMessageProcessor,
			SocketChannel socketChannel) throws IOException {
		super(nioTcpMessageProcessor, socketChannel);

		messageProcessor = nioTcpMessageProcessor;
		myClientInputStream = socketChannel.socket().getInputStream();
	}
	
	@Override
	protected void sendMessage(final byte[] msg, final boolean isClient) throws IOException {
		if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
			logger.logDebug("sendMessage isClient  = " + isClient + " this = " + this);
		}
		lastActivityTimeStamp = System.currentTimeMillis();
		
		NIOHandler nioHandler = ((NioTcpMessageProcessor) messageProcessor).nioHandler;
		if(this.socketChannel != null && this.socketChannel.isConnected() && this.socketChannel.isOpen()) {
			nioHandler.putSocket(NIOHandler.makeKey(this.peerAddress, this.peerPort), this.socketChannel);
		}
		sendWrapped(msg, this.peerAddress, this.peerPort, isClient);
	}
	
	protected void sendNonWebSocketMessage(byte[] msg, boolean isClient) throws IOException {

		if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
			logger.logDebug("sendMessage isClient  = " + isClient + " this = " + this);
		}
		lastActivityTimeStamp = System.currentTimeMillis();
		
		NIOHandler nioHandler = ((NioTcpMessageProcessor) messageProcessor).nioHandler;
		if(this.socketChannel != null && this.socketChannel.isConnected() && this.socketChannel.isOpen()) {
			nioHandler.putSocket(NIOHandler.makeKey(this.peerAddress, this.peerPort), this.socketChannel);
		}
		super.sendTCPMessage(msg, this.peerAddress, this.peerPort, isClient);
	}

	private byte[] wrapBufferIntoWebSocketFrame(byte[] buffer) {
		try {
			return WebSocketCodec.encode(buffer, 0, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void sendWrapped(byte message[], InetAddress receiverAddress,
			int receiverPort, boolean retry) throws IOException {
		message = wrapBufferIntoWebSocketFrame(message);
		super.sendTCPMessage(message, receiverAddress, receiverPort, retry);
	}
	
	@Override
	public void sendMessage(final byte message[], final InetAddress receiverAddress,
			final int receiverPort, final boolean retry) throws IOException {
		sendWrapped(message, receiverAddress, receiverPort, retry);
	}

	public NioWebSocketMessageChannel(InetAddress inetAddress, int port,
			SIPTransactionStack sipStack,
			NioTcpMessageProcessor nioTcpMessageProcessor) throws IOException {
		super(inetAddress, port, sipStack, nioTcpMessageProcessor);
	}
	
	@Override
	protected void addBytes(byte[] bytes) throws Exception {
		String s = new String(bytes);
		if(readingHttp) {
			httpInput += s;
			if(s.endsWith("\r\n") || s.endsWith("\n")) {
				readingHttp = false;
				byte[] response = new WebSocketHttpHandshake().createHttpResponse(s);
				sendNonWebSocketMessage(response, false);
			}
		} else if(!readingHttp) {
			ByteArrayInputStream bios = new ByteArrayInputStream(bytes);
			byte[] decodedMsg = null;
			do {
				decodedMsg = codec.decode(bios);
				if(decodedMsg == null) {
					return; // the codec can't parse a full websocket frame, we will try again when have more data
				}
				nioParser.addBytes(decodedMsg);
				logger.logDebug("Nio websocket bytes were added " + decodedMsg.length);
			} while (decodedMsg != null);
			
		}
	}
	
	@Override
	public String getTransport() {
		return "WS";
	}

	@Override
	public void onNewSocket(byte[] message) {
		super.onNewSocket(message);
		
	}
	
    /**
     * Call back method. When the parser finshes parsing a message let the channel see it and decide
     * if it want to create some address mappings (for Websocket or otherwise).
     * 
     * @param message
     * @throws Exception 
     */
	@Override
    public void processMessage(SIPMessage message) throws Exception {
    	if(message instanceof Request) {
    		Request request = (Request) message;
    		if(request.getMethod().equals(Request.REGISTER)) {
    			ContactHeader contact = (ContactHeader) request.getHeader(ContactHeader.NAME);
    			URI uri = contact.getAddress().getURI();
    			if(uri.isSipURI()) {
    				SipURI sipUri = (SipURI) uri;
    				String host = sipUri.getHost();
    				NioTcpMessageProcessor processor = (NioTcpMessageProcessor) this.messageProcessor;
    				HostPort hostPort = new HostPort();
    				hostPort.setHost(new Host(host));
    				hostPort.setPort(5060);
    				processor.assignChannelToDestination(hostPort, this);
    			}
    		}
    		ContactHeader contact = (ContactHeader)message.getHeader(ContactHeader.NAME);
        	RecordRouteHeader rr = (RecordRouteHeader)message.getHeader(RecordRouteHeader.NAME);
        	ViaHeader via = message.getTopmostViaHeader();
        	
        	if(rr == null) {
        		if(contact != null) {
        			rewriteUri((SipURI) contact.getAddress().getURI());
        		}
        	} else {
        		rewriteUri((SipURI) rr.getAddress().getURI()); // not needed but just in case some clients does it
        	}
        	
        	String viaHost = via.getHost();
        	if(viaHost.endsWith(".invalid")) {
        		via.setHost(getPeerAddress());
        		via.setPort(getPeerPort());
        	}
    	} else {
    		ContactHeader contact = (ContactHeader)message.getHeader(ContactHeader.NAME);
        	if(contact != null) {
        		rewriteUri((SipURI) contact.getAddress().getURI());
        	}
    	}
    	
		super.processMessage(message);
    }
	
	public void rewriteUri(SipURI uri) {
		try {
			String uriHost = uri.getHost();
			if(uriHost.endsWith(".invalid")) {
				uri.setHost(getPeerAddress());
			}
		} catch (ParseException e) {
			logger.logError("Cant parse address", e);
		}
		uri.setPort(getPeerPort());
	}

}
