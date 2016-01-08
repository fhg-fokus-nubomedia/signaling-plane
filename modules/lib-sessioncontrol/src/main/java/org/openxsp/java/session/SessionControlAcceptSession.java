package org.openxsp.java.session;

/**
 * Created by Frank Schulze on 22.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public interface SessionControlAcceptSession extends SessionControlMessage {
    String getContent();

    void setContent(String content);

    String getContentType();

    void setContentType(String contentType);

    SessionControlMessageHeader getHeader();

    void setHeader(SessionControlMessageHeader header);
}
