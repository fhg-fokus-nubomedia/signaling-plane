package org.openxsp.stack;

import gov.nist.core.NameValueList;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.RecordRoute;
import gov.nist.javax.sip.header.Route;
import gov.nist.javax.sip.header.RouteList;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.stack.Transport;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.UUID;

import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.openxsp.java.session.SessionControlMethodEnum;

/**
 * Created by Frank Schulze on 11.04.14. frank.schulze at fokus.fraunhofer.de
 */

public class Session {

	// fix parameter
	private static final int MIN_PORT = 1;
	private static final int MAX_PORT = 65536;

	// User
	/*
	 * User from and User to are swappable, please use the public function
	 * setDirection(user, user)
	 */
	private User from;
	private User to;

	// Stack information
	private ListeningPoint listeningPoint;
	private SipProvider sipProvider;
	private SipStack sipStack;

	// parameter, ToDo(lwo): add this to the config file
	private int maxForwardCount = 10;
	private long sequenceNumber = 1L;

	// Helper List
	private HashMap<String, SipUri> routeHeaderCache = new HashMap<String, SipUri>();

	// Header Sip
	private URI toURI;
	private ArrayList viaHeaders = new ArrayList();
	private LinkedList<SIPHeader> responseRRs = new LinkedList<SIPHeader>();
	private ContentTypeHeader contentTypeHeader;
	private CallIdHeader callIdHeader;
	private CSeqHeader cSeqHeader;
	private MaxForwardsHeader maxForwards;
	private SipURI contactURL, contactURI;
	private Address contactAddress;
	private ContactHeader contactHeader;
	private String host;
	private Request currentRequest;

	// Header EB
	private String EBSessionID, callID, callBackAddress, localAddress;

	// Session
	private State state;

	public Session(ListeningPoint listeningPoint, User from, User to, SipProvider sipProvider) throws Exception {
		if (listeningPoint == null)
			throw new Exception("empty listeningPoint");
		else
			this.listeningPoint = listeningPoint;
		if (from == null)
			throw new Exception("empty from User");
		else {
			this.from = from;
			this.from.setHeaderTag(UUID.randomUUID().toString());
		}
		if (to == null)
			throw new Exception("empty to User");
		else
			this.to = to;
		if (sipProvider == null)
			throw new Exception("empty SipProvider");
		else {
			this.sipProvider = sipProvider;
			this.sipStack = sipProvider.getSipStack();
		}
		this.state = new State();
	}

	/*
	 * private header functions, make them maybe public/protected (lwo)
	 */

	private void createViaHeader(String branch) throws InvalidArgumentException, ParseException {
		/*
		 * String ims_ip = "127.0.0.1"; int ims_port = 5060; String
		 * ims_transport = "udp";
		 * 
		 * ViaHeader imsViaHeader = sipStack.getHeaderFactory().createViaHeader(
		 * ims_ip, ims_port, ims_transport, branch);
		 * viaHeaders.add(imsViaHeader);
		 */
		String ip = from.getPeerHost();
		int port = new Integer(from.getPeerPort());
		String transport = listeningPoint.getTransport();

		if (port < MIN_PORT || port > MAX_PORT)
			throw new InvalidArgumentException("The port is invalid : " + port);
		if ((transport.compareToIgnoreCase(Transport.UDP.toString()) != 0 && transport
				.compareToIgnoreCase(Transport.TCP.toString()) != 0)) {
			throw new InvalidArgumentException("The transport parameter is invalid" + transport);
		}
		if (ip == null) {
			if (host == null) {
				throw new InvalidArgumentException("The host parameter is invalid" + host);
			} else {
				ip = host;
			}
		}
		ViaHeader viaHeader = sipStack.getHeaderFactory().createViaHeader(ip, port, transport, branch);
		// Add the new viaHeader to the viaHeaderArray
		viaHeaders.add(viaHeader);

	}

	private void createContentTypeHeader(String contectType, String contentSubType) throws ParseException {
		if (contentTypeHeader == null)
			contentTypeHeader = sipStack.getHeaderFactory().createContentTypeHeader(contectType, contentSubType);

	}

