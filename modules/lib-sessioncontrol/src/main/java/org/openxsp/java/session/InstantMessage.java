package org.openxsp.java.session;


/**
 * Created by fsc on 7/16/14.
 */
public interface InstantMessage extends SessionControlMessage{

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
