package org.openxsp.java.session;

/**
 * Created by Frank Schulze on 23.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */

public enum SessionControlEventAddress {

    //Global Addresses
    RTC_ADDRESS     ("org.openxsp.rtc.sessioncontrol"),
    SIP_ADDRESS     ("org.openxsp.sip.sessioncontrol"),

    //Module fixes Addresses
    KeepAlive ("KeepAlive");



    private String event;
    SessionControlEventAddress(String event) {
        this.event = event;
    }

    public String getEvent() {
        return this.event;
    }

    public static SessionControlEventAddress validEvent(String event) {
        if (event != null) {
            for (SessionControlEventAddress i : SessionControlEventAddress.values()) {
                if (event.compareTo(i.event) == 0)
                    return i;
            }
        }
        return null;
    }

}
