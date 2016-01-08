package test;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.ims.ImsException;
import javax.ims.ServiceClosedException;
import javax.ims.core.CoreService;
import javax.ims.core.CoreServiceListener;
import javax.ims.core.Message;
import javax.ims.core.PageMessage;
import javax.ims.core.PageMessageListener;
import javax.ims.core.Reference;
import javax.ims.core.Session;
import javax.ims.core.SessionListener;
import javax.ims.core.media.FramedMedia;
import javax.ims.core.media.FramedMediaListener;
import javax.ims.core.media.Media;
import javax.ims.core.media.StreamMedia;

import org.junit.Test;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

import de.fhg.fokus.ims.core.CoreServiceImpl;
import de.fhg.fokus.ims.core.IMSConfigurator;
import de.fhg.fokus.ims.core.IMSManager;
import de.fhg.fokus.ims.core.IMSProfile;
import de.fhg.fokus.ims.core.net.NetworkService;
import de.fhg.fokus.microedition.io.Connector;

public class IMSConnectorVerticleTest extends TestVerticle implements CoreServiceListener{

	private static final Logger LOGGER = LoggerFactory.getLogger(IMSConnectorVerticleTest.class);
	private static String TAG = "IMSConectorVerticleTest: "; 

	protected CoreServiceImpl coreservice;
	protected IMSProfile profile;
	protected Vector<Session> sessions = new Vector<Session>();
	
	@Override
	public void start() {
		initialize();		
		
		startTests();
	}

	@Test
	public void testInitializeCoreService() {		
		/*opens the coreservice - initializes the sip stack and 
		if register is set to true on the profile, a registration will be sent out*/	
		try {			
			this.profile = new IMSProfile(getDefaultProfile());
			IMSConfigurator configurator = new IMSConfigurator(this.profile);

			configurator.configure(IMSManager.getInstance(), new NetworkService());			

			IMSManager.getInstance().start(vertx);
			
			coreservice = (CoreServiceImpl) Connector.open("imscore://openxsp");
			coreservice.setListener(this);
			
			VertxAssert.assertEquals(true, Boolean.valueOf(coreservice.isConnected()));
			
			testSendMessage();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}	
	}	
	
