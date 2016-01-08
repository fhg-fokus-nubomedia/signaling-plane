package javax.ims.core;


/**
 * <p>The Session Description Protocol (SDP) provides a standard representation of media details, transport addresses and other session descriptions to the participants.
 * <p>When one endpoint would like to start a new session containing media, or extend an existing session with new media, it has to formulate a SDP offer. A SDP offer contains a range of available codecs that the client supports and at which port(s) the terminal has opened for media streams. The SDP offer is then sent to the remote endpoint and it will be negotiated until both endpoints agree on the session setup. In the response to the SDP offer the remote endpoint state which port(s) it has opened for media streams.
 * <p>A SDP description is denoted by the content MIME type "application/sdp" and is divided into three different parts: session description, time description and media description. This interface covers the session description, see MediaDescriptor for more details on the media descriptor. See [RFC4566] for more detailed information about the SDP. 
 * <p>A SDP offer for a session with one audio media and one video media could look like this:
 *  
 *  <blockquote>
 *  <b>
 *  v=0<br>
 *  o=alice 2890844526 2890844526 IN IP4 host.atlanta.example.com<br>
 *  s=<br>
 *  c=IN IP4 host.atlanta.example.com<br>
 *  t=0 0<br>
 *  </b>
 *  m=audio 49170 RTP/AVP 0 8 97<br>
 *  a=rtpmap:0 PCMU/8000<br>
 *  a=rtpmap:8 PCMA/8000<br>
 *  a=rtpmap:97 iLBC/8000<br>
 *  m=video 51372 RTP/AVP 31 32<br>
 *  a=rtpmap:31 H261/90000<br>
 *  a=rtpmap:32 MPV/90000<br>
 *  </blockquote> 
 *  NOTE: If the session description is changed so that the Session needs to be renegotiated, the application is responsible to call update on the Session.
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 * @version 1.0
 */
public interface SessionDescriptor
{
	/**
	 * Adds a new attribute to the session.
	 * 
	 * @param attribute - the attribute to add
	 * @throws IllegalArgumentException - if the attribute argument is null or if the syntax of the attribute argument is invalid 
	 * @throws IllegalArgumentException - if the attribute already exist in the Session 
	 * @throws IllegalArgumentException - if the attribute could not be added 
	 * @throws IllegalStateException - if the Session is not in STATE_INITIATED
	 */
	public void addAttribute(String attribute) throws IllegalStateException, IllegalArgumentException ;

	/**
	 * Returns all attributes for the session.
	 * 
	 * @return a string array containing the attributes
	 */
	public String[] getAttributes();	
	
	/**
	 * Returns the version of the Session Description Protocol.
	 * See [RFC4566], chapter 5.1. for more information. 
	 * 
	 * @return the protocol version or null if the protocol version could not be retrieved
	 */
	public String getProtocolVersion();

	
	/**
	 *   Returns a unique identifier for the session.
	 *   See [RFC4566], chapter 5.2. for more information.
	 * 
	 * @return the Session identifier or null if the session identifier could not be retrieved
	 */
	public String getSessionId();

	/**
	 * Returns textual information about the session. This method provides a human-readable description of the session.
	 * See [RFC4566], chapter 5.4. for more information. 
	 * 
	 * @return Session information or null if the session information is not set
	 * @throws IllegalStateException - if the Session is not in STATE_INITIATED 
     * @throws IllegalArgumentException - if the info argument is null
	 */
	String getSessionInfo() throws IllegalArgumentException, IllegalStateException;

	/**
	 * Returns the textual session name.
	 * See [RFC4566], chapter 5.3. for more information.
	 * 
	 * @return the Session name or null if the session name is not set
	 */
	String getSessionName();

	
	/**
	 * <p>Removes an attribute from the Session.
	 * <p>The example below removes the "synced" attribute:
	 * <code>SessionDescriptor desc = session.getSessionDescriptor();
	 * desc.removeAttribute("synced");</code>
	 * 
	 * @param attribute - the attribute to remove
	 * 
	 * @throws IllegalArgumentException - if the attribute argument is null 
	 * @throws IllegalArgumentException - if the attribute does not exist in the Session 
     * @throws IllegalArgumentException - if the attribute could not be removed 
     * @throws IllegalStateException - if the Session is not in STATE_INITIATED
	 */
	public void removeAttribute(String attribute) throws IllegalStateException, IllegalArgumentException;
	
	
	/**
	 * Sets the textual information about the session.
	 * 
	 * @param info - session information
	 * @throws IllegalStateException - if the Session is not in STATE_INITIATED 
	 * @throws IllegalArgumentException - if the info argument is null
	 */
	public void setSessionInfo(String info) throws IllegalStateException, IllegalArgumentException;

	/**
	 * Sets the name of the session.
	 * 
	 * @param name - the session name
	 * @throws IllegalStateException - if the Session is not in STATE_INITIATED 
	 * @throws IllegalArgumentException - if the name argument is null
	 */
	public void setSessionName(String name);
	
}
