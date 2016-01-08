package de.fhg.fokus.ims.core.sdp;


/** 
 * SDP attribute field.
 * <p>attribute-fields = "a=" (att-field ":" att-value) | att-field CRLF
  * 
  */
public class AttributeField extends SdpField
{  
   
   public AttributeField(String attribute)
   {  super('a',attribute);
   }

   
   public AttributeField(String attribute, String a_value)
   {  super('a',attribute+":"+a_value);
   }

 
   public AttributeField(SdpField sf)
   {  super(sf);
   }
      
   
   public String getAttributeName()
   {  int i=value.indexOf(":");
      if (i<0) return value; else return value.substring(0,i);
   }

  
   public String getAttributeValue()
   {  int i=value.indexOf(":");
      if (i<0) return null; else return value.substring(i+1);
   }

   public Object clone()
   {
	   return new AttributeField(this);
   }
   
   public static void main(String[] args)
   {
	   AttributeField af = new AttributeField("orient:landscape");
	   System.out.println("--> name: "+af.getAttributeName());
	   System.out.println("--> value: "+af.getAttributeValue());
	   System.out.println("--> value2: "+af.getValue());
   }

}