	private void createCallIdHeader() throws Exception {
		callIdHeader = sipProvider.getNewCallId();
	}

	private void createCseq(String method) throws ParseException, InvalidArgumentException {
		cSeqHeader = sipStack.getHeaderFactory().createCSeqHeader(sequenceNumber, method);
	}

	private void createMaxForward() throws InvalidArgumentException {
		maxForwards = sipStack.getHeaderFactory().createMaxForwardsHeader(maxForwardCount);
	}

	private void createRequestURI() throws ParseException {
		toURI = (SipUri) sipStack.getAddressFactory().createSipURI(to.getName(), to.getPeerHostPort());
	}

	private void createContacURI() throws ParseException {
		if (contactURI == null) {
			contactURI = sipStack.getAddressFactory().createSipURI(from.getName(), listeningPoint.getIPAddress());
			contactURI.setPort(listeningPoint.getPort());
			contactAddress = null;
		}
		if (contactAddress == null) {
			contactAddress = sipStack.getAddressFactory().createAddress(contactURI);
			contactAddress.setDisplayName(from.getDisplayName());
		}
	}

	/*
	 * Public functions
	 */
	public Request createRequest(String method, String sdp, String sdpTyp, String requestURI) throws ParseException,
			InvalidArgumentException {
		Request x = null;
		URI tmp = null;

		this.createRequestURI();
		if (callIdHeader == null) {
			try { // change of Exception, its not nice but it is okay for the
					// moment
					// ToDo(lwo): change to a popper handing, when necessary
				this.createCallIdHeader();
			} catch (Exception ex) {
				throw new InvalidArgumentException(ex.toString());
			}
		}
		if (viaHeaders == null) {
			this.createViaHeader(this.callID);
		}
		// this.createViaHeader(this.getEBSessionID());
		// this.createViaHeader(null);
		this.createMaxForward();
		this.callIdHeader.setCallId(this.callID);

		if (requestURI != null) {
			tmp = toURI;
			toURI = sipStack.getAddressFactory().createURI(requestURI);
		}

		if (method.equals(Request.INVITE)) {
			this.createCseq(Request.INVITE);
			x = sipStack.getMessageFactory().createRequest(toURI, Request.INVITE, callIdHeader, cSeqHeader,
					from.createFromHeader(), to.createToHeader(), viaHeaders, maxForwards);
			if (sdp != null) {

				String delims = "[/]+";
				String[] typeParts = sdpTyp.split(delims);
				if (typeParts.length > 1)
					createContentTypeHeader(typeParts[0], typeParts[1]);
				else
					createContentTypeHeader(typeParts[0], null);

				x.setContent(sdp.getBytes(), this.contentTypeHeader);
			}
		} else if (method.equals(Request.ACK)) {
			this.createCseq(Request.ACK);
			x = sipStack.getMessageFactory().createRequest(toURI, Request.ACK, callIdHeader, cSeqHeader,
					from.createFromHeader(), to.createToHeader(), viaHeaders, maxForwards);
			if (sdp != null) {
				String delims = "[/]+";
				String[] typeParts = sdpTyp.split(delims);
				if (typeParts.length > 1)
					createContentTypeHeader(typeParts[0], typeParts[1]);
				else
					createContentTypeHeader(typeParts[0], null);
			}
		} else if (method.equals(Request.CANCEL)) {
			this.createCseq(Request.CANCEL);
			x = sipStack.getMessageFactory().createRequest(toURI, Request.CANCEL, callIdHeader, cSeqHeader,
					from.createFromHeader(), to.createToHeader(), viaHeaders, maxForwards);
		} else if (method.equals(Request.UPDATE)) {
			this.createCseq(Request.UPDATE);
			x = sipStack.getMessageFactory().createRequest(toURI, Request.CANCEL, callIdHeader, cSeqHeader,
					from.createFromHeader(), to.createToHeader(), viaHeaders, maxForwards);
			if (sdp != null) {
				createContentTypeHeader("application", "sdp");
				x.setContent(sdp.getBytes(), this.contentTypeHeader);
			}
		} else if (method.equals(Request.BYE)) {
			this.createCseq(Request.BYE);
			x = sipStack.getMessageFactory().createRequest(toURI, Request.CANCEL, callIdHeader, cSeqHeader,
					from.createFromHeader(), to.createToHeader(), viaHeaders, maxForwards);
		} else if (method.equals(Request.INFO)) {
			this.createCseq(Request.INFO);
			x = sipStack.getMessageFactory().createRequest(toURI, Request.INFO, callIdHeader, cSeqHeader,
					from.createFromHeader(), to.createToHeader(), viaHeaders, maxForwards);
			if (sdp != null) {

				String delims = "[/]+";
				String[] typeParts = sdpTyp.split(delims);
				if (typeParts.length > 1)
					createContentTypeHeader(typeParts[0], typeParts[1]);
				else
					createContentTypeHeader(typeParts[0], null);

				x.setContent(sdp.getBytes(), this.contentTypeHeader);
			}
		}

		if (requestURI != null) {
			toURI = tmp;
		}

		return x;
	}

