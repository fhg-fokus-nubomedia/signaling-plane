/*
 * Conditions Of Use
 *
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 United States Code Section 105, works of NIST
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
/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 *******************************************************************************/
package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackImpl;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.datagram.DatagramSocket;
import org.vertx.java.core.datagram.InternetProtocolFamily;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetServer;
import org.vertx.java.core.net.NetSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

/*
 * TLS support Added by Daniel J.Martinez Manzano <dani@dif.um.es>
 *
 */

/**
 * Low level Input output to a socket. Caches TCP connections and takes care of
 * re-connecting to the remote party if the other end drops the connection
 *
 * @version 1.2
 *
 * @author M. Ranganathan <br/>
 *
 *
 */

public class IOHandler {
	
	private static StackLogger logger = CommonLogger.getLogger(IOHandler.class);

    private SipStackImpl sipStack;

    private final int max_retry = 2;

    // A cache of client sockets that can be re-used for
    // sending tcp messages.
    private final ConcurrentHashMap<String, Object> socketTable = new ConcurrentHashMap<String, Object>();
    private final ConcurrentHashMap<String, NetSocket> netSocketTable = new ConcurrentHashMap<String, NetSocket>();

    /**
     * Create a unique key for a Vertx NetClient.
     * @param socket
     * @param port
     * @return String key value
     */
    protected static String makeKey(NetClient socket, int port) {
        return socket.toString() + ":" + port;
    }

    /**
     * Create a unique key for a Vertx NetServer.
     * @param socket
     * @return String key value
     */
    protected static String makeKey(NetServer socket) {
        return socket.host() + ":" + socket.port();
    }

    /**
     * Create a unique key for a String and Port (used for Datagram).
     * @param addr
     * @param port
     * @return
     */
    protected static String makeKey(String addr, int port) {
        return addr + ":" + port;
    }

    protected IOHandler(SIPTransactionStack sipStack) {
        this.sipStack = (SipStackImpl) sipStack;
    }

