package de.fhg.fokus.ims.core.media;

/**
 * Media related functionalities
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public class MediaManager 
{
	
	public static final int audioCodecs[] = {0, 3, 8 }; //PCMU, GSM, PCMA
	public static final int videoCocecs[] = {34, 26 }; //H263, JPEG
	public static final String messageMimeTypes[] = { "*" };	
	
	public static final int audioPort = 22222;
	public static final int videoPort = 22224;
	
	public static String getAudioCodecs()
	{
		String codecs = "";
		for(int i=0; i<audioCodecs.length; i++)
			codecs = codecs+audioCodecs[i]+" ";
		
		return codecs.trim();
	}
	
	public static String getVideoCodecs()
	{
		String codecs = "";
		for(int i=0; i<MediaManager.videoCocecs.length; i++)
			codecs = codecs+MediaManager.videoCocecs[i]+" ";
		return codecs.trim();
	}

}
