package org.openxsp.java.session;

import org.vertx.java.core.json.JsonObject;

/**
 * Created by Frank Schulze on 22.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public class SessionControlEndSessionImpl extends SessionControlMessgeImpl implements SessionControlEndSession {

    private SessionControlMessageHeader header;

    @Override
    public SessionControlMethodEnum getMethod() {
        return super.getMethod();
    }

    @Override
    public void setMethod(SessionControlMethodEnum methodEnum) {
        super.setMethod(methodEnum);
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
    public SessionControlMessage parse(JsonObject jsonMessage) {
        if (jsonMessage == null)
            return null;
        this.setMethod(SessionControlMethodEnum.END);
        super.setSessionID(jsonMessage.getString(SessionControlHeaderField.sessionID.toLowerCase()));
        super.setAddress(jsonMessage.getString(SessionControlHeaderField.address));
        header = new SessionControlMessageHeader();
        header.parse(jsonMessage.getObject(SessionControlHeaderField.header.toLowerCase()));

        return this;
    }

    @Override
    public JsonObject create() {
        JsonObject message = new JsonObject();

        message.putString(SessionControlHeaderField.method, SessionControlMethodEnum.END.toString().toLowerCase());

        if (super.getSessionID() != null)
            message.putString(SessionControlHeaderField.sessionID, super.getSessionID());

	if (super.getAddress() == null)
            return null;
        else
            message.putString(SessionControlHeaderField.address, super.getAddress());
        if (this.header != null)
            message.putObject(SessionControlHeaderField.header, header.create());
        return message;
    }

}
