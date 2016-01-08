package de.fhg.fokus.ims.core.sdp;


import java.util.ArrayList;

import de.fhg.fokus.ims.core.media.MediaDescriptorImpl;
import de.fhg.fokus.ims.core.utils.Parser;


/** 
 * Class SdpParser extends class Parser for parsing of SDP strings.
 */
public class SdpParser extends Parser
{  
	/** Creates a SdpParser based on String <i>s</i> */ 
	public SdpParser(String s)
	{  
		super(s);
	}

	/** Creates a SdpParser based on String <i>s</i> and starting from position <i>i</i> */ 
	public SdpParser(String s, int i)
	{  
		super(s,i);
	}   

	/** Returns the first SdpField.
	 *  The SDP value terminates with the end of the String or with the first CR or LF char.
	 *  @return the first SdpField, or null if no SdpField is recognized. */
	public SdpField parseSdpField()
	{  
		int begin=index;
		while (begin>=0 && begin<str.length()-1 && str.charAt(begin+1)!='=') begin=str.indexOf("\n",begin);  
		if (begin<0) return null;
		char type=str.charAt(begin);
		begin+=2;
		int end=str.length();
		int CR=str.indexOf('\r',begin);
		if (CR>0 && CR<end) end=CR;
		int LF=str.indexOf('\n',begin);
		if (LF>0 && LF<end) end=LF;
		String value=str.substring(begin,end).trim();
		if (value==null) return null;
		setPos(end);
		goToNextLine();

		return new SdpField(type,value);
	}


	/** Returns the first SdpField of type <i>type</i>.
	 *  The SDP value terminates with the end of the String or with the first CR or LF char.
	 *  @return the first SdpField, or null if no <i>type</i> SdpField is found. */
	public SdpField parseSdpField(char type)
	{  
		int begin=0;
		if (!str.startsWith(type+"=",index))
		{  begin=str.indexOf("\n"+type+"=",index);
		//if (begin<0) begin=str.indexOf("\r"+type+"=",index);
		if (begin<0)
		{  // return null if no type SdpField has been found
			return null;
		}
		index=begin+1;
		}
		return parseSdpField(); 
	}  

	/** Returns the first OriginField.
	 *  @return the first OriginField, or null if no OriginField is found. */
	public OriginField parseOriginField()
	{  
		SdpField sf=parseSdpField('o');
		if (sf!=null) return new OriginField(sf); else return null;
	}

	/** Returns the first MediaField.
	 *  @return the first MediaField, or null if no MediaField is found. */
	public MediaField parseMediaField()
	{  
		SdpField sf=parseSdpField('m');
		if (sf!=null) return new MediaField(sf); else return null;
	}

	/** Returns the first ConnectionField.
	 *  @return the first ConnectionField, or null if no ConnectionField is found. */
	public ConnectionField parseConnectionField()
	{  
		SdpField sf=parseSdpField('c');
		if (sf!=null) return new ConnectionField(sf); else return null;
	}

	/** Returns the first SessionNameField.
	 *  @return the first SessionNameField, or null if no SessionNameField is found. */
	public SessionNameField parseSessionNameField()
	{  SdpField sf=parseSdpField('s');
	if (sf!=null) return new SessionNameField(sf); else return null;
	}

	/** Returns the first TimeField.
	 *  @return the first TimeField, or null if no TimeField is found. */
	public TimeField parseTimeField()
	{  
		SdpField sf=parseSdpField('t');
		if (sf!=null) return new TimeField(sf); else return null;
	}

	/** Returns the first AttributeField.
	 *  @return the first AttributeField, or null if no AttributeField is found. */
	public AttributeField parseAttributeField()
	{  SdpField sf=parseSdpField('a');
	if (sf!=null) return new AttributeField(sf); else return null;
	}

	/** Returns the first MediaDescriptor.
	 *  @return the first MediaDescriptor, or null if no MediaDescriptor is found. */
	public MediaDescriptorImpl parseMediaDescriptor()
	{  
		MediaField m=parseMediaField();
		if (m==null) return null;
		int begin=index;
		int end=str.indexOf("\nm",begin);
		if (end<0) end=str.length(); else end++;
		index=end;
		SdpParser par=new SdpParser(str.substring(begin,end));
		ConnectionField c=par.parseConnectionField();
		ArrayList av=new ArrayList();
		AttributeField a=par.parseAttributeField();
		while (a!=null)
		{  av.add(a);
		a=par.parseAttributeField();
		}
		return new MediaDescriptorImpl(m,c,av);
	}

}
