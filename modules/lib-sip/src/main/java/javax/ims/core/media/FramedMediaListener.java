package javax.ims.core.media;

/**
 * A listener type for receiving notification of when FramedMedia content is transferred. 
 * When content becomes available the application is notified by a call to the contentReceived method. 
 * The application can then retrieve the content by either using the receiveBytes or receiveFile method on the FramedMedia interface.
 * 
 * @author Cheambe Alice <alice.cheambe@fraunhofer.fokus.de>
 *
 */
public interface FramedMediaListener
{

	/**
	 * Notifies the application when the content is completely received. The content can now be retrieved by calling receiveBytes or receiveFile with the corresponding messageId on the FramedMedia object.
	 *
	 * @param media - the FramedMedia that received the content
	 * @param messageId - identifies which content that is ready for retrieval
	 * @param size - the size of the content in bytes
	 * @param fileName - the file name or null if a file name is not associated with the messageId
	 */
	public void contentReceived(FramedMedia media,String messageId,int size, String fileName);
	
	/**
	 * Notifies the application that the content could not be received or that the content has been canceled by the sending 
	 * endpoint. The messageId is considered invalid after this callback and further use of this messageId is not possible.

	 * @param media - the FramedMedia that could not receive the content
	 * @param messageId - identifies which content that could not be received
	 */
	public void contentReceiveFailed(FramedMedia media,String messageId);

	
	/**
	 * Notifies the application when there is progress to be reported. This will be invoked for both outgoing and incoming content.

	 * @param media - the FramedMedia that received/sent the content
	 * @param messageId - identifies which content that is being transferred
	 * @param bytesTransferred - the number of bytes that is transferred
	 * @param bytesTotal - the number of total bytes in the content, this parameter is -1 if the size is not know
	 */
	public void transferProgress(FramedMedia media,String messageId, int bytesTransferred, int bytesTotal);
	
	/**
	 * <p>Notifies the application when the content corresponding to the messageId has been successfully delivered to the remote endpoint. 
	 * The messageId is considered invalid after this callback and further use of this messageId is not possible.
	 * <p>This method is invoked at the endpoint that sent the content.
	 * 
	 * @param media - the FramedMedia that sent the content
	 * @param messageId - identifies which content that has been transferred
	 */
	public void deliverySuccess(FramedMedia media, String messageId);
	
	/**
	 * <p>Notifies the application when the content corresponding to the messageId has not been successfully delivered to the remote 
	 * endpoint. The messageId is considered invalid after this callback and further use of this messageId is not possible.
	 * 
	 * <p>This method is invoked at the endpoint that sent the content. See [RFC4975] for valid status codes.
	 * 
	 * @param media - the FramedMedia that sent the content
	 * @param messageId - identifies which content that could not be transferred
	 * @param statusCode - the status code why the transaction failed
	 * @param reasonPhrase - the reason phrase
	 */
	public void deliveryFailure(FramedMedia media, String messageId, int statusCode, String reasonPhrase);

	/**
	 * Notifies the application when an I/O error has occured.
	 * 
	 * @param media - the concerned FramedMedia
	 */
	public void connectionError(FramedMedia media);

}
