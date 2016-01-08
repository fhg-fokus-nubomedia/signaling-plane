package javax.ims;

/**
 * An ImsException indicates an unexpected error condition in a method.
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public class ImsException extends Exception
{

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an ImsException with null  as its error detail message.
	 */
    public ImsException()
    {
    }
    
    /**
     * Constructs an ImsException with the specified detail message. 
     * The error message string reason can later be retrieved by the Throwable.getMessage()  
     * method of class java.lang.Throwable.
     * 
     * @param reason - the detail message
     */
    public ImsException(String reason)
    {
        super(reason);
    }

    public ImsException(String reason, Throwable t)
    {
        super(reason, t);
    }

        
}
