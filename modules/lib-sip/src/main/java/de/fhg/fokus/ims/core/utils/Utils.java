package de.fhg.fokus.ims.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * Provides common utilities used through out the client ims-core
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public class Utils 
{
	
	public static String ConvertByteToString(InputStream bais)
	{
		try 
		{			
			int length = bais.available();
			byte [] buff = new byte[length];
			bais.read(buff);
			return new String(buff,"utf-8");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	 /**
     * Tokenizes a string, that is, splits it in parts based on a separator.
     * <p>
     * Example: The string "a b c" and separator ' ' will result in a vector
     * with three elements, "a", "b", and "c".
     * 
     * @param string the string to tokenize
     * @param separator the separator character
     * @return the tokens
     */
    public static String[] tokenize(String string, char separator) {
        return tokenize(string, "" + separator);
    }

    /**
     * Tokenizes a string, that is, splits it in parts based on a separator.
     * <p>
     * Example: The string "a b c" and separator " " will result in a vector
     * with three elements, "a", "b", and "c".
     * 
     * @param string the string to tokenize
     * @param separator the separator string
     * @return the tokens
     */
    public static String[] tokenize(String string, String separator) {
        Vector tokens = new Vector();
        int startOfToken = 0;
        boolean done = false;
        while (!done) {
            int endOfToken = string.indexOf(separator, startOfToken);
            if (endOfToken != -1) {
                tokens.addElement(string.substring(startOfToken, endOfToken));
                startOfToken = endOfToken + separator.length();
            } else {
                done = true;
            }
        }
        tokens.addElement(string.substring(startOfToken));

        String[] result = new String[tokens.size()];
        tokens.copyInto(result);
        return result;
    }

    /**
     * Tokenizes a string, that is, splits it in space separated parts.
     * <p>
     * This is equivalent to calling <code>tokenize(string, ' ')</code>.
     * 
     * @param string the string to tokenize
     * @return the tokens
     */
    public static String[] tokenize(String string) {
        return tokenize(string, ' ');
    }

    /**
     * Concatenates the input parameters the the operator. Example, with
     * operator ";app_subtype" and parameters "{sdp example}" will result in
     * ";app_subtype="sdp,example".
     * 
     * @param operator the operator
     * @param parameters the parameters
     * @return the concatenated string
     */
    public static String concatenate(String operator, String[] parameters) {
        StringBuffer result = new StringBuffer();
        if (parameters.length != 0) {
            result.append(operator + "=\"");
        }
        for (int i = 0; i < parameters.length; i++) {
            if (i != 0) {
                result.append(",");
            }
            result.append(parameters[i]);
            if (i == parameters.length - 1) {
                result.append("\"");
            }
        }
        return result.toString();
    }

}
