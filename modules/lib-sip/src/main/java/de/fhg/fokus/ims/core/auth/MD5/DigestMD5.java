/*
 * Copyright (C) 2007 FhG FOKUS, Institute for Open Communication Systems
 *
 * This file is part of OpenIC - an IMS user endpoint implementation
 * 
 * OpenIC is proprietary software that is licensed 
 * under the FhG FOKUS "SOURCE CODE LICENSE for FOKUS IMS COMPONENTS".
 * You should have received a copy of the license along with this 
 * program; if not, write to Fraunhofer Institute FOKUS, Kaiserin-
 * Augusta Allee 31, 10589 Berlin, GERMANY 
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * It has to be noted that this software is not intended to become 
 * or act as a product in a commercial context! It is a PROTOTYPE
 * IMPLEMENTATION for IMS technology testing and IMS application 
 * development for research purposes, typically performed in IMS 
 * test-beds. See the attached license for more details. 
 *
 * For a license to use this software under conditions
 * other than those described here, please contact Fraunhofer FOKUS 
 * via e-mail at the following address:
 * 
 *    info@open-ims.org
 *
 */
package de.fhg.fokus.ims.core.auth.MD5;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



/**
 * 
 * 
 * 
 * @author FhG FOKUS <a href="mailto:openic@open-ims.org">openic@open-ims.org</a>
 *
 */
public class DigestMD5
{
   
    public static final String MD5="MD5";
    private static final String AUTH="auth";
    private static final String AUTH_INT="auth-int";
    
   
        
    public static byte[] hexa2bin(String hexa) {
    	byte[] h = hexa.getBytes();
    	byte[] x = new byte[h.length/2];
    	int i,j,hi,low;
    	for(i=0,j=0;i<h.length&&j<x.length;i+=2,j++) {
    		hi=0;
    		low=0;
    		if (h[i]>='0'&&h[i]<='9') hi = h[i]-'0';
    		else
    		if (h[i]>='a'&&h[i]<='f') hi = h[i]-'a'+10;
    		else
    		if (h[i]>='A'&&h[i]<='F') hi = h[i]-'A'+10;

    		if (h[i+1]>='0'&&h[i+1]<='9') low = h[i+1]-'0';
    		else
    		if (h[i+1]>='a'&&h[i+1]<='f') low = h[i+1]-'a'+10;
    		else
    		if (h[i+1]>='A'&&h[i+1]<='F') low = h[i+1]-'A'+10;    		
    		x[j]= (byte)(hi*16 + low);
    	}
    	return x;
    }
    
    private static byte[] hexa= {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    protected static String bin2hexa(byte[] bin) {
    	byte[] h = new byte[bin.length*2];
    	int i,j;
    	for(i=0,j=0;i<h.length&&j<bin.length;i+=2,j++) {
    		h[i]= hexa[(bin[j]&0xF0)>>4];
    		h[i+1]= hexa[bin[j]&0x0F];
    	}
    	return new String(h);
    }  

    private static byte[] concat(byte[] a, byte b[])
    {
    	byte[] c = new byte[a.length+b.length];
		System.arraycopy(a,0,c,0,a.length);
		System.arraycopy(b,0,c,a.length,b.length);		
		return c;
    }
    
    private static String intToHex8(int x)
    {
    	StringBuffer stb = new StringBuffer();
    	while(x>0){
    		stb.insert(0,(char)hexa[x%16]);
    		x = x/16;
    	}
    	while (stb.length()<8) stb.insert(0, '0');
    	return stb.toString();
    }
    
    public static DigestMD5Response getResponse(
    		String algorithm,
    		String nonce,
			String username,
			String realm,							
			byte[] password,
			int nonceCount,
			String cNonce,
			String method,
			String uri,
			String contentBody,
			String qop) 
    {
    	byte[] A1;
    	String qopOut=null;
    	
//    	if (algorithm==null ||algorithm.length()==0 ||algorithm.equalsIgnoreCase(MD5)) {
    		A1 = concat((username+":"+realm+":").getBytes(),password);
//    	}else 
//    		A1 = new String(H(username + ":" + realm + ":" + password))
//             	+ ":" + nonce + ":" + cnonce;    	
//    	System.out.println("---");
//    	System.out.println("A1:"+new String(A1));
        byte[] A2 =null;
        if (qop == null || qop.length() == 0){
        	A2 = (method + ":" + uri).getBytes();
       		qopOut = null;
        }
        else if (qop.equalsIgnoreCase(AUTH)) {
        	A2 = (method + ":" + uri).getBytes();
    		qopOut = AUTH;        	 
        }
        else if(qop.equalsIgnoreCase(AUTH_INT)) {
        	A2 = concat((method + ":" + uri + ":").getBytes(),bin2hexa(H(contentBody.getBytes())).getBytes());
        	qopOut = AUTH_INT;
        }
//    	System.out.println("A2:"+new String(A2));

        String response = null;
        String data;
        if(cNonce != null && qop != null 
           && (qop.equals(AUTH) || (qop.equals(AUTH_INT))))
        	data = bin2hexa(H(A1))+":"+nonce+":"+intToHex8(nonceCount)+":"+cNonce+":"+qop+":"+bin2hexa(H(A2));
        else
        	data = bin2hexa(H(A1))+":"+nonce + ":"+bin2hexa(H(A2));

//    	System.out.println("H(A1):"+bin2hexa(H(A1)));
//    	System.out.println("H(A2):"+bin2hexa(H(A2)));
//    	System.out.println("DATA:"+data);

    	byte[] res  = H(data.getBytes());
    	response = bin2hexa(res);
//    	System.out.println("MD5:"+response);
//    	System.out.println("---");

    	return new DigestMD5Response(response,res,qopOut,cNonce,nonceCount);
    }

    private static byte[] H(byte[] data)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance(MD5);
            return digest.digest(data);
        }
        catch (NoSuchAlgorithmException ex)
        {
        	System.out.println("MD5 algorithm is unknown...");
            return null;
        }
    }    
//    private static byte[] KD(byte[] secret, byte[] data)
//    {
//    	byte[] b = new byte[secret.length+1+data.length];
//    	System.arraycopy(secret,0, b, 0,secret.length);
//    	b[secret.length]=':';
//    	System.arraycopy(data,0, b, secret.length+1,data.length);
//        return H(b);
//    }
    
//    Authorization: Digest username="061965242884",uri="sip:arcor.de",realm="arcor.de",nonce=""",response="71579f87
 //   9685cf6f5392b6cb8c318fa4",algoritm=MD5
    public static void main(String[] args)
    {
    	

    	System.out.println(getResponse("MD5", "49a3f8248b11ad57cb45eb26feabc34688c3e919", "061965242884", "arcor.de", "95529012008".getBytes(), 0, null, "REGISTER", "sip:arcor.de", null, null));
    }
}
