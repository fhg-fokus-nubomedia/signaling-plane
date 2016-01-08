package org.openxsp.stack;

/**
 * Created by Frank Schulze on 10.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */


import org.openxsp.java.Verticle;
import org.vertx.java.core.json.JsonObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Properties;

/**
 * Please only use UDP, really only UDP!
 */
public class Start extends Verticle {


    private final String transport = "UDP";
    private SessionControl sessionControl;
    private ServerSocket tcpServerSocket;

    @Override
    public void start() {
    	try{
    		JsonObject config = container.config();
            String configIP = config.getString("bind");
            String configPort = config.getString("port");
            String configDomain = config.getString("domain");
            String configIMSIP  = config.getString("ims_ip");
            String configIMSPCSCF = config.getString("ims_pcscf");
            String configIMSSCSCF = config.getString("ims_scscf");
            String configIMSICSCF = config.getString("ims_icscf");
            //XXX fsc String configIMSDomain = config.getString("ims_domain");

            Properties properties = new Properties();
            
            try{
            	 properties.setProperty("javax.sip.OUTBOUND_PROXY", configIP + ":" + configPort + "/" + transport);
                 properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "modules/java_sip/log/sipstack.log");
                 properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");
                 properties.setProperty("javax.sip.STACK_NAME", "OpenXSP-SipStack");
                 properties.setProperty("javax.sip.IP_ADDRESS", configIP);
                 properties.setProperty("gov.nist.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING","false");

                 properties.setProperty("org.openxsp.stack.HOST_IP", configIP);
                 properties.setProperty("org.openxsp.stack.PORT", configPort);
                 properties.setProperty("org.openxsp.stack.TRANSPORT", transport);
                 properties.setProperty("org.openxsp.stack.DOMAIN", configDomain);
                 properties.setProperty("org.openxsp.stack.IMSPCSCF", configIMSPCSCF);
                 properties.setProperty("org.openxsp.stack.IMSSCSCF", configIMSSCSCF);
                 properties.setProperty("org.openxsp.stack.IMSICSCF", configIMSICSCF);
                 properties.setProperty("org.openxsp.stack.IMSIP", configIMSIP);
                 //XXX fsc properties.setProperty("org.openxsp.stack.IMSDOMAIN", configIMSDomain);
                 properties.setProperty("org.openxsp.stack.IMSDOMAIN", configDomain);
                 properties.setProperty("org.openxsp.stack.use.server.socket", "true");
                 
            }catch(NullPointerException e){
            	System.out.println("Couldn't load all configuration paramters");
            	e.printStackTrace();
            	return;
            }
            
           
            
            this.sessionControl = new SessionControl(properties, openxsp);
            
            try {
                this.sessionControl.init();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            String host = properties.getProperty("org.openxsp.stack.HOST_IP");
            int port = Integer.valueOf(properties.getProperty("org.openxsp.stack.PORT"));
            
            startDummySocketServer(host, port);
    	}catch (Exception e){
    		System.out.println("Error starting module");
    		e.printStackTrace();
    	}
        

    }

    /*
     * Starts a server socket that doesnt serve any requests. It is basically only used 
     * as an endpoint for clients that want to check their reachability to the module. SIP
     * signaling still needs to be done via UDP.
     * @param host
     * @param port
     */
	private void startDummySocketServer(String host, int port) {
		
    	try {
    		tcpServerSocket = new ServerSocket();
    		tcpServerSocket.bind(new InetSocketAddress(host, port));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() {
		super.stop();
		
		if(tcpServerSocket!=null && !tcpServerSocket.isClosed())
			try {
				tcpServerSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}