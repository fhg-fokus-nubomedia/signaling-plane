package org.openxsp.java.session;

/**
 * Created by Frank Schulze on 16.05.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public interface SessionControlInfoSession extends SessionControlMessage {

    String getFrom();

    void setFrom(String from);

    String getTo();

    void setTo(String to);

    String getContent();

    void setContent(String content);

    String getContentType();

    void setContentType(String contentType);

    SessionControlMessageHeader getHeader();

    void setHeader(SessionControlMessageHeader header);
}
