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
package de.fhg.fokus.ims.core.auth.AKA;

import java.security.InvalidKeyException;
import java.util.Arrays;

import de.fhg.fokus.ims.core.auth.MD5.DigestMD5;
import de.fhg.fokus.ims.core.auth.MD5.DigestMD5Response;

/**
 * 
 * @author FhG FOKUS <a href="mailto:openic@open-ims.org">openic@open-ims.org</a>
 *
 */
public class DigestAKA
{
   
	public static final String AKA_V1 = "AKAv1-MD5";
	public static final String AKA_V2 = "AKAv2-MD5";
    
    private static Rijndael32Bit kernel = new Rijndael32Bit();;
        
    
    private static int base64Val(byte x)
    {
    	switch(x){
    		case '=': return -1;
    		case 'A': return 0;
    		case 'B': return 1;
    		case 'C': return 2;
    		case 'D': return 3;
    		case 'E': return 4;
    		case 'F': return 5;
    		case 'G': return 6;
    		case 'H': return 7;
    		case 'I': return 8;
    		case 'J': return 9;
    		case 'K': return 10;
    		case 'L': return 11;
    		case 'M': return 12;
    		case 'N': return 13;
    		case 'O': return 14;
    		case 'P': return 15;
    		case 'Q': return 16;
    		case 'R': return 17;
    		case 'S': return 18;
    		case 'T': return 19;
    		case 'U': return 20;
    		case 'V': return 21;
    		case 'W': return 22;
    		case 'X': return 23;
    		case 'Y': return 24;
    		case 'Z': return 25;
    		case 'a': return 26;
    		case 'b': return 27;
    		case 'c': return 28;
    		case 'd': return 29;
    		case 'e': return 30;
    		case 'f': return 31;
    		case 'g': return 32;
    		case 'h': return 33;
    		case 'i': return 34;
    		case 'j': return 35;
    		case 'k': return 36;
    		case 'l': return 37;
    		case 'm': return 38;
    		case 'n': return 39;
    		case 'o': return 40;
    		case 'p': return 41;
    		case 'q': return 42;
    		case 'r': return 43;
    		case 's': return 44;
    		case 't': return 45;
    		case 'u': return 46;
    		case 'v': return 47;
    		case 'w': return 48;
    		case 'x': return 49;
    		case 'y': return 50;
    		case 'z': return 51;
    		case '0': return 52;
    		case '1': return 53;
    		case '2': return 54;
    		case '3': return 55;
    		case '4': return 56;
    		case '5': return 57;
    		case '6': return 58;
    		case '7': return 59;
    		case '8': return 60;
    		case '9': return 61;
    		case '+': return 62;
    		case '/': return 63;
    	}
    	return 0;
    }
    private static byte[] base642bin(byte[] from) {
    	int i,j,x1,x2,x3,x4;
    	byte[] out;
    	int len = from.length;
    	out = new byte[( len * 3/4 ) + 8 ];
    	for(i=0,j=0;i+3<len;i+=4){
    		x1=base64Val(from[i]);
    		x2=base64Val(from[i+1]);
    		x3=base64Val(from[i+2]);
    		x4=base64Val(from[i+3]);
    		out[j++]=(byte)((x1*4) + ((x2 & 0x30)/16));
    		if (x3!=-1) out[j++]=(byte)(((x2 & 0x0F)*16) + ((x3 & 0x3C)/4));
    		if (x4!=-1) out[j++]=(byte)(((x3 & 0x03)*64) + (x4 & 0x3F));
    	}
    	if (i<len) {
    		x1 = base64Val(from[i]);
    		if (i+1<len)
    			x2=base64Val(from[i+1]);
    		else 
    			x2=-1;
    		if (i+2<len)		
    			x3=base64Val(from[i+2]);
    		else
    			x3=-1;
    		if(i+3<len)	
    			x4=base64Val(from[i+3]);
    		else x4=-1;
    		if (x2!=-1) {
    			out[j++]=(byte)( (x1*4) + ((x2 & 0x30)/16));
    			if (x3==-1) {
    				out[j++]=(byte)(((x2 & 0x0F)*16) + ((x3 & 0x3C)/4));
    				if (x4==-1) {
    					out[j++]=(byte)(((x3 & 0x03)*64) | (x4 & 0x3F));
    				}
    			}
    		}
    			
    	}
    	byte[] x = new byte[j];
    	System.arraycopy(out,0,x,0,j);    	
    	return x;
    }
    private static byte[] base64=String.valueOf("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/").getBytes();
    private static byte[] bin2base64( byte[] from)
    {
    	int i,k;
    	int triplets,rest;
    	byte[] out;
    	int ptr=0;
    	int len = from.length;
    	triplets = len/3;
    	rest = len%3;
    	out = new byte[ triplets * 4 + 8 ];
    	
    	ptr = 0;
    	for(i=0;i<triplets*3;i+=3){
    		k = (from[i]&0xFC) / 4;
    		out[ptr++]=base64[k];

    		k = (from[i]&0x03) * 16;
    		k += (from[i+1]&0xF0)/16;
    		out[ptr++]=base64[k];

    		k = (from[i+1]&0x0F)*4;
    		k += (from[i+2]&0xC0)/64;
    		out[ptr++]=base64[k];

    		k = (from[i+2]&0x3F);
    		out[ptr++]=base64[k];
    	}
    	i=triplets*3;
    	switch(rest){
    		case 0:
    			break;
    		case 1:
    			k = (from[i]&0xFC)/4;
    			out[ptr++]=base64[k];

    			k = (from[i]&0x03)*16;
    			out[ptr++]=base64[k];

    			out[ptr++]='=';

    			out[ptr++]='=';
    			break;
    		case 2:
    			k = (from[i]&0xFC)/4;
    			out[ptr++]=base64[k];

    			k = (from[i]&0x03)*16;
    			k +=(from[i+1]&0xF0)/16;
    			out[ptr++]=base64[k];

    			k = (from[i+1]&0x0F)*4;
    			out[ptr++]=base64[k];

    			out[ptr++]='=';
    			break;
    	}    	
        byte[] x = new byte[ptr];
        System.arraycopy(out,0,x,0,ptr);
    	return x;
    }    
    
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
    
