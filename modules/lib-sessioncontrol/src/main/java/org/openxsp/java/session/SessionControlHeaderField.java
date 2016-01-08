package org.openxsp.java.session;

/**
 * Created by Frank Schulze on 23.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public class SessionControlHeaderField {

    //Header for all
    public final static String method = "method";
    public final static String sessionID = "session_id";
    public final static String address = "address";

    //INIT
    public final static String from = "from";
    public final static String to = "to";
    public final static String content = "content";
    public final static String contentType = "content_type";
    public final static String header = "header";

    //Header
    public final static String userAgent = "User-Agent";
    public final static String applicationSpecificHeader = "ApplicationSpecificHeader";
    //ToDo(lwo): please note that the protocol is not using a lower case style
    //ToDo(lwo): please have a look in to https://etherpad.fokus.fraunhofer.de/groups/ntt_rtc/Event_Bus_Protocol



}