	@Test
	  public void testCloseCoreService() {
		if (coreservice != null)
		{
			coreservice.close();
			coreservice = null;
		}
	    VertxAssert.testComplete();
	  }
	
	
	//@Test
	public void testSendMessage()
	{
		String from = "alice";
		String to = "boo";
		String msg = "I am Testing!";
		
		if (coreservice == null || !coreservice.isConnected())
			return;
		
		PageMessage pageMessage;
		try
		{
			pageMessage = coreservice.createPageMessage(fixUserURI(from), fixUserURI(to));
			pageMessage.setListener(new PageMessageListener() {						
				public void pageMessageDelivered(PageMessage pagemessage)
				{
					System.out.print("the pagemessage has been successfully delivered");
					VertxAssert.assertEquals(PageMessage.STATE_SENT, pagemessage.getState());
				}

				public void pageMessageDeliveredFailed(PageMessage pagemessage)
				{
					System.out.print("the pagemessage was NOT delivered");
					VertxAssert.assertEquals(PageMessage.STATE_UNSENT, pagemessage.getState());
				}
			});
			pageMessage.send(msg.toString().getBytes(), "text/plain");
		} catch (ServiceClosedException e)
		{
			LOGGER.error(e.getMessage(), e);
		} catch (IllegalStateException e)
		{
			LOGGER.error(e.getMessage(), e);
		} catch (IllegalArgumentException e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	//@Test
	public void testCallSession(){
		
		String caller = "alice";
		String callee = "boo";
		
		if (coreservice == null || !coreservice.isConnected())
			return;
		try
		{
			Session session = coreservice.createSession(fixUserURI(caller), fixUserURI(callee));
			session.setListener(new SessionListener() {
				
				@Override
				public void sessionUpdated(Session session) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void sessionUpdateReceived(Session session) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void sessionUpdateFailed(Session session) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void sessionTerminated(Session session) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void sessionStarted(Session session) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void sessionStartFailed(Session session) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void sessionReferenceReceived(Session session, Reference reference) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void sessionAlerting(Session session) {
					// TODO Auto-generated method stub
					
				}
			});
			StreamMedia myMedia = (StreamMedia) session.createMedia("StreamMedia", Media.DIRECTION_SEND_RECEIVE);
			if(myMedia != null)
			{
				myMedia.setStreamType(StreamMedia.STREAM_TYPE_VIDEO);
				myMedia.setSource("capture://video");				
			}
			sessions.add(session);
			session.start();
		} catch (ImsException ie)
		{
			LOGGER.error(ie.getMessage(), ie);
		} catch (ServiceClosedException e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	private String fixUserURI(String x)
	{
		if (x.substring(0, 1).equals("+"))
			x = "tel:" + x;
		else if (!x.substring(0, 3).equalsIgnoreCase("sip:") && !x.substring(0, 3).equalsIgnoreCase("tel:"))
			x = "sip:" + x;
		if (x.indexOf('@') < 0 && x.indexOf('.') < 0 && x.indexOf("tel:") < 0)
			x = x + "@" + profile.getDomain();
		return x;
	}

	private JsonObject getDefaultProfile(){
		Map<String, Object> obj = new HashMap<String, Object>();

		LOGGER.info( TAG+"getting default profile");
		obj.put("imsDomain", "open-ims.test");
		obj.put("imsLocalInterface", "10.147.65.128");
		obj.put("imsDisplayName", "Alice");
		obj.put("imsRegister", "false");
		obj.put("imsDisplayName", "alice");
		obj.put("imsSubscribeToReg", "false");
		obj.put("imsPrack", "false");
		obj.put("imsLocalPort", "5061");
		obj.put("imsPCSCFPort", "5061");
		obj.put("imsICSCFPort", "5060");
		obj.put("imsSCSCFPort", "6060");
		obj.put("imsRegisterTime", "3600)");
		obj.put("imsEnableLog",  "false");
		obj.put("imsPrivateId", "alice1@open-ims.test");
		obj.put("imsPCSCFAddress", "10.147.65.128");//10.147.66.199
		obj.put("imsPublicId", "sip:alice1@open-ims.test");
		obj.put("sessionExpireTimer", "3600");

		return new JsonObject(obj);
	}

	// ------ CoreService Event Handlers ------//
	
	@Override
	public void pageMessageReceived(CoreService coreservice,
			PageMessage pagemessage) {
		System.out.print("\nMessage ");
		System.out.print("From: " + pagemessage.getRemoteUserId());
		System.out.println("ContentType: " + pagemessage.getContentType());
		System.out.println(new String(pagemessage.getContent()));

	}

	@Override
	public void sessionInvitationReceived(CoreService coreservice,
			Session session) {
		
		session.setListener(new SessionListener() {
			
			@Override
			public void sessionUpdated(Session session) {
				System.out.println("session updated");
				
			}
			
			@Override
			public void sessionUpdateReceived(Session session) {
				System.out.println("session update received");
				
			}
			
			@Override
			public void sessionUpdateFailed(Session session) {
				Message msg = session.getPreviousResponses(Message.SESSION_START)[0];
				System.out.println("session start failed: status code:" + msg.getStatusCode() + " reason:" + msg.getReasonPhrase());

				sessions.remove(session);
				
			}
			
			@Override
			public void sessionTerminated(Session session) {
				Message msg = session.getPreviousResponses(Message.SESSION_TERMINATE)[0];
				System.out.println("session terminated: status code:" + msg.getStatusCode() + " reason:" + msg.getReasonPhrase());
				sessions.remove(session);
				
			}
			
			@Override
			public void sessionStarted(Session session) {
				System.out.println("session started");

				Media[] media = session.getMedia();
				for (int i = 0; i < media.length; i++)
				{
					if (media[i] instanceof FramedMedia)
						((FramedMedia) media[i]).setListener(new FramedMediaListener() {
							
							@Override
							public void transferProgress(FramedMedia media, String messageId,
									int bytesTransferred, int bytesTotal) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void deliverySuccess(FramedMedia media, String messageId) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void deliveryFailure(FramedMedia media, String messageId,
									int statusCode, String reasonPhrase) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void contentReceived(FramedMedia media, String messageId, int size,
									String fileName) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void contentReceiveFailed(FramedMedia media, String messageId) {
								// TODO Auto-generated method stub
								
							}
							
							@Override
							public void connectionError(FramedMedia media) {
								// TODO Auto-generated method stub
								
							}
						});
				}
			}
			
			@Override
			public void sessionStartFailed(Session session) {
				Message msg = session.getPreviousResponses(Message.SESSION_START)[0];
				System.out.println("session update failed: status code:" + msg.getStatusCode() + " reason:" + msg.getReasonPhrase());
			}
			
			@Override
			public void sessionReferenceReceived(Session session, Reference reference) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void sessionAlerting(Session session) {
				// TODO Auto-generated method stub
				
			}
		});
		sessions.add(session);
		LOGGER.info("Session received");

	}

	@Override
	public void referRequestReceived(CoreService coreservice,
			Reference reference) {
		System.out.println("Received reference: " + reference.getReferMethod() + " " + reference.getReferToUserId());
		try
		{
			reference.reject();
		} catch (ServiceClosedException e)
		{
			LOGGER.error(e.getMessage(), e);
		}

	}

	@Override
	public void unsolicitedNotifyReceived(CoreService coreservice, String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void serviceClosed(CoreService service) {
		// TODO Auto-generated method stub

	}


}
