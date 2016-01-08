package org.openxsp.stack;

/**
 * Created by Frank Schulze on 27.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public enum StateEnum {

    NULL    ("null"),
    INVITE  ("invite"),
    RINGING ("ringing"),
    OK      ("ok"),
    CANCEL  ("cancel"),
    ACK     ("ACK"),
    BYE     ("bye"),
    UPDATE  ("update");


    private String value;
    private StateEnum predecessor;

    StateEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public StateEnum getPredecessor() {
        return this.predecessor;
    }

    public void setPredecessor(StateEnum predecessor) {
        this.predecessor = predecessor;
    }
}
