package org.openxsp.stack;

import java.text.ParseException;
import java.util.ListIterator;
import java.util.Properties;
import java.util.UUID;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransportNotSupportedException;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.openxsp.java.EventBus;
import org.openxsp.java.OpenXSP;
import org.openxsp.java.RPCResult;
import org.openxsp.java.RPCResultHandler;
import org.openxsp.java.session.SessionControlAcceptSession;
import org.openxsp.java.session.SessionControlCreateSession;
import org.openxsp.java.session.SessionControlEventAddress;
import org.openxsp.java.session.SessionControlMessage;
import org.openxsp.java.session.SessionControlMessgeImpl;
import org.openxsp.java.session.SessionControlMethodEnum;
import org.openxsp.java.session.eventbus.EventBusReceiver;
import org.openxsp.java.session.eventbus.EventBusReceiverListener;
import org.openxsp.java.session.eventbus.EventBusResult;
import org.openxsp.java.session.eventbus.EventBusResultListener;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by Frank Schulze on 17.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */

public class SessionControl implements Listener, EventBusReceiverListener, EventBusResultListener {

    //fix parameter
    private final static int MIN_PORT = 1;
    private final static int MAX_PORT = 65536;
    private final static int MIN_URI_PARTS = 3;
    private final static int USER_NAME = 1;
    private final static int DOMAIN_NAME = 2;


    //Stack
    private SipStack sipStack; //for the SipStack
    private EventBus eventBus; //for the EventBus
    private ListeningPoint listeningPoint;
    private SipProvider sipProvider;
    private Mapping mapping;

    //OpenXSP
    private OpenXSP openxsp;
    private Properties properties;

    //Translate
    private TranslateSipToEB sipToEB;
    private StateEnum state = StateEnum.NULL;
    private State stateMachine;

    //Addressing
    private String moduleID;

    public  SessionControl(Properties properties, OpenXSP openxsp) {
        this.openxsp = openxsp;
        this.properties = properties;
        this.sipToEB = new TranslateSipToEB();
        this.mapping = new Mapping();
        this.stateMachine = new State();
        this.moduleID = UUID.randomUUID().toString();
    }

    /*
     * Private Methods
     */
    private String getUserKey(User user) {
        if (user == null)
            return null;
        Integer i = user.getSipURIAddress().toString().hashCode();
        return i.toString();
    }

    //Use the URI as key!!!
    private String getUserKey(String uri) {
        if (uri == null)
            return null;
        Integer i = uri.hashCode();
        return i.toString();
    }

    private String getMessageKey(User a, User b) {
        if (a == null || b == null)
            return null;
        Integer ia = a.hashCode();
        Integer ib = b.hashCode();
        return (ia += ib).toString();
    }

    private String getSipProviderKey(SipProvider sipProvider) {
        if (sipProvider == null)
            return null;
        Integer i = sipProvider.hashCode();
        return i.toString();
    }

    private String getReceiverKey(Receiver receiver) {
        if (receiver == null)
            return null;
        Integer i = receiver.hashCode();
        return i.toString();
    }

    private String getReceiverKey(EventBusReceiver eventBusReceiver) {
        if (eventBusReceiver == null)
            return null;
        Integer i = eventBusReceiver.hashCode();
        return i.toString();
    }

    private String getResultKey(EventBusResult eventBusResult) {
        if (eventBusResult == null)
            return null;
        Integer i = eventBusResult.hashCode();
        return i.toString();
    }

    private User createUserFromSipURI(URI uri, int port) throws InvalidArgumentException{
        String phrase = uri.toString();
        String delims = "[:@]+";
        String[] uriParts = phrase.split(delims);
        if (uriParts.length < MIN_URI_PARTS)
            throw new InvalidArgumentException("Invalid URI :" + phrase);
        return createUser(uriParts[USER_NAME],uriParts[DOMAIN_NAME], null, port);
    }

