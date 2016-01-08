package javax.ims.core.media;


/**
 * <p>The different medias in IMS are described by the Session Description Protocol (SDP). SDP provides information of the media such as codecs, bitrates and transport addresses.
 *
 * <p>The media decription, for each media that is offered, starts with an "m=" field followed by the actual media description. This field describes the media type, the transport ports, transport protocol and a media format description.
 *
 * <p>Each media descriptor can be followed by one or many attributes. An attribute starts with an "a=" field. Since MIDlet writers can add additional attributes as they are required, the number of available attributes will continously increase. This interface gives access to review and to add new attributes to the SDP. See [RFC4566] for more detailed information about the SDP.
 *
 * <p>A SDP offer for a session with one audio media and one video media could look like this (the example shows a SDP with two media descriptiors, one marked in bold text):
 * 
 *  <blockquote>
 *  v=0 <br>
 *   o=alice 2890844526 2890844526 IN IP4 host.atlanta.example.com
 *   s=
 *   c=IN IP4 host.atlanta.example.com
 *   t=0 0
 *   m=audio 49170 RTP/AVP 0 8 97
 *   a=rtpmap:0 PCMU/8000
 *   a=rtpmap:8 PCMA/8000
 *   a=rtpmap:97 iLBC/8000
 *   m=video 51372 RTP/AVP 31 32
 *   a=rtpmap:31 H261/90000
 *   a=rtpmap:32 MPV/90000
 *   </blockquote>
 *   
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface MediaDescriptor
{

    /**
     * <p>Adds an attribute to the Media. Adding attributes that the IMS engine can set, such as "sendonly", "recvonly", "sendrecv", will lead to an IllegalArgumentException.
     * <p>The example below adds two attributes for a FramedMedia.
     * <code>
     * MediaDescriptor[] desc = framedMedia.getMediaDescriptors();
     * desc[0].addAttribute("max-size:2048000");
     * desc[0].addAttribute("synced");
     * </code>
     * <p>The resulting attribute lines will be:
     *
     * <code>a=max-size:2048000<br>
             a=synced
     * </code>        
     * @param attribute - the attribute to add
     * @throws IllegalArgumentException - if the attribute argument is null or if the syntax of the attribute argument is invalid 
     * @throws IllegalArgumentException - if the attribute already exist in the Media 
     * @throws IllegalArgumentException - if the attribute could not be added 
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE or STATE_ACTIVE
     */
	public void addAttribute(String attribute)throws IllegalArgumentException;

    /**
     * <p>Removes an attribute from the Media.
     * <p>The example below removes the "max-size:2048000" attribute for a FramedMedia.
     * <code>
     * MediaDescriptor[] desc = framedMedia.getMediaDescriptors();
     * desc[0].removeAttribute("max-size:2048000");
     * </code>    
     *     
     * @param attribute - the attribute to remove
     * @throws IllegalArgumentException - if the attribute argument is null 
     * @throws IllegalArgumentException - if the attribute does not exist in the Media 
     * @throws IllegalArgumentException - if the attribute could not be removed 
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE or STATE_ACTIVE
     */
    public void removeAttribute(String attribute)
        throws IllegalArgumentException;

    /**
     * Returns all attributes for the Media. If there are no attributes, an empty string array will be returned.
     * @return a string array containing the attributes
     */
    public String[] getAttributes();

    /**
     * <p>Returns the contents of the media description field in SDP for this media. 
     * 
     * <p>Example of SDP mediadescriptor: audio 4000 RTP/AVP 97 101
     * 
     * @return the media description
     * @throws IllegalStateException - if the Media is in STATE_INACTIVE
     */
    public String getMediaDescription() throws IllegalStateException;

    /**
     * Returns the title of the Media.
     * See [RFC4566], chapter 5.4. for more information. 
     * 
     * @return the Media title or null if the title has not been set
     */
    public String getMediaTitle();

    /**
     * Returns the title of the Media.
     * See [RFC4566], chapter 5.4. for more information. 
     * 
     * @param title - the Media title to set
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE 
     * @throws IllegalArgumentException - if the title argument is null
     */
    public void setMediaTitle(String title)
        throws IllegalStateException, IllegalArgumentException;
    
    /**
     * <p>Returns the proposed bandwidth to be used by the media. An empty string array will be returned if the bandwidth information could not be retrieved.
     * <p>See [RFC4566], chapter 5.8. for more information. 
     * 
     * @return bandwidth information
     */
    public String[] getBandwidthInfo();
    
    /**
     * <p>Sets the proposed bandwidth to be used by the media. All previous bandwidth info will be removed from the media.
     * <p>See [RFC4566], chapter 5.8. for more information.
     * <p>Example:
     * <blockquote>MediaDescriptor desc;<br>
     * desc.setBandwidthInfo(new String[]{ "AS:128" });
 	 * </blockquote>
     * @param info - the bandwidth info to set
     * @throws IllegalStateException - if the Media is not in STATE_INACTIVE 
     * @throws IllegalArgumentException - if the info argument is null or if the syntax is invalid
     */
    public void setBandwidthInfo(String[] info) throws IllegalStateException, IllegalArgumentException;
}