 /*   public static long hexa2long(String hexa) {
    	long result = 0;
    	
    	byte[] h = hexa.getBytes();
    	
    	int i, hi, low;
    	for (i = 0; i < h.length; i += 2){
    		result <<= 8;
    		hi = 0;
    		low = 0;
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
    		result |= (hi*16 + low);
    	}
    	
    	return result;
    }

    public static String long2hexa(long l) {
    	String temp = null;
    	String result = null;
    	temp = Long.toHexString(l);
    	if (temp.length() != 12){
    		StringBuffer sBuff = new StringBuffer();
    		for (int i = 0; i < 12 - temp.length(); i++){
    			sBuff.append("0");
    		}
    		sBuff.append(temp);
    		result = sBuff.toString();
    	}
    	else{
    		result = temp;
    	}
    	
    	return result;
    }
   */ 
	
    private static byte[] hexa= {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
    public static byte[] bin2hexa(byte[] bin) {
    	byte[] h = new byte[bin.length*2];
    	int i,j;
    	for(i=0,j=0;i<h.length&&j<bin.length;i+=2,j++) {
    		h[i]= hexa[(bin[j]&0xF0)>>4];
    		h[i+1]= hexa[bin[j]&0x0F];
    	}
    	return h;
    }  


    public static DigestMD5Response getMD5Response(
    		DigestAKAResponse akaResponse,
    		String algorithm,
    		String nonce,
			int nonceCount,
			String cNonce,    		
			String username,
			String realm,							
			String method,
			String uri,
			String contentBody,
			String qop) 
    {   
    	byte[] password = null;
    	int k;
    	if (algorithm.equalsIgnoreCase(AKA_V1)) {
    		password = akaResponse.getRes();
    	}
    	else if (algorithm.equalsIgnoreCase(AKA_V2)) {
    		password = new byte[akaResponse.getRes().length+
    		                    akaResponse.getIk().length+
    		                    akaResponse.getCk().length];
    		k=0;
    		System.arraycopy(akaResponse.getRes(),0, password,k,akaResponse.getRes().length);
    		k+=akaResponse.getRes().length;
    		System.arraycopy(akaResponse.getIk(),0, password,k,akaResponse.getIk().length);
    		k+=akaResponse.getIk().length;
    		System.arraycopy(akaResponse.getCk(),0, password,k,akaResponse.getCk().length);    		
    	}
    	else {
    		password=new byte[0];
    	}    	
    	//System.out.println("The MD5 password is:"+(new String(password))+"\n");
    	return DigestMD5.getResponse(algorithm,nonce,username,realm,
    			password,nonceCount,cNonce,method,uri,contentBody,qop);
    }
    
    
    public static DigestAKAResponseImpl getResponse(String Nonce,String secretKey,byte[] sqnMs, 
    		byte[] op,byte[] amf,byte[] amfStar, boolean useAK,
    		boolean simulateISIM, byte[][] sqnVector, int indLen, int delta, int L) {
    	
    	byte[] sqnNew;
    	boolean isSync;
    	byte[] smallKey = secretKey.getBytes();
    	byte[] key = new byte[16];
    	Arrays.fill(key,0,16,(byte)0);
    	System.arraycopy(smallKey,0,key,0,smallKey.length>16?16:smallKey.length);
    	
    	byte[] nonce = base642bin(Nonce.getBytes());
    	if (nonce.length<32) {
    		System.err.print("Nonce decoded from "+Nonce+" is too short: "+nonce.length+"<32");
    		return null;
    	}
    	byte[] rand = new byte[16];
    	byte[] autn = new byte[16];
    	byte[] sqn = new byte[6];
    	byte[] mac = new byte[8];    	
    	
    	System.arraycopy(nonce,0,rand,0,16);
//    	System.out.println("\n\nRAND     : " + new String(bin2hexa(rand)));
    	System.arraycopy(nonce,16,autn,0,16);
    	System.arraycopy(nonce,16,sqn,0,6);
    	System.arraycopy(nonce,24,mac,0,8);
//    	System.out.println("\n\nMAC     : " + new String(bin2hexa(mac)));    	    	
        try
        {        	
        	byte[] opc = generateOp_c(key, op);
//            System.out.println("KEY     : " + new String(bin2hexa(key)));
//            System.out.println("OPC     : " + new String(bin2hexa(opc)));
            byte[] res = f2(key, rand, opc);
//            System.out.println("RES(f2) : " + new String(bin2hexa(res)));
            byte[] ck = f3(key, rand, opc);            
//            System.out.println("CK(f3)  : " + new String(bin2hexa(ck)));
            byte[] ik = f4(key, rand, opc);
//            System.out.println("IK(f4)  : " + new String(bin2hexa(ik)));
            byte[] ak = f5(key, rand, opc);
//            System.out.println("AK(f5)  : " + new String(bin2hexa(ak)));
            if (useAK) {
            	for(int i=0;i<sqn.length;i++)
            	{
            		sqn[i]^=ak[i];
            	}
            }
            byte[] xmac = f1(key, rand, opc, sqn, amf);
//            System.out.println("MAC     : " + new String(bin2hexa(mac)));
//            System.out.println("XMAC(f1): " + new String(bin2hexa(xmac)));            
            byte[] akResynch = f5star(key, rand, opc);            
//            System.out.println("AKResynch(f5star):     " + new String(bin2hexa(akResynch)));

//            System.out.println("SQN_HE  : " + new String(bin2hexa(sqn)));
//            System.out.println("SQN_MS  : " + new String(bin2hexa(sqnMs)));

        	BooleanResults bool = new BooleanResults();     
            if (simulateISIM == true){

            	if (SQNinRange(sqnMs, sqn, sqnVector, indLen, delta, L, bool)){
            		// we accept the sqn value from HSS
            		sqnNew = sqn;
            		isSync = true;
            	}
            	else{
            		// the sqn value remains the same, it is not accepted the sqnMs and synch is performed with the HSS
            		sqnNew = sqnMs;
            		isSync = false;
            	}
            }
            else{
            	// we accept always the SQN from HSS
            	sqnNew = sqn;
            	isSync = true;
            }
            	
 /*           if (!SQNinRange(sqn,sqnMs)) {
            	sqnNew = getNextSQN(sqnMs);
            	isSync = true;
            }else {
            	sqnNew = getNextSQN(sqn);
            	isSync = false;
            }
*/            
//            System.out.println("SQN_NEW : " + new String(bin2hexa(sqnNew)));
            

            byte[] macS = f1star(key, rand, opc, sqnNew,amfStar);            
//            System.out.println("MACS(f1star)    : " + new String(bin2hexa(mac_s)));

            byte[] auts = new byte[sqnNew.length+macS.length];
            if (useAK) {
            	for(int i=0;i<sqnNew.length;i++)
            	{
            		auts[i]=(byte)(sqnNew[i] ^ akResynch[i]);
            	}
            }else
            	System.arraycopy(sqnNew,0,auts,0,sqnNew.length);
            System.arraycopy(macS,0,auts,sqn.length,macS.length);
//            System.out.println("AUTS()16  : " + new String(bin2hexa(auts)));            
//            System.out.println("AUTS()64  : " + new String(bin2base64(auts)));
//            System.out.println("AUTS()    : " + new String(bin2hexa(base642bin(bin2base64(auts)))));
            DigestAKAResponseImpl r = new DigestAKAResponseImpl(new String(bin2hexa(res)),
            											new String(bin2base64(auts)),
            											isSync,
            											!Arrays.equals(mac,xmac),
            											sqnNew,
            											bool.isSqnMS,
            											res,ck,ik);
//            System.out.println("AUTS   : " + r.auts);
//            System.out.println("RESP   : " + r.response);
            return r;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }        
    }
  
    private static long bin2long(byte[] b){
    	long result = 0;
    	for (int i = 0; i < b.length; i++){
    		result <<= 8;
    		result |= (b[i] & 255);
    	}
    	return result;
    }
    
    private static boolean SQNinRange(byte[] sqnMS, byte[] sqnHE, byte[][] sqnVector, int indLen, int delta, int L, BooleanResults b){

    	// get the long values for sqnMS and sqnHE, these are needed for comparations operations 
    	//System.out.println(new String(DigestAKA.bin2hexa(sqnMS)));
    	//System.out.println(new String(DigestAKA.bin2hexa(sqnHE)));
    	long sqnMSLong = DigestAKA.bin2long(sqnMS);
    	long sqnHELong = DigestAKA.bin2long(sqnHE);
    	//System.out.println("sqnMSLong:" + sqnMSLong);
    	//System.out.println("sqnHELong:" + sqnHELong);
    	
    	long seqMS = sqnMSLong >> indLen;
    	long seqHE = sqnHELong >> indLen;
    	//System.out.println("seqMS:" + seqMS);
    	//System.out.println("seqHE:" + seqHE);
    	
    	// get the index
    	long mask = 0;
    	for (int i=0; i<indLen; i++){
    		mask <<= 1;
    		mask |= 1;
    	}
    	long index = mask & sqnHELong;
    	//System.out.println("index:" + index);
    	
    	byte[] sqnMSIndex = sqnVector[(int)index];
    	long seqMSIndex = DigestAKA.bin2long(sqnMSIndex) >> indLen;
    	//System.out.println("seqMSIndex:" + seqMSIndex);

    	// C.2.1 from TS 33.102
    	if (seqHE - seqMS > delta)
    		return false;
    	
    	// C.2.2 from TS 33.102
    	if (seqMS - seqHE > L)
    		return false;
    	
    	if (seqHE <= seqMSIndex)
    		return false;
    	
    	if (seqHE > seqMS){
    		b.isSqnMS = true;
    		b.isInRange = true;
    	}
    	return true;
    }
    
    /*private static boolean SQNinRange(byte[] sqnHe,byte[] sqnMs)
    {
    	for(int i=0;i<5;i++)
    		if (sqnHe[i]!=sqnMs[i]) return false;
    	return sqnHe[5]/32 == sqnMs[5]/32;
    }

    private static byte[] getNextSQN(byte[] sqn) {
    	byte[] x = new byte[sqn.length];
    	System.arraycopy(sqn,0,x,0,sqn.length);
    	int i=sqn.length-1;
    	do {
    		if (x[i]==(byte)255) {
    			x[i]=(byte)0;
    			i--;
    		}else {
    			x[i]++;
    			break;
    		}    		
    	} while(i>=0);
    	return x;
    }
*/    
    public static byte[] generateOp_c(byte[] secretKey, byte[] op) throws InvalidKeyException            
    {
        kernel.init(secretKey);        
        
        byte[] byteOp_c = kernel.encrypt(op);

        byte[] op_c = new byte[byteOp_c.length];
        for (int i = 0; i < byteOp_c.length; i++)
        {
            op_c[i] ^= (byte) (byteOp_c[i] ^ op[i]);
        }
        return op_c;
    }

    /*
     * the network authentication function
     */
    public static byte[] f1(byte[] secretKey, byte[] rand, byte[] op_c,
            byte[] sqn, byte[] amf) throws InvalidKeyException
    {
        kernel.init(secretKey);
        byte[] temp = new byte[16];
        byte[] in1 = new byte[16];
        byte[] out1 = new byte[16];
        byte[] rijndaelInput = new byte[16];

        for (int i = 0; i < rand.length && i<op_c.length; i++)
        {
            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);
        }
        
        temp = kernel.encrypt(rijndaelInput);

        // expand sqn and amf into 128 bit value
        for (int i = 0; i < sqn.length; i++)
        {
            in1[i] = sqn[i];
            in1[i + 8] = sqn[i];
        }
        
        for (int i = 0; i < amf.length; i++)
        {
            in1[i + 6] = amf[i];
            in1[i + 14] = amf[i];
        }

        /*
         * XOR op_c and in1, rotate by r1=64, and XOR on the constant c1 (which
         * is all zeroes)
         */

        for (int i = 0; i < in1.length; i++)
        {
            rijndaelInput[(i + 8) % 16] = (byte) (in1[i] ^ op_c[i]);
        }
        
        /* XOR on the value temp computed before */

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[i] ^= temp[i];
        }
        
