package de.fhg.fokus.ims.core.sdp;


/** 
 *  session-name-field = "s=" text CRLF
  */
public class SessionNameField extends SdpField
{  
   
   public SessionNameField(String session_name)
   {  
	   super('s',session_name);
   }

   public SessionNameField()
   {  
	   super('s'," ");
   }

   public SessionNameField(SdpField sf)
   {  
	   super(sf);
   }
      
   
   public String getSessionName()
   {  
	   return value;
   }

   public Object clone()
   {
	   return new SessionNameField(this);
   }
}
