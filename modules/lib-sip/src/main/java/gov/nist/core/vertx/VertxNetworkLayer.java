/**
 * Created by Lukas Woellner on 21.03.14.
 * lukas dot woellner at fokus dot fraunhofer dot de
 */

package gov.nist.core.vertx;

import gov.nist.javax.sip.SipStackImpl;

import java.io.IOException;
import java.net.SocketException;


import org.vertx.java.core.datagram.InternetProtocolFamily;
import org.vertx.java.core.net.NetServer;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.datagram.DatagramSocket;

public interface VertxNetworkLayer {

    /**
     * @param backlog
     * @return the NetServer
     * @exception IOException
     */
    public abstract NetServer createServerSocket(int backlog) throws IOException;

    public abstract  NetServer createServerSocket() throws IOException;

    /**
     * @return NetClient
     * @throws IOException
     */
    public abstract NetClient createNetClientSocket() throws IOException;

    /**
     * @param backlog
     * @param clientTrust if false it trust each client CA
     * @return the NetServer
     */
    public abstract NetServer createSSLServerSocket(int backlog, boolean clientTrust)
            throws IOException;

    /**
     *
     * @param serverTrust if false it trust each server CA, and this is bad while
     *                    it makes man in the middle attacks possible.
     * @return NetClient
     * @throws IOException
     */
    public abstract NetClient createSSLNetClient(boolean serverTrust)
            throws IOException;

    /**
     * @param type defines the internet protocol family (IPv4 or IPv6)
     * @return the datagram socket
     */
    public abstract DatagramSocket createDatagramSocket(InternetProtocolFamily type) throws SocketException;


    /**
     * Set the sip stack impl so that the network layer can access it to query properties
     * @param sipStackImpl
     */
    public abstract void setSipStack(SipStackImpl sipStackImpl);
}
