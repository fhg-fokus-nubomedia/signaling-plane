package javax.sip.header;

/**
 * @author user
 *
 */
public interface AcceptContactHeader extends Parameters, Header
{

	/** Accept-contact header field label. */
	// public static final String NAME = Header.ACCEPT_CONTACT;
	public static final String NAME = "Accept-Contact";

	/** Type label. */
	public static final String GENERAL_TYPE = "type";

	/**
	 * Sets the "type" parameter.
	 *
	 * @param value
	 *            value of the parameter.
	 */
	public void setType(String value);

	/**
	 * Returns the value of the "type" parameter, or null if it is not set.
	 *
	 * @return the value of specified parameter
	 */
	public String getType();

}