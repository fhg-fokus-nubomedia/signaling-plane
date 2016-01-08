package org.openxsp.cdn.connector.youtube.util;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import com.hazelcast.util.Base64;


/**
 * This class consists exclusively of static methods that convert to and from a PrivateKey. 
 * The PrivateKey is assumed to be encoded according to the PKCS #8 standard using the RSA algorithm.
 *
 */


public class PrivateKeyConverter {

	/**
	 * Converts a Base64 encoded String representation of a PrivateKey.
	 * The decoded Base64 is assumed to be encoded according to the PKCS #8 standard.
	 * Then the RSA algorithm is used to generate the Private Key.
	 *  
	 * @param key64 the Base64 encoded String to be converted 
	 * @return privateKey the PKCS8 Encoded PrivateKey 
	 * @throws NoSuchAlgorithmException if the encodedKey is null
	 * @throws InvalidKeySpecException if keySpec is inappropriate for the keyFactory 
	 */
	
	public static PrivateKey fromBase64StringToPrivateKey(String key64) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		byte[] encodedKey = Base64.decode(key64.getBytes());
		
		if(encodedKey==null) {
			System.out.println("encoded key is null");
			return null;
		}
		
	    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
	    
	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
	    
	    return privateKey;
	}

	/**
	 * Converts a PrivateKey into a Base64 encoded String.
	 * The PrivateKey bytes are returned according to the PKCS #8 standard and using the RSA algorithm.
	 * Then the bytes are encoded to Base 64 and returned as a String.
	 *  
	 * @param privateKey the PrivateKey to be converted 
	 * @return key64 the Base64 encoded String 
	 * @throws NoSuchAlgorithmException if no Provider supports a KeyFactorySpi implementation for the RSA algorithm.
	 * @throws InvalidKeySpecException if keySpec is inappropriate for the given privateKey or the privateKey cannot be processed
	 */

	public static String fromPrivateKeyToBase64String(PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {	
	    KeyFactory factory = KeyFactory.getInstance("RSA");
	    PKCS8EncodedKeySpec keySpec = factory.getKeySpec(privateKey,PKCS8EncodedKeySpec.class);
	    
	    byte[] bytes = keySpec.getEncoded();
	    String key64 = new String(Base64.encode(bytes));
	    	
	    return key64;
	}
	
	
}