    protected void putSocket(String key, NetServer socket) {
    	if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
            logger.logDebug("adding NetServer for key " + key);
        }
        socketTable.put(key, socket);
    }

    protected void putSocket(String key, NetClient socket) {
        if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
            logger.logDebug("adding NetClient with key :" + key);
        }
        socketTable.put(key, socket);
    }

    protected  void putNetSocket(String key, NetSocket socket) {
        logger.log(StackLogger.TRACE_DEBUG, "Adding NetSocket with key :" + key);
        if (netSocketTable.get(key) == null)
            netSocketTable.put(key, socket);
    }

    protected void putSocket(String key, DatagramSocket socket) {
        if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
            logger.logDebug("adding NetClient for key " + key);
        }
        socketTable.put(key,socket);
    }

    protected Object getSocket(String key) {
        return (Object) socketTable.get(key);
    }

    protected void removeSocket(String key) {
        socketTable.remove(key);
        if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
            logger.logDebug("removed Socket and Semaphore for key " + key);
        }
    }

    /**
     * A private function to write things out. This needs to be synchronized as
     * writes can occur from multiple threads. We write in chunks to allow the
     * other side to synchronize for large sized writes.
     */
    private void writeChunks(OutputStream outputStream, byte[] bytes, int length)
            throws IOException {
        // Chunk size is 16K - this hack is for large
        // writes over slow connections.
        synchronized (outputStream) {
            // outputStream.write(bytes,0,length);
            int chunksize = 8 * 1024;
            for (int p = 0; p < length; p += chunksize) {
                int chunk = p + chunksize < length ? chunksize : length - p;
                outputStream.write(bytes, p, chunk);
            }
        }
        outputStream.flush();
    }

    private NetClient sendBytesClient(InetAddress receiverAddress, int contactPort, final String data) {
        final String key = makeKey(receiverAddress.getHostName(), contactPort);
        NetClient client = (NetClient) getSocket(key);

        try {
            if (client == null) {
                client = sipStack.networkLayer.createNetClientSocket();
                putSocket(key, client);
            }
        } catch (IOException ex) {
            logger.log(LogWriter.TRACE_WARN, "IOException occured " + ex.toString());
            try {
                client.close();
                return null;
            } catch (Exception e) {
                logger.log(LogWriter.TRACE_ERROR, "Exception occured " + e.toString());
                return null;
            }
        }
        client.setReconnectAttempts(max_retry);
        client.connect(contactPort, receiverAddress.getHostAddress(),new
                Handler<AsyncResult<NetSocket>>() {
                    @Override
                    public void handle(AsyncResult<NetSocket> netSocketAsyncResult) {
                        if (netSocketAsyncResult.succeeded()) {
                            NetSocket socket = netSocketAsyncResult.result();
                            putNetSocket(key,socket);
                            socket.write(data);
                        } else {
                            netSocketAsyncResult.cause().printStackTrace();
                        }
                    }
                });
        return client;
    }

    private NetClient sendBytesTLSClient(InetAddress receiverAddress, int contactPort, final String data) {
        final String key = makeKey(receiverAddress.getHostName(), contactPort);
        NetClient client = (NetClient) getSocket(key);

        try {
            if (client == null) {
                client = sipStack.networkLayer.createNetClientSocket();
                putSocket(key, client);
            }
        } catch (IOException ex) {
            logger.log(LogWriter.TRACE_WARN, "IOException occured " + ex.toString());
            try {
                client.close();
                return null;
            } catch (Exception e) {
                logger.log(LogWriter.TRACE_ERROR, "Exception occured " + e.toString());
                return null;
            }
        }
        client.setReconnectAttempts(max_retry);
        client.connect(contactPort, receiverAddress.getHostAddress(),new
                Handler<AsyncResult<NetSocket>>() {
                    @Override
                    public void handle(AsyncResult<NetSocket> netSocketAsyncResult) {
                        if (netSocketAsyncResult.succeeded()) {
                            NetSocket socket = netSocketAsyncResult.result();
                            putNetSocket(key,socket);
                            socket.write(data);
                        } else {
                            netSocketAsyncResult.cause().printStackTrace();
                        }
                    }
                });

        /**
        sendBytesTLSClient(receiverAddress,contactPort, bytes.toString());
        String key = makeKey(receiverAddress, contactPort);
        Socket clientSock = null;
        enterIOCriticalSection(key);

        try {
            clientSock = getSocket(key);

            while (retry_count < max_retry) {
                if (clientSock == null) {

                    clientSock = sipStack.getNetworkLayer()
                            .createSSLSocket(receiverAddress, contactPort,
                                    senderAddress);
                    SSLSocket sslsock = (SSLSocket) clientSock;

                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        logger.logDebug(
                                "inaddr = " + receiverAddress);
                        logger.logDebug(
                                "port = " + contactPort);
                    }
                    HandshakeCompletedListenerImpl listner = new HandshakeCompletedListenerImpl((TLSMessageChannel)messageChannel, clientSock);
                    ((TLSMessageChannel) messageChannel)
                            .setHandshakeCompletedListener(listner);
                    sslsock.addHandshakeCompletedListener(listner);
                    sslsock.setEnabledProtocols(sipStack
                            .getEnabledProtocols());

                    listner.startHandshakeWatchdog();
                    sslsock.startHandshake();
                    ((TLSMessageChannel)messageChannel).setHandshakeCompleted(true);
                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        this.logger.logDebug(
                                "Handshake passed");
                    }
                    // allow application to enforce policy by validating the
                    // certificate

                    try {
                        sipStack
                                .getTlsSecurityPolicy()
                                .enforceTlsPolicy(
                                        messageChannel
                                                .getEncapsulatedClientTransaction());
                    } catch (SecurityException ex) {
                        throw new IOException(ex.getMessage());
                    }

                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        this.logger.logDebug(
                                "TLS Security policy passed");
                    }
                    OutputStream outputStream = clientSock
                            .getOutputStream();
                    writeChunks(outputStream, bytes, length);
                    putSocket(key, clientSock);
                    break;
                } else {
                    try {
                        OutputStream outputStream = clientSock
                                .getOutputStream();
                        writeChunks(outputStream, bytes, length);
                        break;
                    } catch (IOException ex) {
                        if (logger.isLoggingEnabled())
                            logger.logException(ex);
                        // old connection is bad.
                        // remove from our table.
                        removeSocket(key);

                        try {
                            logger.logDebug(
                                    "Closing socket");
                            clientSock.close();
                        } catch (Exception e) {
                        }
                        clientSock = null;
                        retry_count++;
                    }
                }
            }
        } catch (SSLHandshakeException ex) {
            removeSocket(key);
            throw ex;
        } catch (IOException ex) {
            removeSocket(key);
        } finally {
            leaveIOCriticalSection(key);
        }
        if (clientSock == null) {
            throw new IOException("Could not connect to " + receiverAddress
                    + ":" + contactPort);
        } else
            return clientSock;**/
        return client;
    }

    private void sendBytesUDP(InetAddress receiverAddress, int contactPort, final String data) {
        DatagramSocket datagramSocket = null;

        String useServerSocket = sipStack.getConfigurationProperties().getProperty("org.openxsp.stack.use.server.socket", "true");
        if ("true".equals(useServerSocket)) {
            for (MessageProcessor messageProcessor : sipStack.getMessageProcessors()) {
                if (messageProcessor instanceof UDPMessageProcessor) {
                    String stack_ip = sipStack.getConfigurationProperties().getProperty("javax.sip.IP_ADDRESS");
                    String stack_port = sipStack.getConfigurationProperties().getProperty("org.openxsp.stack.PORT");
                    int port = ((UDPMessageProcessor) messageProcessor).getPort();
                    String ip = ((UDPMessageProcessor) messageProcessor).getIpAddress().toString();
                    ip = ip.replace("/", "");
                    if (ip.equals(stack_ip) && port == Integer.parseInt(stack_port)) {
                        ((UDPMessageProcessor) messageProcessor).getSocket().send(data, receiverAddress.getHostAddress(), contactPort, new Handler<AsyncResult<DatagramSocket>>() {
                            @Override
                            public void handle(AsyncResult<DatagramSocket> datagramSocketAsyncResult) {
                                logger.log(LogWriter.TRACE_INFO, "Send data via UDP, result :" + datagramSocketAsyncResult.succeeded());
                            }
                        });
                        return;
                    }
                }
            }
        }

        try {
            if (receiverAddress instanceof Inet4Address) {
                datagramSocket = sipStack.getNetworkLayer()
                        .createDatagramSocket(InternetProtocolFamily.IPv4);
            } else {
                datagramSocket = sipStack.getNetworkLayer()
                        .createDatagramSocket(InternetProtocolFamily.IPv6);
            }
        } catch (IOException io) {
            logger.log(LogWriter.TRACE_WARN, "IOException occured " + io.toString());
            try {
                datagramSocket.close();
                return;

            } catch (Exception e) {
                logger.log(LogWriter.TRACE_ERROR, "Exception occured " + e.toString());
                return;
            }
        }

        datagramSocket.send(data,receiverAddress.getHostAddress(),contactPort,new Handler<AsyncResult<DatagramSocket>>() {
            @Override
            public void handle(AsyncResult<DatagramSocket> datagramSocketAsyncResult) {
                logger.log(LogWriter.TRACE_INFO, "Send data via UDP, result :" + datagramSocketAsyncResult.succeeded());
            }
        });
        datagramSocket.close();
    }

    /**
     * Send an array of bytes.
     *
     * @param receiverAddress
     *            -- inet address
     * @param contactPort
     *            -- port to connect to.
     * @param transport
     *            -- tcp or udp.
     * @param isClient
     *            -- retry to connect if the other end closed connection
     * @throws IOException
     *             -- if there is an IO exception sending message.
     */

    public Object sendBytes(InetAddress senderAddress,
            InetAddress receiverAddress, int contactPort, Transport transport,
            byte[] bytes, boolean isClient)
            throws IOException {

        // Server uses TCP transport. TCP client sockets are cached
        int length = bytes.length;
        String data = new String(bytes);
        System.out.println(data);
      /*  logger.log(LogWriter.TRACE_DEBUG,
            "sendBytes " + transport + " local inAddr "
                    + senderAddress.getHostAddress() + " remote inAddr "
                    + receiverAddress.getHostAddress() + " port = "
                    + contactPort + " length = " + length + " isClient " + isClient );

        if (logger.isLoggingEnabled(LogLevels.TRACE_INFO)
                && sipStack.isLogStackTraceOnMessageSend()) {
            logger.logStackTrace(StackLogger.TRACE_INFO);
        }*/

        if (transport == Transport.TCP) {
            return sendBytesClient(receiverAddress,contactPort, data);
        } else if (transport == Transport.TLS) {
            return sendBytesTLSClient(receiverAddress,contactPort, data);
        } else {
            sendBytesUDP(receiverAddress, contactPort, data);
        }
        Object temp = new Object();
        return temp;
    }

    /*
     * private void enterIOCriticalSection(String key) throws IOException { try
     * { if ( ! this.ioSemaphore.tryAcquire(10,TimeUnit.SECONDS) ) { throw new
     * IOException("Could not acquire semaphore"); } } catch
     * (InterruptedException e) { throw new
     * IOException("exception in acquiring sem"); } }
     *
     *
     * private void leaveIOCriticalSection(String key) {
     * this.ioSemaphore.release(); }
     */

    /**
     * Close all the cached connections.
     */
    public void closeAll() {
        logger.log(LogWriter.TRACE_DEBUG, "Closing " + socketTable.size() + " sockets from IOHandler");

        for (Enumeration<Object> values = socketTable.elements(); values.hasMoreElements();) {
            Object s = values.nextElement();
            if (s instanceof NetClient)
                ((NetClient) s).close();
            else {
                if (s instanceof NetServer)
                    ((NetServer) s).close();
                else
                    logger.log(LogWriter.TRACE_ERROR, "Error: Entry in a unknown Object");
            }
        }

        for (Enumeration<NetSocket> values = netSocketTable.elements(); values.hasMoreElements();) {
            NetSocket s = values.nextElement();
            s.close();
        }
    }
}
