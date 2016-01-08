package de.fhg.fokus.ims.core.media;

import java.io.IOException;

import javax.ims.ImsException;
import javax.ims.core.media.BasicUnreliableMedia;

public interface BasicUnreliableMedia2 extends BasicUnreliableMedia {
	
    /**
     * Reads blocking from the socket.
     * 
     * @return
     * @throws IOException - if an I/O error occurs
     * @throws ImsException - if there is no content available for retrieval
     * @throws IllegalStateException - if the media's direction is DIRECTION_SEND or DIRECTION_INACTIVE 
     * @throws IllegalStateException - if the Media is not in STATE_ACTIVE
     */
	public abstract byte[] read()  throws IOException, IllegalArgumentException , IllegalStateException;

}