    private User createUserFromSipURI(URI uri, String ip, int port) throws InvalidArgumentException{
        String phrase = uri.toString();
        String delims = "[:@]+";
        String[] uriParts = phrase.split(delims);
        if (uriParts.length != MIN_URI_PARTS)
            throw new InvalidArgumentException("Invalid URI :" + phrase);
        return createExternUser(uriParts[USER_NAME], uriParts[DOMAIN_NAME], null, ip, port);
    }

    private String getUserNameFromEB(String uri) {
        String delims = "[:@]+";
        String[] uriParts = uri.split(delims);
        if (uriParts.length != MIN_URI_PARTS) {
           return uriParts[0]; //this is a presumption that this is the name
        }
        return uriParts[1];
    }

    private String getDomainFromEB(String uri) {
        String delims = "[:@]+";
        String[] uriParts = uri.split(delims);
        if (uriParts.length != MIN_URI_PARTS) {
            return this.properties.getProperty("org.openxsp.stack.DOMAIN");
        }

        return uriParts[2];
    }

    private User createUser(String userName, String sipDomain, String displayName, int port) {
        User tmp;
        try {
            tmp = new User(this.sipStack,  properties.getProperty("org.openxsp.stack.HOST_IP"), port);
            tmp.setName(userName);
            tmp.setSipProvider(sipDomain);

            if (displayName != null)
                tmp.setDisplayName(displayName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return tmp;
    }

    private User createExternUser(String userName, String sipDomain, String displayName, String host, int port) {
        User tmp;
        try {
            tmp = new User(this.sipStack, host, port);
            tmp.setName(userName);
            tmp.setSipProvider(sipDomain);

            if (displayName != null)
                tmp.setDisplayName(displayName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return tmp;
    }


    private void initSip()
        throws Exception {
        String server = properties.getProperty("org.openxsp.stack.HOST_IP");
        int port = Integer.parseInt(properties.getProperty("org.openxsp.stack.PORT"));
        String transport = properties.getProperty("org.openxsp.stack.TRANSPORT");
        Receiver receiver;

        if (server == null)
            throw new Exception("missing HOST_IP property");
        if (port < MIN_PORT || port > MAX_PORT)
            throw new Exception("PORT property is out of bound");
        if (transport == null)
            throw new Exception("missing TRANSPORT property");

        //create a SipStack
        sipStack = createSipSack(properties);
        System.out.println("Stack :" + sipStack.hashCode());
        if (sipStack == null)
            throw new Exception("Failed to create a new sipStack");

        //create ListeningPoint
        try {
            if ((listeningPoint = sipStack.createListeningPoint(server, port, transport)) == null) {
                throw new Exception("Fail to create a ListeningPoint");
            }
            
        } catch (TransportNotSupportedException ex) {
            throw new Exception("TransportNotSupportedException for ListeningPoint : " + ex.toString());

        } catch (InvalidArgumentException ex) {
            throw new Exception("InvalidArgumentException for ListeningPoint : " + ex.toString());
        }
        
        //create sipProvider
        try {
            sipProvider = sipStack.createSipProvider(listeningPoint);
            sipProvider.setAutomaticDialogSupportEnabled(false);
            receiver = new Receiver(sipProvider, this);
            sipProvider.addSipListener(receiver);
        } catch (ObjectInUseException ex) {
            throw new Exception("Failed to create a SipProvider", ex);
        }
    }

    private SipStack createSipSack(Properties properties) {
    	SipStack sipStack;
        HeaderFactory headerFactory;
        AddressFactory addressFactory;
        MessageFactory messageFactory;
        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        try {
            sipStack = sipFactory.createSipStack(properties, this.openxsp.getVertx());

            headerFactory = sipFactory.createHeaderFactory();
            sipStack.setHeaderFactory(headerFactory);

            addressFactory = sipFactory.createAddressFactory();
            sipStack.setAddressFactory(addressFactory);

            messageFactory = sipFactory.createMessageFactory();
            sipStack.setMessageFactory(messageFactory);

        } catch (PeerUnavailableException pue) {
            pue.printStackTrace();
            return null;
        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        }

        return sipStack;
	}

	private void initEvents(String event) {
        try {
            EventBusReceiver receiver = new EventBusReceiver(this.eventBus,this, event);
            receiver.registerEvent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EventBusResult createEventPoint(String address) {
        EventBusReceiver receiver = new EventBusReceiver(this.eventBus, this, address);
        // receiver.registerEvent(result);
        return new EventBusResult(this.eventBus, this);
    }


    private void initEventBus() throws Exception{ //Todo(lwo): never stop that feeling
        eventBus = this.openxsp.eventBus();
        initEvents(SessionControlEventAddress.SIP_ADDRESS.getEvent());//Global Address
        initEvents(SessionControlEventAddress.SIP_ADDRESS.getEvent() + ":" + this.moduleID);//Module Address
        initEvents(SessionControlEventAddress.SIP_ADDRESS.getEvent() + ":" + SessionControlEventAddress.KeepAlive); //KeepAlive Address for Module Registry
    }

    private Session getSession(String callId, String EBSessionID) {
        if (callId != null)
            return this.mapping.getSessionByCallId(callId);
        if (EBSessionID != null)
            return this.mapping.getSessionByEBSessionId(EBSessionID);
        return null;
    }

    private Session createSession(User from, User to) {
        try {
            return new Session(this.listeningPoint, from, to, this.sipProvider);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * Public Methods
     */
    public void init() throws Exception {
        this.initSip();
        this.initEventBus();
    }

    public void close() {
        this.mapping.clearAll();
        this.stateMachine = new State();
    }

    /*
     * Listener Sip
     */
    @Override
    public void processRequest(SipProvider sipProvider, SessionControl sessionControl, RequestEvent requestEvent) {
        Session session = null;
        CallIdHeader callID = (CallIdHeader) requestEvent.getRequest().getHeader(EBHeader.CallId.getValue());

        //its a Register
        if (requestEvent.getRequest().getMethod().compareTo(Request.REGISTER) != 0) {
            session = getSession(callID.getCallId(), null);
            
            if (session == null) {
                try {
                    ViaHeader via = (ViaHeader) requestEvent.getRequest().getHeader("Via");
                    int port = via.getPort();
                    FromHeader fromHeader = (FromHeader) requestEvent.getRequest().getHeader(EBHeader.From.getValue());
                    User from = createUserFromSipURI(fromHeader.getAddress().getURI(),port);
                    ToHeader toHeader = (ToHeader) requestEvent.getRequest().getHeader(EBHeader.To.getValue());
                    User to = createUserFromSipURI(toHeader.getAddress().getURI(),
                            Integer.parseInt(properties.getProperty("org.openxsp.stack.PORT")));
                    session = this.createSession(from, to);
                    session.setCallID(callID.getCallId());
                    session.createEBSessionId();
                    session.setLocalAddress(SessionControlEventAddress.SIP_ADDRESS.getEvent() + ":" + this.moduleID);
                    mapping.addSession(session);
                } catch (InvalidArgumentException e) {
                    e.printStackTrace();
                }
            }
        } else try {
            ListIterator vias = requestEvent.getRequest().getHeaders("Via");
            ViaHeader via = (ViaHeader) requestEvent.getRequest().getHeader("Via");
            ContactHeader con = (ContactHeader) requestEvent.getRequest().getHeader("Contact");
            String host = via.getHost();
            String branch = via.getBranch();
            int port = via.getPort();
            CSeqHeader cSeqHeader = (CSeqHeader) requestEvent.getRequest().getHeader("CSeq");
            FromHeader fromHeader = (FromHeader) requestEvent.getRequest().getHeader(EBHeader.From.getValue());
            User from = createUserFromSipURI(fromHeader.getAddress().getURI(), host, port);
            from.setContactHeader(con);

            session = this.createSession(from, from);
            session.setCallID(callID.getCallId());
            session.createEBSessionId();
            mapping.addSession(session);

            Response x = session.createResponse(Response.OK, branch, cSeqHeader.getSeqNumber(), vias, con);
            // requestEvent.getServerTransaction().sendResponse(x);

            this.sipProvider.sendResponse(x);
            //mapping.addSession(session); not know we don't need it in the moment
        } catch (InvalidArgumentException | ParseException | SipException e) {
            e.printStackTrace();
        }
        assert session != null;
        EventBusResult result = new EventBusResult(this.eventBus,this);
        if ( requestEvent.getRequest().getMethod().compareTo(Request.INVITE) == 0) {
            JsonObject message = sipToEB.inviteToInit(requestEvent, session);
            session.setCallBackAddress(null); //make sure the address is null
            if (!session.updateStateFromSipRequest(requestEvent.getRequest().getMethod()))
                return;
           //send to general Address
          
            EventBusResult resultCallBack = new EventBusResult(this.eventBus,this);
            resultCallBack.setResultId(session.getEBSessionID());
            session.setCurrentRequest(requestEvent.getRequest());
            
            //get RR and save it
           	session.createRecordRouteList(requestEvent.getRequest());
           
            System.out.println("Send to Message :" + message);
            eventBus.invoke(SessionControlEventAddress.RTC_ADDRESS.getEvent(), " ", message, result);
            return;
        }
        if ( requestEvent.getRequest().getMethod().compareTo(Request.BYE) == 0) {
            if (!session.updateStateFromSipRequest(requestEvent.getRequest().getMethod()))
                return;
            //XXX
            JsonObject json = sipToEB.byeToEnd(requestEvent, session);
            String callbackAddress = session.getCallBackAddress();
            System.out.println("Sending message "+json+" to "+callbackAddress);
            
            if(callbackAddress==null){
            	System.out.println("!!!!!! No callback set !!!!!");
            	return;
            }
            eventBus.invoke(callbackAddress, " ", json, result);
          //  eventBus.send(session.getCallBackAddress(), sipToEB.byeToEnd(requestEvent, session));
            return;
        }
        if ( requestEvent.getRequest().getMethod().compareTo(Request.CANCEL) == 0) {
            if (!session.updateStateFromSipRequest(requestEvent.getRequest().getMethod()))
                return;
            eventBus.invoke(session.getCallBackAddress(), " ", sipToEB.cancelToCancel(requestEvent, session), result);
          //  eventBus.send(session.getCallBackAddress(), sipToEB.cancelToCancel(requestEvent, session));
            //clear session
            return;
        }
        if ( requestEvent.getRequest().getMethod().compareTo(Request.UPDATE) == 0) {
            if (!session.updateStateFromSipRequest(requestEvent.getRequest().getMethod()))
                return;
            eventBus.invoke(session.getCallBackAddress(), " ", sipToEB.updateToUpdate(requestEvent, session), result);
           // eventBus.send(session.getCallBackAddress(), sipToEB.updateToUpdate(requestEvent, session));
            return;
        }
        if ( requestEvent.getRequest().getMethod().compareTo(Request.ACK) == 0) {
            if (!session.updateStateFromSipRequest(requestEvent.getRequest().getMethod())) return;
            
            //XXX
            JsonObject json = sipToEB.ackToConfirm(requestEvent, session);
            String callbackAddress = session.getCallBackAddress();
            System.out.println("Sending message "+json+" to "+callbackAddress);
            
            if(callbackAddress==null){
            	System.out.println("!!!!!! No callback set !!!!!");
            	return;
            }
            
            eventBus.invoke(callbackAddress, " ", json, result);
            //eventBus.send(session.getCallBackAddress(), sipToEB.ackToConfirm(requestEvent, session));
        }
    }

    @Override
    public void processResponse(SipProvider sipProvider, SessionControl sessionControl, ResponseEvent responseEvent) {
    	CallIdHeader callIDHeader = (CallIdHeader) responseEvent.getResponse().getHeader(EBHeader.CallId.getValue());
        String callID = callIDHeader.getCallId(); 
        Session session = getSession(callID, null);
        EventBusResult result = new EventBusResult(this.eventBus,this);
        if (session == null)
            return; //ToDo(lwo): send cancel on sip
        int statusCode = responseEvent.getResponse().getStatusCode();
        if (statusCode == Response.OK) {//200 we ignore 100 
            if (!session.updateStateFromSipResponse(responseEvent.getResponse()))
                return;       
            session.getTo().setContactHeader((ContactHeader) responseEvent.getResponse().getHeader("Contact"));
        	session.createRecordRouteList(responseEvent.getResponse());
            eventBus.invoke(session.getCallBackAddress(), " ", sipToEB.okToAccept(responseEvent, session), (RPCResultHandler) result);
        }
    }

    @Override
    public void processTimeout(SipProvider sipProvider, SessionControl sessionControl, TimeoutEvent timeoutEvent) {
        Session session = getSession(timeoutEvent.getClientTransaction().getDialog().getCallId().toString(), null);

        eventBus.publish(SessionControlEventAddress.RTC_ADDRESS.getEvent() + ":" + session.getEBSessionID(),
                sipToEB.cancelToCancel(session));
    }

    @Override
    public void processIOException(SipProvider sipProvider, SessionControl sessionControl, IOExceptionEvent exceptionEvent) {
        System.out.println("processIOException handle not implemented");
    }

    @Override
    public void processTransactionTerminated(SipProvider sipProvider, SessionControl sessionControl, TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("processTransactionTerminated handle not implemented");
    }

    @Override
    public void processDialogTerminated(SipProvider sipProvider, SessionControl sessionControl, DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("processDialogTerminated handle not implemented");
    }

    /*
     * Listener EventBus
     */
    @Override
    public void handle(EventBusReceiver receiver, JsonObject jsonObject) {
        SessionControlMessage message = new SessionControlMessgeImpl();
        message = message.parse(jsonObject);
        Session session = null;
        
        //XXX
        System.out.println("\r \n Received "+message.getMethod()+" \r \n");
        
        if (message.getMethod() != SessionControlMethodEnum.INIT) {
            session = getSession(null, message.getSessionID());
            if (session == null){
            	System.out.println("Could not find session with id "+message.getSessionID());
            	return;
            }
        }
        try {
            assert session != null;
            switch (message.getMethod()) {
                case INIT:
                	//create user
                    User from;
                    User to;
                    from = createUser(
                            getUserNameFromEB(((SessionControlCreateSession) message).getFrom()),
                            getDomainFromEB(((SessionControlCreateSession) message).getFrom()), null,
                            Integer.parseInt(properties.getProperty("org.openxsp.stack.PORT")));
                    to =  createUser(
                            getUserNameFromEB(((SessionControlCreateSession) message).getTo()),
                            getDomainFromEB(((SessionControlCreateSession) message).getFrom()), null,
                            Integer.parseInt(properties.getProperty("org.openxsp.stack.IMSICSCF")));
                    
                    //create a new Session and save it
                    session = this.createSession(from, to);
                    session.createCallId();
                    session.setEBSessionID(message.getSessionID());
                    mapping.addSession(session);
                    session.updateStateFromEB(SessionControlMethodEnum.INIT);
                    
                    String callback = message.getAddress();
                    if(callback==null){
                    	System.out.println("callback address of INIT request is null - stopping");
                    	return;
                    }
                    session.setCallBackAddress(callback);//Address of RTC Control
                    session.setLocalAddress(SessionControlEventAddress.SIP_ADDRESS.getEvent() + ":" + this.moduleID); //Address of SipStack
                    
                    session.createViaHeader(null, properties.getProperty("org.openxsp.stack.HOST_IP"), properties.getProperty("org.openxsp.stack.PORT"));
                    Request x = session.createRequest(Request.INVITE, ((SessionControlCreateSession) message).getContent(),
                            ((SessionControlCreateSession) message).getContentType(), to.getSipURIAddress().toString());

                    session.createRouteHeader(x, properties.getProperty("org.openxsp.stack.IMSIP"), properties.getProperty("org.openxsp.stack.IMSSCSCF"), null);
          
                    //move to session
                    Header accept = sipStack.getHeaderFactory().createHeader("Accept-Contact",
                            "<sip:" + from.getName() + "@" + properties.getProperty("org.openxsp.stack.HOST_IP")
                    + ":" + properties.getProperty("org.openxsp.stack.PORT") + ">;rtc.control");
                    x.addHeader(accept);            
                    //move to session
                    Header associated = sipStack.getHeaderFactory().createHeader("P-Associated-URI", "sip:bob@kamailio-ims.org");
                    x.addHeader(associated);
                    Header pai = sipStack.getHeaderFactory().createHeader("P-Asserted-Identity", "sip:bob@kamailio-ims.org");
                    x.addHeader(pai);
                    
                    session.createViaHeader(session.getCallID(), properties.getProperty("org.openxsp.stack.HOST_IP"), properties.getProperty("org.openxsp.stack.PORT"));
                    session.createContactHeader(x);
                    
                    sipProvider.sendRequest(x);
                    break;
                case ACCEPT:
                	session.updateStateFromEB(SessionControlMethodEnum.ACCEPT);
                	session.createViaHeader(null, properties.getProperty("org.openxsp.stack.HOST_IP"), properties.getProperty("org.openxsp.stack.PORT"));
                	                    
                    Response ok = sipStack.getMessageFactory().createResponse(200, session.getCurrentRequest());
                    
                    session.addRecordRouteHeaderFromRequest(ok);
                    //session.createRouteHeader(ok, properties.getProperty("org.openxsp.stack.IMSIP"), properties.getProperty("org.openxsp.stack.IMSSCSCF"));

                    if ( ((SessionControlAcceptSession) message).getContent() != null) {
                        ContentTypeHeader type;
                        String delims = "[/]+";
                        String[] typeParts = ((SessionControlAcceptSession) message).getContentType().split(delims);
                        if (typeParts.length > 1)
                            type = sipStack.getHeaderFactory().createContentTypeHeader(typeParts[0], typeParts[1]);
                        else
                            type = sipStack.getHeaderFactory().createContentTypeHeader(typeParts[0], null);

                        ok.setContent(((SessionControlAcceptSession) message).getContent().getBytes(), type);
                    }
                    
                    String cb = ((SessionControlAcceptSession) message).getCallBackAddress();
                    if(cb==null){
                    	System.out.println("Callback address not set - aborting");
                    	return;
                    }
                    
                    session.setCallBackAddress(cb);
                    
                    //XXX
                    Session session2 = getSession(null, message.getSessionID());
                    if(session2.getCallBackAddress()==null){
                    	System.out.println("Couldn't set callback address");
                    }
                    
                    session.createRouteHeader(ok, properties.getProperty("org.openxsp.stack.IMSIP"), properties.getProperty("org.openxsp.stack.IMSSCSCF"));
                    session.createContactHeader(ok);
                    sipProvider.sendResponse(ok);
                    break;
                case CANCEL:
                	session.updateStateFromEB(SessionControlMethodEnum.CANCEL);
                	session.createViaHeader(null, properties.getProperty("org.openxsp.stack.HOST_IP"), properties.getProperty("org.openxsp.stack.PORT"));
                 
                    Request cancel = session.createRequest(Request.CANCEL, null, null,
                            properties.getProperty("org.openxsp.stack.IMSDOMAIN") + ":" + properties.getProperty("org.openxsp.stack.IMSICSCF"));
                    session.addRecordRouteHeaderFromRequest(cancel);
                    
                    session.createRouteHeader(cancel, properties.getProperty("org.openxsp.stack.IMSIP"), properties.getProperty("org.openxsp.stack.IMSSCSCF"), null);
                    
                    session.createContactHeader(cancel);
                    sipProvider.sendRequest(cancel);
                    break;
                case CONFIRM:
                	session.updateStateFromEB(SessionControlMethodEnum.CONFIRM);
                
                	//session.createViaHeader(null, properties.getProperty("org.openxsp.stack.HOST_IP"), properties.getProperty("org.openxsp.stack.PORT"));
                    Request ack = session.createRequest(Request.ACK, null, null,session.getTo().getSipURIAddress().toString());
                    session.adddRouteHeaderFromResonse(ack);
                    
                    //XXX
                    session.createRouteHeader(ack, properties.getProperty("org.openxsp.stack.IMSIP"), properties.getProperty("org.openxsp.stack.IMSSCSCF"), null);
                                       
                    session.createContactHeader(ack);
                  //move to session
                    Header acceptACK = sipStack.getHeaderFactory().createHeader("Accept-Contact",
                            "<sip:" + session.getFrom().getName() + "@" + properties.getProperty("org.openxsp.stack.HOST_IP")
                    + ":" + properties.getProperty("org.openxsp.stack.PORT") + ">;rtc.control");
                    ack.addHeader(acceptACK);            
                    //move to session
                    Header associatedACK = sipStack.getHeaderFactory().createHeader("P-Associated-URI", "sip:bob@kamailio-ims.org");
                    ack.addHeader(associatedACK);
                    Header paiACK = sipStack.getHeaderFactory().createHeader("P-Asserted-Identity", "sip:bob@kamailio-ims.org");
                    ack.addHeader(paiACK);
                                       
                    sipProvider.sendRequest(ack);
                    break;
                case UPDATE:
                	session.updateStateFromEB(SessionControlMethodEnum.UPDATE);
                	session.createViaHeader(null, properties.getProperty("org.openxsp.stack.HOST_IP"), properties.getProperty("org.openxsp.stack.PORT"));
                    
                    Request update = session.createRequest(Request.UPDATE, null, null,
                            properties.getProperty("org.openxsp.stack.IMSDOMAIN") + ":" + properties.getProperty("org.openxsp.stack.IMSICSCFT"));
                    session.createRouteHeader(update, properties.getProperty("org.openxsp.stack.IMSIP"), properties.getProperty("org.openxsp.stack.IMSSCSCF"), null);
                    
                    session.createContactHeader(update);
                    sipProvider.sendRequest(update);
                    break;
                case END:
                	session.updateStateFromEB(SessionControlMethodEnum.END);
                	session.createViaHeader(null, properties.getProperty("org.openxsp.stack.HOST_IP"), properties.getProperty("org.openxsp.stack.PORT"));
                    
                    Request bye = session.createRequest(Request.BYE, null, null,
                            properties.getProperty("org.openxsp.stack.IMSDOMAIN") + ":" + properties.getProperty("org.openxsp.stack.IMSICSCF"));
                    session.createRouteHeader(bye, properties.getProperty("org.openxsp.stack.IMSIP"), properties.getProperty("org.openxsp.stack.IMSSCSCF"), null);
                    
                    session.createContactHeader(bye);
                    sipProvider.sendRequest(bye);
                    break;
                case INFO:
                	session.updateStateFromEB(SessionControlMethodEnum.INFO);
                	session.createViaHeader(null, properties.getProperty("org.openxsp.stack.HOST_IP"), properties.getProperty("org.openxsp.stack.PORT"));;
                   
                    Request info = session.createRequest(Request.INFO, null, null,
                            properties.getProperty("org.openxsp.stack.IMSDOMAIN") + ":" + properties.getProperty("org.openxsp.stack.IMSICSCF"));
                    session.createRouteHeader(info, properties.getProperty("org.openxsp.stack.IMSIP"), properties.getProperty("org.openxsp.stack.IMSSCSCF"), null);
                    
                    session.createContactHeader(info);
                    sipProvider.sendRequest(info);
                    break;
            }
        }  catch (InvalidArgumentException | ParseException | SipException e) {
            e.printStackTrace();
        }
    }

    /*
     * Result EventBus
     */
    @Override
    public void handle(EventBusResult result, RPCResult rpcResult) {
        if (result != null) {
            if (result.getResultId() != null) {
                Session session = mapping.getSessionByEBSessionId(result.getResultId());
                if (!rpcResult.succeeded())
                    session.setCurrentRequest(null);
            }
        }
    }
}