	public Response createResponse(int statusCode, String branch, long cSeq, ListIterator vias, ContactHeader con)
			throws ParseException, InvalidArgumentException {
		Response x;
		this.createRequestURI();
		// this.createViaHeader(branch);

		this.createMaxForward();
		if (callIdHeader == null) {
			try { // change of Exception, its not nice but it is okay for the
					// moment
					// ToDo(lwo): change to a popper handing, when necessary
				this.createCallIdHeader();
			} catch (Exception ex) {
				throw new InvalidArgumentException(ex.toString());
			}
		}
		cSeqHeader = sipStack.getHeaderFactory().createCSeqHeader(cSeq, Request.REGISTER);
		ViaHeader via;
		while (vias.hasNext()) {
			via = (ViaHeader) vias.next();
			this.viaHeaders.add(via);
		}
		this.callIdHeader.setCallId(this.callID);
		x = sipStack.getMessageFactory().createResponse(statusCode, this.callIdHeader, cSeqHeader,
				from.createFromHeader(), to.createToHeader(), viaHeaders, maxForwards);
		x.setReasonPhrase("Ok");
		x.addHeader(con);

		// x = sipStack.getMessageFactory().createResponse(200, request);
		return x;

	}

	public void createContactHeader(Request request) throws ParseException {
		// contactURL =
		// sipStack.getAddressFactory().createSipURI(from.getName(),
		// listeningPoint.getIPAddress());
		contactURL = sipStack.getAddressFactory().createSipURI(this.from.getName(), listeningPoint.getIPAddress());
		contactURL.setPort(listeningPoint.getPort());
		contactURL.setLrParam();// RFC 3261 parameter ToDo(lwo): check if it is
								// necessary

		this.createContacURI();// It creates the contactAddress

		contactHeader = sipStack.getHeaderFactory().createContactHeader(this.contactAddress);
		request.addHeader(contactHeader);
	}

	public void createContactHeader(Response response) throws ParseException {
		contactURL = sipStack.getAddressFactory().createSipURI(from.getName(), listeningPoint.getIPAddress());
		contactURL.setPort(listeningPoint.getPort());
		contactURL.setLrParam();// RFC 3261 parameter ToDo(lwo): check if it is
								// necessary

		this.createContacURI();// It creates the contactAddress

		contactHeader = sipStack.getHeaderFactory().createContactHeader(this.contactAddress);
		response.addHeader(contactHeader);
	}

	public void createRecordRouteList(Request request) {

		if (request == null)
			return;
		@SuppressWarnings("unchecked")
		ListIterator<SIPHeader> li = request.getHeaders("Record-Route");
		while (li.hasNext()) {
			responseRRs.add(li.next());
		}
	}

	public void createRecordRouteList(Response response) {

		if (response == null)
			return;
		@SuppressWarnings("unchecked")
		ListIterator<SIPHeader> li = response.getHeaders("Record-Route");
		while (li.hasNext()) {
			responseRRs.add(li.next());
		}
	}

