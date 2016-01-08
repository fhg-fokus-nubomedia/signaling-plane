package de.fhg.fokus.ims.core;

/**
 * Constants to some predefined Feature Tags
 * 
 * @author Alice Motanga
 *
 *IP Voice Call +g.3gpp.icsi-ref="urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel"
IP Video Call +g.3gpp.icsi-ref="urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel";video

Note also that when a device supports both IP Voice Call and IP Video Call, the feature
tag +g.3gpp.icsi-ref="urn:urn-7:3gpp-service.ims.icsi.mmtel" is only included once in the
OPTIONS request/response.

Geolocation PUSH +g.3gpp.iari-ref="urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geopush"
Geolocation PULL +g.3gpp.iari-ref="urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geopull"
Social presence
information
+g.3gpp.iari-ref="urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.sp"

Video Share
outside of a voice
call
+g.3gpp.iari-ref= "urn:urn-7:3gpp-application.ims.iari.gsma-vs"

IP Based
Standalone
messaging
+g.3gpp.icsi-ref="urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.msg;
urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.largemsg"

Chat +g.3gpp.iari-ref="urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.im"
File Transfer +g.3gpp.iari-ref="urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.ft"


Image Share +g.3gpp.iari-ref="urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-is"
Video Share +g.3gpp.cs-voice

SIP MESSAGE +g.3gpp.icsi-ref="urn%3Aurn-7%3A3gppservice.ims.icsi.oma.cpm.msg"
 */

public class FeatureTagConstants 
{
	/**
	 * OMA IM feature tag
	 */
	public final static String FT_OMA_IM = "+g.oma.sip-im";
	
	/**
	 * OMA IM feature tag large message
	 */
	public final static String FT_OMA_IM_LM = "+g.oma.sip-im.large-message";

	/**
	 * 3GPP video share feature tag
	 */
	public final static String FT_3GPP_VIDEO_SHARE = "+g.3gpp.cs-voice";

//	/**
//     * 3GPP image share feature tag
//     */
//    public final static String FT_3GPP_IMAGE_SHARE = "+g.3gpp.app_ref=\"urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-is\"";	
//                                                                       "urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-is"
//    
    /**
   	 * 3GPP feature tag prefix
   	 */
   	public final static String FT_3GPP_PREFIX = "+g.3gpp.icsi-ref";
   	
   	
    /**
	 * 3GPP voice call
	 */
	public final static String FT_3GPP_VOICE_CALL = "urn%3Aurn-7%3A3gpp-service.ims.icsi.mmtel";
	
	/**
	 * 3GPP video call
	 */
	public final static String FT_3GPP_VIDEO_CALL = ";video";
	
	
    /**
	 * RCS-e feature tag prefix
	 */
	public final static String FT_RCSE = "+g.3gpp.iari-ref";
	
	/**
	 * RCS-e extension feature tag prefix
	 */
	public final static String FT_RCSE_EXTENSION = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcse";
	
	/**
	 * 3GPP video call
	 */
	public final static String FT_RCSE_MESSAGING_STANDALONE = "urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.msg;urn%3Aurn-7%3A3gpp-service.ims.icsi.oma.cpm.largemsg";
		
	/**
	 * RCS-e image share feature tag
	 */
	public final static String FT_RCSE_IMAGE_SHARE = "urn%3Aurn-7%3A3gpp-application.ims.iari.gsma-is";
	
	/**
	 * RCS-e image share feature tag
	 */
	public final static String FT_RCSE_VIDEO_SHARE = "urn:urn-7:3gpp-application.ims.iari.gsma-vs";

	/**
	 * RCS-e chat feature tag
	 */
	public final static String FT_RCSE_CHAT = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.im";

	/**
	 * RCS-e file transfer feature tag
	 */
	public final static String FT_RCSE_FT = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.ft";

	/**
	 * RCS-e presence discovery feature tag
	 */
	public final static String FT_RCSE_PRESENCE_DISCOVERY = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.dp";

	/**
	 * RCS-e social presence feature tag
	 */
	public final static String FT_RCSE_SOCIAL_PRESENCE = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcse.sp";		
	
	/**
	 * RCS-e geo-location pull
	 */
	public final static String FT_RCSE_GEOLOCATION_PULL = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geopull";

	/**
	 * RCS-e geo-location push
	 */
	public final static String FT_RCSE_GEOLOCATION_PUSH = "urn%3Aurn-7%3A3gpp-application.ims.iari.rcs.geopush";

}
