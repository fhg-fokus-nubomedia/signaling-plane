package org.openxsp.java.session;

import org.vertx.java.core.json.JsonObject;

/**
 * Created by Frank Schulze on 22.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public class SessionControlCreateSessionImpl extends SessionControlMessgeImpl implements SessionControlCreateSession {

    private String from;
    private String to;
    private String content;
    private String contentType;
    private SessionControlMessageHeader header;

    public SessionControlCreateSessionImpl(){}

    public SessionControlCreateSessionImpl(String from, String to, String content, String contentType){
        this.from = from;
        this.to = to;
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public String getFrom() {
        return this.from;
    }

    @Override
    public void setFrom(String from) {
        this.from = from;
    }

    public SessionControlMessageHeader getHeader() {
        return header;
    }

    public void setHeader(SessionControlMessageHeader header) {
        this.header = header;
    }

    @Override
    public String getTo() {
        return this.to;
    }

    @Override
    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String getSessionID() {
        return super.getSessionID();
    }

    @Override
    public void setSessionID(String sessionID) {
        super.setSessionID(sessionID);
    }

    @Override
    public SessionControlMethodEnum getMethod() {
        return super.getMethod();
    }

    @Override
    public void setMethod(SessionControlMethodEnum methodEnum) {
        super.setMethod(methodEnum);
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public SessionControlMessage parse(JsonObject jsonMessage) {
        if (jsonMessage == null)
            return null;

        super.setSessionID(jsonMessage.getString(SessionControlHeaderField.sessionID.toLowerCase()));
        super.setAddress(jsonMessage.getString(SessionControlHeaderField.address));
        super.setMethod(SessionControlMethodEnum.INIT);

        if (!super.checkHeaderFields())
            return null;

        this.setFrom(jsonMessage.getString(SessionControlHeaderField.from.toLowerCase()));
        this.setTo(jsonMessage.getString(SessionControlHeaderField.to.toLowerCase()));

        this.setContent(jsonMessage.getString(SessionControlHeaderField.content.toLowerCase()));
        this.setContentType(jsonMessage.getString(SessionControlHeaderField.contentType.toLowerCase()));
        header = new SessionControlMessageHeader();
        header.parse(jsonMessage.getObject(SessionControlHeaderField.header.toLowerCase()));
        return this;
    }

    @Override
    public JsonObject create() {
        JsonObject message = new JsonObject();

        //Header
        message.putString(SessionControlHeaderField.method, SessionControlMethodEnum.INIT.toString().toLowerCase());
        if (super.getAddress() == null)
            return null;
        else
            message.putString(SessionControlHeaderField.address, super.getAddress());
        if (super.getSessionID() == null)
            return null;
        else
            message.putString(SessionControlHeaderField.sessionID, super.getSessionID());

        if (this.from != null)
            message.putString(SessionControlHeaderField.from, this.from);
        if (this.to != null)
            message.putString(SessionControlHeaderField.to, this.to);
        if (this.content != null)
            message.putString(SessionControlHeaderField.content, this.content);
        if (this.contentType != null)
            message.putString(SessionControlHeaderField.contentType, this.contentType);
        if (this.header != null)
            message.putObject(SessionControlHeaderField.header, header.create());

        return message;
    }


}
