package org.openxsp.java.session;

import org.vertx.java.core.json.JsonObject;

/**
 * Created by Frank Schulze on 22.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public class SessionControlCancelSessionImpl extends SessionControlMessgeImpl implements SessionControlCancelSession {
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
        this.setMethod(SessionControlMethodEnum.CANCEL);
        super.setSessionID(jsonMessage.getString(SessionControlHeaderField.sessionID.toLowerCase()));
        return this;
    }

    @Override
    public org.vertx.java.core.json.JsonObject create() {
        JsonObject message = new JsonObject();

        message.putString(SessionControlHeaderField.method, SessionControlMethodEnum.CANCEL.toString().toLowerCase());
        if (super.getSessionID() == null)
            message.putString(SessionControlHeaderField.sessionID, super.getSessionID());

        return message;
    }
}
