package org.openxsp.java.session;

import org.vertx.java.core.json.JsonObject;

/**
 * Created by Frank Schulze on 22.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public interface SessionControlMessage {
    SessionControlMethodEnum getMethod();

    void setMethod(SessionControlMethodEnum methodEnum);

    String getSessionID();

    void setSessionID(String sessionID);

    String getAddress();

    void setAddress(String address);

    SessionControlMessage parse(JsonObject jsonMessage);

    JsonObject create();

    boolean checkHeaderFields();

    String getCallBackAddress();

}
