package org.openxsp.java.session;

import org.vertx.java.core.json.JsonObject;

/**
 * Created by Frank Schulze on 22.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public class SessionControlMessgeImpl implements SessionControlMessage {

    private SessionControlMethodEnum methodE;
    private String sessionID;
    private String address;

    public SessionControlMethodEnum getMethod() {
        return this.methodE;
    }

    public void setMethod(SessionControlMethodEnum methodEnum) {
        this.methodE = methodEnum;
    }

    @Override
    public String getSessionID() {
        return this.sessionID;
    }

    @Override
    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    @Override
    public String getAddress() {
        return this.address;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public SessionControlMessage parse(JsonObject jsonMessage) {
        String method = jsonMessage.getString("method");

        if (method == null)
            return null;

        switch (SessionControlMethodEnum.valueOf(method.toUpperCase())) {
            case INIT:
                SessionControlCreateSession createSession = new SessionControlCreateSessionImpl();
                return createSession.parse(jsonMessage);
            case ACCEPT:
                SessionControlAcceptSession acceptSession = new SessionControlAcceptSessionImpl();
                return acceptSession.parse(jsonMessage);
            case CANCEL:
                SessionControlCancelSession cancelSession = new SessionControlCancelSessionImpl();
                return cancelSession.parse(jsonMessage);
            case CONFIRM:
                SessionControlConfirmSessionEstablishment confirmSessionEstablishment = new SessionControlConfirmSessionEstablishmentImpl();
                return confirmSessionEstablishment.parse(jsonMessage);
            case UPDATE:
                SessionControlUpdateSession controlUpdateSession = new SessionControlUpdateSessionImpl();
                return controlUpdateSession.parse(jsonMessage);
            case END:
                SessionControlEndSession endSession = new SessionControlEndSessionImpl();
                return endSession.parse(jsonMessage);
            case INFO:
                SessionControlInfoSession info = new SessionControlInfoSessionImpl();
                return info.parse(jsonMessage);
            case MESSAGE:
                InstantMessage instantMessage = new InstantMessageImpl();
                return instantMessage.parse(jsonMessage);
            default:
                System.out.println("Unknown method "+this.methodE);
                return null;
        }
    }

    @Override
    public JsonObject create() {
        switch (this.methodE) {
            case INIT:
                SessionControlCreateSessionImpl createSession = (SessionControlCreateSessionImpl)this;
                return createSession.create();
            case ACCEPT:
                SessionControlAcceptSessionImpl acceptSession = (SessionControlAcceptSessionImpl)this;
                return acceptSession.create();
            case CANCEL:
                SessionControlCancelSessionImpl cancelSession = (SessionControlCancelSessionImpl)this;
                return cancelSession.create();
            case CONFIRM:
                SessionControlConfirmSessionEstablishmentImpl establishment = (SessionControlConfirmSessionEstablishmentImpl)this;
                return establishment.create();
            case UPDATE:
                SessionControlUpdateSessionImpl updateSession = (SessionControlUpdateSessionImpl)this;
                return updateSession.create();
            case END:
                SessionControlEndSessionImpl endSession = (SessionControlEndSessionImpl)this;
                return endSession.create();
            case INFO:
                SessionControlInfoSessionImpl info = (SessionControlInfoSessionImpl)this;
                return info.create();
            case MESSAGE:
                InstantMessageImpl im = (InstantMessageImpl)this;
                return im.create();
            default:
                System.out.println("Unknown method "+this.methodE);
                return null;
        }
    }

    @Override
    public boolean checkHeaderFields() {
        if (this.address == null)
            return false;
        if (this.methodE == null)
            return false;
        if (this.sessionID == null)
            return false;
        return true;
    }

    @Override
    public String getCallBackAddress() {
        if (this.address == null)
            return null;
//        if (this.sessionID == null)
//            return null;
        return (this.address /*+ ":" + this.sessionID*/);
    }
}