	public void clearRouteHeader() {
		responseRRs.clear();
	}

	public void addRecordRouteHeaderFromRequest(Request request) throws ParseException, NullPointerException,
			SipException {
		ListIterator<SIPHeader> li = this.responseRRs.listIterator();
		while (li.hasNext()) {
			request.addFirst(li.next());
		}
	}

	public void addRecordRouteHeaderFromRequest(Response response) throws ParseException, NullPointerException,
			SipException {
		ListIterator<SIPHeader> li = this.responseRRs.listIterator();
		// its dirty but it works, so what
		while (li.hasNext()) {
			response.addHeader(li.next());

		}
	}

	/*
	 * Build a WS RoutHeader form WS RecordRoute header
	 */
	private RouteHeader wsdetect(RecordRoute ri) {
		RouteHeader x = null;
		Address addr = null;

		if (ri.toString().toString().contains("transport")) {
			ContactHeader ch = to.getContactHeader();
			String chT = ch.getAddress().toString();
			String delims = "[@]+";
			String[] typeParts = chT.split(delims);

			String tmp = "\"sip:" + typeParts[1] + ";" + "transport=ws" + "\";nat=yes";
			try {

				addr = ri.getAddress();
				SipUri uri = (SipUri) addr.getURI();
				NameValueList parameters = uri.getParameters();
				parameters.remove("transport");
				uri.removeParameters();

				uri.setParameter("received", tmp);

				Iterator<String> li = (Iterator<String>) parameters.getNames();

				while (li.hasNext()) {
					String paraName = li.next();
					uri.setParameter(paraName, parameters.getParameter(paraName));
				}

				Address addr2 = sipStack.getAddressFactory().createAddress(uri);
				x = sipStack.getHeaderFactory().createRouteHeader(addr2);

			} catch (ParseException e) {
				e.printStackTrace();
			}

		}
		return x;
	}

	/*
	 * This function checks if a route uri address is already in
	 * 
	 * @ret true if a URI is in, false if URI is not in
	 */
	private boolean AddressDuplicationDetect(RouteHeader route) {
		SipUri uri = (SipUri) route.getAddress().getURI();
		uri.removeParameters();
		if (routeHeaderCache.containsValue(uri))
			return true;
		else {
			routeHeaderCache.put(uri.toString(), uri);
			return false;
		}
	}

	public void adddRouteHeaderFromResonse(Request request) throws ParseException, NullPointerException, SipException {
		RecordRoute ri = null;
		RouteHeader x = null;
		boolean firstHop = true;
		RouteList defRoute = new RouteList();
		boolean isOverWrith = false;

		// XXX
		if (responseRRs.size() == 0) {
			System.out.println("No Record Route header from response stored");

		}

		for (int i = this.responseRRs.size() - 1; i >= 0; i--) {
			isOverWrith = false;
			ri = (RecordRoute) this.responseRRs.get(i);

			if ((x = wsdetect(ri)) == null)
				x = sipStack.getHeaderFactory().createRouteHeader(ri.getAddress());
			else
				isOverWrith = true;

			if (firstHop) {
				// XXX
				System.out.println("Adding route " + x.getName() + " as first hop");
				defRoute.add((Route) x);
				// get the default Route
				firstHop = false;
			}
			if (!AddressDuplicationDetect(x)) {
				// XXX
				System.out.println("Adding route " + x.getName());
				request.addHeader(x);
			}

			else if (isOverWrith) {
				System.out.println("Removing route " + x.getAddress().getURI());
				request.removeRouteHeader((SipUri) x.getAddress().getURI());
				request.addHeader(x);
			}

		}

		request.attachDefaultRoute(defRoute);
	}

	public void createRouteHeader(Request request, String domain, String port, ImsRouting routing)
			throws InvalidArgumentException, ParseException, NullPointerException, SipException {
		if (request == null || domain == null || port == null) {
			throw new InvalidArgumentException("Please check the parameter request, domain and port");
		}

		Address addr;
		if (routing == null) {
			addr = sipStack.getAddressFactory().createAddress("sip:orig@" + domain + ":" + port + ";lr=on");
		} else {
			addr = sipStack.getAddressFactory().createAddress("sip:" + routing.getValue() + "@" + domain + ":" + port);
		}

		RouteHeader route = sipStack.getHeaderFactory().createRouteHeader(addr);
		request.addHeader(route);
	}

