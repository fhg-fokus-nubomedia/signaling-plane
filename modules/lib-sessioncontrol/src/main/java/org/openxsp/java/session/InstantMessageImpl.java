package org.openxsp.java.session;

import org.vertx.java.core.json.JsonObject;

/**
 * Created by xsp on 7/16/14.
 */
public class InstantMessageImpl extends SessionControlMessgeImpl implements InstantMessage {

    private String from;
    private String to;
    private String content;
    private String contentType;

    public InstantMessageImpl(){}

    public InstantMessageImpl(String from, String to, String content, String contentType){
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

    @Override
    public String getTo() {
        return this.to;
    }

    @Override
    public void setTo(String to) {
        this.to = to;
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

    public SessionControlMessageHeader getHeader() {
        return header;
    }

    public void setHeader(SessionControlMessageHeader header) {
        this.header = header;
    }

    private SessionControlMessageHeader header;

    @Override
    public SessionControlMessage parse(JsonObject jsonMessage) {
        if (jsonMessage == null){
            System.out.println("Could not parse Instant Message - Json Object is null - nothing to parse");
            return null;
        }

        this.setMethod(SessionControlMethodEnum.MESSAGE);
        super.setSessionID(jsonMessage.getString(SessionControlHeaderField.sessionID.toLowerCase()));

        /*if (!super.checkHeaderFields()){
            System.out.println("Could not parse InstantMessage - Header field check not passed");
            return null;
        }*/

        header = new SessionControlMessageHeader();
        header.parse(jsonMessage.getObject(SessionControlHeaderField.header.toLowerCase()));


        this.setFrom(jsonMessage.getString(SessionControlHeaderField.from.toLowerCase()));
        this.setTo(jsonMessage.getString(SessionControlHeaderField.to.toLowerCase()));

        this.setContent(jsonMessage.getString(SessionControlHeaderField.content.toLowerCase()));
        this.setContentType(jsonMessage.getString(SessionControlHeaderField.contentType.toLowerCase()));

        return this;
    }

    @Override
    public JsonObject create() {
        JsonObject message = new JsonObject();

        message.putString(SessionControlHeaderField.method, SessionControlMethodEnum.MESSAGE.toString().toLowerCase());

        if (super.getAddress() == null)
            return null;
        else
            message.putString(SessionControlHeaderField.address, super.getAddress());

        if (super.getSessionID() == null)
            message.putString(SessionControlHeaderField.sessionID, super.getSessionID());

        if (this.header != null)
            message.putObject(SessionControlHeaderField.header, header.create());

        if (this.from != null)
            message.putString(SessionControlHeaderField.from, this.from);
        if (this.to != null)
            message.putString(SessionControlHeaderField.to, this.to);
        if (this.content != null)
            message.putString(SessionControlHeaderField.content, this.content);
        if (this.contentType != null)
            message.putString(SessionControlHeaderField.contentType, this.contentType);

        return message;
    }
}
