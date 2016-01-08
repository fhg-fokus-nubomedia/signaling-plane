package org.openxsp.stack;

/**
 * Created by Frank Schulze on 05.05.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public enum EBHeader {

    //EB header and Sip Header
    ApplicationData ("ApplicationData"),
    UserAgent ("User-Agent"),
    CallId ("Call-ID"),
    From ("From"),
    To ("To");

    private String value;

    EBHeader(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