	public void createRouteHeader(Response response, String domain, String port) throws InvalidArgumentException,
			ParseException {
		if (response == null || domain == null || port == null) {
			throw new InvalidArgumentException("Please check the parameter request, domain and port");
		}
		Address addr = sipStack.getAddressFactory().createAddress("sip:" + domain + ":" + port);
		RouteHeader route = sipStack.getHeaderFactory().createRouteHeader(addr);
		route.setParameter("lr", null);
		response.addHeader(route);
	}

	public void addSDP(Request request, byte[] sdp) throws ParseException {
		createContentTypeHeader("application", "sdp");
	}

	public ClientTransaction createClientTransaction(Request request) throws TransactionUnavailableException,
			InvalidArgumentException {
		ClientTransaction clientTid = null;

		if (request == null)
			throw new InvalidArgumentException("empty requst");

		clientTid = sipProvider.getNewClientTransaction(request);
		return clientTid;
	}

	public void createViaHeader(String branch, String ip, String port) throws InvalidArgumentException, ParseException {
		int iPort = new Integer(port);
		String transport = listeningPoint.getTransport();

		if (iPort < MIN_PORT || iPort > MAX_PORT)
			throw new InvalidArgumentException("The port is invalid : " + iPort);
		if ((transport.compareToIgnoreCase(Transport.UDP.toString()) != 0 && transport
				.compareToIgnoreCase(Transport.TCP.toString()) != 0)) {
			throw new InvalidArgumentException("The transport parameter is invalid" + transport);
		}
		if (ip == null) {
			if (host == null) {
				throw new InvalidArgumentException("The host parameter is invalid" + host);
			} else {
				ip = host;
			}
		}
		ViaHeader viaHeader = sipStack.getHeaderFactory().createViaHeader(ip, iPort, transport, branch);

		// Add the new viaHeader to the viaHeaderArray
		viaHeaders.add(viaHeader);

	}

	public void setDirection(User from, User to) throws InvalidArgumentException {
		if (from == null)
			throw new InvalidArgumentException("from user is null");
		if (to == null)
			throw new InvalidArgumentException("to user is null");
		if (from != this.from && from != this.to)
			throw new InvalidArgumentException("user from is unknown in this context,"
					+ "please create new message Object for this user");
		if (to != this.from && to != this.to)
			throw new InvalidArgumentException("user to is unknown in this context,"
					+ "please create new message Object for this user");

		this.from = from;
		this.to = to;
		// clear variables
		this.toURI = null;
		this.contentTypeHeader = null;
		this.cSeqHeader = null;
		this.contactURL = null;
		this.contactURI = null;
		this.contactAddress = null;
		this.contactHeader = null;
	}

	public String createCallId() {
		this.callID = UUID.randomUUID().toString();
		return this.callID;
	}

	public String createEBSessionId() {
		this.EBSessionID = UUID.randomUUID().toString();
		return this.EBSessionID;
	}