        out1 = kernel.encrypt(rijndaelInput);
        for (int i = 0; i < 16; i++)
        {
            out1[i] = (byte) (out1[i] ^op_c[i]);
        }
        
        byte[] mac = new byte[8];
        for (int i = 0; i < 8; i++)
        {
            mac[i] = (byte) out1[i];
        }
        
        return mac;
    }

    public static byte[] f1star(byte[] secretKey, byte[] rand, byte[] op_c, byte[] sqn,byte[] amfStar)
            throws InvalidKeyException
    {
        //Amf amfObj= new Amf("b9b9");
        //return f1(secretKey, rand, op_c, sqn, AMF);
        //return fOne(secretKey, randObj, op_cObj, sqnObj, amfObj);        
        kernel.init(secretKey);
        byte[] temp = new byte[16];
        byte[] in1 = new byte[16];
        byte[] out1 = new byte[16];
        byte[] rijndaelInput = new byte[16];

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);
        }
        
        temp = kernel.encrypt(rijndaelInput);

        // expand sqn and amf into 128 bit value
        for (int i = 0; i < 6; i++)
        {
            in1[i] = sqn[i];
            in1[i + 8] = sqn[i];
        }
        
        for (int i = 0; i < 2; i++)
        {
            in1[i + 6] = amfStar[i];
            in1[i + 14] = amfStar[i];
        }

        
         /* XOR op_c and in1, rotate by r1=64, and XOR on the constant c1 (which
         * is all zeroes)*/
         

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[(i + 8) % 16] = (byte) (in1[i] ^ op_c[i]);
        }
        
         //XOR on the value temp computed before 

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[i] ^= temp[i];
        }
        
        out1 = kernel.encrypt(rijndaelInput);
        for (int i = 0; i < 16; i++)
        {
            out1[i] ^= op_c[i];
        }
        
        byte[] mac = new byte[8];
        for (int i = 0; i < 8; i++)
        {
            mac[i] = (byte) out1[i+8];
        }
        
        return mac;
    }

    public static byte[] f2(byte[] secretKey, byte[] rand, byte[] op_c)
            throws InvalidKeyException
    {
        kernel.init(secretKey);
        byte[] temp = new byte[16];
        byte[] out = new byte[16];
        byte[] rijndaelInput = new byte[16];

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);
        }
        
        temp = kernel.encrypt(rijndaelInput);

        /*
         * To obtain output block OUT2: XOR OPc and TEMP, rotate by r2=0, and
         * XOR on the constant c2 is all zeroes except that the last bit is 1).
         */

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[i] = (byte) (temp[i] ^ op_c[i]);
        }
        
        rijndaelInput[15] ^= 1;

        out = kernel.encrypt(rijndaelInput);

        for (int i = 0; i < 16; i++)
        {
            out[i] = (byte) (out[i] ^op_c[i]);
        }
        
        byte[] res = new byte[8];
        for (int i = 0; i < 8; i++)
        {
            res[i] = (byte) out[i + 8];
        }
        
        return res;
    }

    public static byte[] f3(byte[] secretKey, byte[] rand, byte[] op_c)
            throws InvalidKeyException
    {
        kernel.init(secretKey);
        byte[] temp = new byte[16];
        byte[] out = new byte[16];
        byte[] rijndaelInput = new byte[16];

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);
        }
        
        temp = kernel.encrypt(rijndaelInput);

        /*
         * To obtain output block OUT3: XOR OPc and TEMP, rotate by r3=32, and
         * XOR on the constant c3 (which * is all zeroes except that the next to
         * last bit is 1).
         */

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[(i + 12) % 16] = (byte) (temp[i] ^ op_c[i]);
        }
        rijndaelInput[15] ^= 2;

        out = kernel.encrypt(rijndaelInput);
        for (int i = 0; i < 16; i++)
        {
            out[i] = (byte) (out[i] ^op_c[i]);
        }
        
        byte[] ck = new byte[16];
        for (int i = 0; i < 16; i++)
        {
            ck[i] = (byte) out[i];
        }
        return ck;
    }

    public static byte[] f4(byte[] secretKey, byte[] rand, byte[] op_c)
            throws InvalidKeyException
    {
        kernel.init(secretKey);
        byte[] temp = new byte[16];
        byte[] out = new byte[16];
        byte[] rijndaelInput = new byte[16];

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);
        }
        
        temp = kernel.encrypt(rijndaelInput);

        /*
         * To obtain output block OUT4: XOR OPc and TEMP, rotate by r4=64, and
         * XOR on the constant c4 (which * is all zeroes except that the 2nd
         * from last bit is 1).
         */

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[(i + 8) % 16] = (byte) (temp[i] ^ op_c[i]);
        }
        rijndaelInput[15] ^= 4;

        out = kernel.encrypt(rijndaelInput);
        for (int i = 0; i < 16; i++)
        {
            out[i] = (byte) (out[i] ^op_c[i]);
        }
        
        byte[] ik = new byte[16];
        for (int i = 0; i < 16; i++)
        {
            ik[i] = (byte) out[i];
        }
        return ik;
    }

    public static byte[] f5(byte[] secretKey, byte[] rand, byte[] op_c)
            throws InvalidKeyException
    {
        kernel.init(secretKey);
        byte[] temp = new byte[16];
        byte[] out = new byte[16];
        byte[] rijndaelInput = new byte[16];

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);
        }
        temp = kernel.encrypt(rijndaelInput);

        /*
         * To obtain output block OUT2: XOR OPc and TEMP, rotate by r2=0, and
         * XOR on the constant c2 is all zeroes except that the last bit is 1).
         */

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[i] = (byte) (temp[i] ^ op_c[i]);
        }
        
        rijndaelInput[15] ^= 1;

        out = kernel.encrypt(rijndaelInput);

        for (int i = 0; i < 16; i++)
        {
            out[i] = (byte) (out[i] ^op_c[i]);
        }
        
        byte[] ak = new byte[6];
        for (int i = 0; i < 6; i++)
        {
            ak[i] = (byte) out[i];
        }
        
        return ak;
    }

    public static byte[] f5star(byte[] secretKey, byte[] rand, byte[] op_c)
            throws InvalidKeyException
    {
        kernel.init(secretKey);
        byte[] temp = new byte[16];
        byte[] out = new byte[16];
        byte[] rijndaelInput = new byte[16];

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[i] = (byte) (rand[i] ^ op_c[i]);
        }
        temp = kernel.encrypt(rijndaelInput);

        /*
         * To obtain output block OUT5: XOR OPc and TEMP, rotate by r5=96, and
         * XOR on the constant c5 (which * is all zeroes except that the 3rd
         * from last bit is 1).
         */

        for (int i = 0; i < 16; i++)
        {
            rijndaelInput[(i + 4) % 16] = (byte) (temp[i] ^ op_c[i]);
        }
        
        rijndaelInput[15] ^= 8;

        out = kernel.encrypt(rijndaelInput);

        for (int i = 0; i < 16; i++)
        {
            out[i] = (byte) (out[i] ^op_c[i]);
        }
        
        byte[] ak = new byte[6];
        for (int i = 0; i < 6; i++)
        {
            ak[i] = (byte) out[i];
        }
        
        return ak;
    }
    
    public static void main(String args[]){
    	System.out.println("Testing!");
    	//System.out.println(DigestAKA.hexa2long("800000000001") + "\n" + ((long)1<<47));
    	//System.out.println(DigestAKA.long2hexa(DigestAKA.hexa2long("000000000001")));
    	//byte[] b = {'a','2'};
    	//System.out.println(new String(b));
    	System.out.println("LONG:" + DigestAKA.bin2long(DigestAKA.hexa2bin("100000000001")));
    	byte[] xmac =null;
    	byte[] key = hexa2bin("465b5ce8b199b49faa5f0a2ee238a6bc");
    	byte[] op = hexa2bin("cdc202d5123e20f62b6d676ac72cb318");
    	byte[] rand = hexa2bin("23553cbe9637a89d218ae64dae47bf35");
    	byte[] sqn = hexa2bin("ff9bb4d0b607");
    	byte[] amf = hexa2bin("b9b9");
    	byte[] opc = null;
    	try {
    		opc = generateOp_c(key,op);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			 xmac = f1(key,rand, opc, sqn, amf);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	System.out.println(new String(bin2hexa(opc)));
    	System.out.println(new String(bin2hexa(xmac)));
    	//byte[] test = f1()
    }  
    
}

class BooleanResults{
	public boolean isSqnMS = false;
	public boolean isInRange = false;
}