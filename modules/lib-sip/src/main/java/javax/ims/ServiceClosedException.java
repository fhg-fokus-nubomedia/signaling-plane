package javax.ims;

import java.io.IOException;

/**
 * A ServiceClosedException indicates that a method is invoked on a Service that is closed.
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public class ServiceClosedException extends IOException 
{
	private static final long serialVersionUID = 4630882178596580040L;
 
	/**
	 * Constructs a ServiceClosedException with null  as its error detail message
	 */
	public ServiceClosedException()
    {
    }

	/**
	 * Constructs a ServiceClosedException with the specified detail message. 
	 * The error message string reason can later be retrieved by the Throwable.getMessage()  
	 * method of class java.lang.Throwable
	 * 
	 * @param reason - the detail message
	 */
	public ServiceClosedException(String reason)
    {
        super(reason);
    }

   
}