	public void destroy() {
		try {
			finalize();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	public boolean updateStateFromSipRequest(String method) {
		if (method == null)
			return false;
		if (method.compareTo(Request.INVITE) == 0) {
			if (this.state.setNewState(StateEnum.INVITE) == null)
				return false;
			else
				return true;
		} else if (method.compareTo(Request.BYE) == 0) {
			if (this.state.setNewState(StateEnum.BYE) == null)
				return false;
			else
				return true;
		} else if (method.compareTo(Request.CANCEL) == 0) {
			if (this.state.setNewState(StateEnum.CANCEL) == null)
				return false;
			else
				return true;
		} else if (method.compareTo(Request.UPDATE) == 0) {
			if (this.state.setNewState(StateEnum.UPDATE) == null)
				return false;
			else
				return true;
		} else if (method.compareTo(Request.ACK) == 0) {
			if (this.state.setNewState(StateEnum.ACK) == null)
				return false;
			else
				return true;
		}
		return false;
	}

	public boolean updateStateFromSipResponse(Response method) {
		if (method == null)
			return false;
		if (method.getStatusCode() == Response.OK) { // 200
			if (this.state.setNewState(StateEnum.OK) == null) {
				System.out.println("updateStateFromSipResponse Faile Current State : [" + this.state.getCurrentState()
						+ "] and New State [" + method.getStatusCode() + "] are not matching.");
				return false;
			} else
				return true;
		}
		System.out.println("Status code not supported: [" + method.getStatusCode() + "]");
		return false;
	}

	public boolean updateStateFromEB(SessionControlMethodEnum method) {
		if (method == null)
			return false;
		switch (method) {
		case INIT:
			if (this.state.setNewState(StateEnum.INVITE) == null)
				return false;
			else
				return true;
		case ACCEPT:
			if (this.state.setNewState(StateEnum.OK) == null)
				return false;
			else
				return true;
		case CANCEL:
			if (this.state.setNewState(StateEnum.CANCEL) == null)
				return false;
			else
				return true;
		case CONFIRM:
			if (this.state.setNewState(StateEnum.ACK) == null)
				return false;
			else
				return true;
		case UPDATE:
			if (this.state.setNewState(StateEnum.UPDATE) == null)
				return false;
			else
				return true;
		case END:
			if (this.state.setNewState(StateEnum.BYE) == null)
				return false;
			else
				return true;
		}
		return false;
	}

	/*
	 * Getter and Setter
	 */
	public CallIdHeader getCallIdHeader() {
		return callIdHeader;
	}

	public void setCallIdHeader(CallIdHeader callIdHeader) {
		this.callIdHeader = callIdHeader;
	}

	public void reSetCallIdHeader() {
		this.callIdHeader = null;
	}

	public ContactHeader getContactHeader() {
		return contactHeader;
	}

	public User getFrom() {
		return from;
	}

	public User getTo() {
		return to;
	}

	public ListeningPoint getListeningPoint() {
		return listeningPoint;
	}

	public SipProvider getSipProvider() {
		return sipProvider;
	}

	public SipStack getSipStack() {
		return sipStack;
	}

	public int getMaxForwardCount() {
		return maxForwardCount;
	}

	public void setMaxForwardCount(int maxForwardCount) {
		this.maxForwardCount = maxForwardCount;
	}

	public long getSequenceNumber() {
		return sequenceNumber;
	}

	public URI getToURI() {
		return toURI;
	}

	public ArrayList getViaHeaders() {
		return viaHeaders;
	}

	public void setViaHeaders(ArrayList viaHeaders) {
		this.viaHeaders = viaHeaders;
	}

	public ContentTypeHeader getContentTypeHeader() {
		return contentTypeHeader;
	}

	public CSeqHeader getcSeqHeader() {
		return cSeqHeader;
	}

	public MaxForwardsHeader getMaxForwards() {
		return maxForwards;
	}

	public SipURI getContactURL() {
		return contactURL;
	}

	public SipURI getContactURI() {
		return contactURI;
	}

	public Address getContactAddress() {
		return contactAddress;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getEBSessionID() {
		if (EBSessionID == null) {
			this.EBSessionID = this.createEBSessionId();
			return EBSessionID;
		}
		return EBSessionID;
	}

	public Request getCurrentRequest() {
		return currentRequest;
	}

	public void setCurrentRequest(Request currentRequest) {
		this.currentRequest = currentRequest;
	}

	public void setEBSessionID(String EBSessionID) {
		this.EBSessionID = EBSessionID;
	}

	public String getCallID() {
		return this.callID;
	}

	public void setCallID(String callID) {
		this.callID = callID;
	}

	public String getCallBackAddress() {
		return callBackAddress;
	}

	public void setCallBackAddress(String callBackAddress) {
		this.callBackAddress = callBackAddress;
	}

	public String getLocalAddress() {
		return localAddress;
	}

	public void setLocalAddress(String localAddress) {
		this.localAddress = localAddress;
	}

}
