/**
 * Created by Lukas Woellner on 21.03.14.
 * lukas dot woellner at fokus dot fraunhofer dot de
 */

package gov.nist.core.vertx;


import gov.nist.javax.sip.SipStackImpl;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.datagram.DatagramSocket;
import org.vertx.java.core.datagram.InternetProtocolFamily;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetServer;

import java.io.IOException;
import java.net.SocketException;


public class DefaultVertxNetworkLayer implements VertxNetworkLayer {

    private final Vertx vertx;
    
    public DefaultVertxNetworkLayer(Vertx vertx) {
    	this.vertx = vertx;
    }

    /**
     * @param backlog
     * @return NetServer
     * @throws IOException
     */
	public NetServer createServerSocket(int backlog)
            throws IOException {
		return this.vertx.createNetServer().setAcceptBacklog(backlog);
	}

    @Override
    public NetServer createServerSocket() throws IOException {
        return this.vertx.createNetServer();
    }

    public NetServer createServerSochet()
        throws  IOException {
        return this.vertx.createNetServer();
    }

    /**
     * @return NetClient
     * @throws IOException
     */
    public NetClient createNetClientSocket()
            throws IOException {
        return this.vertx.createNetClient();
    }

    /**
     * @param backlog
     * @param clientTrust if false it trust each client CA
     * @return
     * @throws IOException
     */
	public NetServer createSSLServerSocket(int backlog, boolean clientTrust)
            throws IOException {
        if (!clientTrust) {
            return this.vertx.createNetServer()
                    .setSSL(true)
                    .setAcceptBacklog(backlog)
                    .setClientAuthRequired(false);
        } else {
            return this.vertx.createNetServer()
                    .setSSL(true)
                    .setAcceptBacklog(backlog)
                    .setClientAuthRequired(true);
        }
	}

    /**
     * @param serverTrust if false it trust each server CA, and this is bad while
     *                    it makes man in the middle attacks possible.
     * @return
     * @throws IOException
     */
    public NetClient createSSLNetClient(boolean serverTrust)
            throws IOException {
        if (!serverTrust) {
            return this.vertx.createNetClient()
                    .setSSL(true)
                    .setTrustAll(false);
        } else {
            return this.vertx.createNetClient()
                    .setSSL(true)
                    .setTrustAll(false);
        }
    }

    /**
     * @param type defines the internet protocol family (IPv4 or IPv6)
     * @return
     * @throws SocketException
     */
	public DatagramSocket createDatagramSocket(InternetProtocolFamily type) throws SocketException {
        return this.vertx.createDatagramSocket(type);
	}

	@Override
	public void setSipStack(SipStackImpl sipStackImpl) {
		// TODO Auto-generated method stub
		
	}

}
