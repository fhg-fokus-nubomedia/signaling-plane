package org.openxsp.stack;

/**
 * Created by Frank Schulze on 27.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public class State {

    private StateEnum currentState = StateEnum.NULL;

    public StateEnum setNewState(StateEnum newState) {

        if (currentState == null || newState == null)
            return null;

        switch (currentState) {
            case NULL:
                if (newState == StateEnum.CANCEL || newState == StateEnum.INVITE) {
                    newState.setPredecessor(currentState);
                    this.currentState = newState;
                    return newState;
                }
                System.out.println("Fail to set New State: ["+newState.getValue()+"] in CurrentState: ["+currentState.getValue()+"]");
                return null;
            case INVITE:
                if (newState == StateEnum.CANCEL
                 || newState == StateEnum.OK
                 || newState ==StateEnum.RINGING) {
                    newState.setPredecessor(currentState);
                    this.currentState = newState;
                    return newState;
                }
                System.out.println("Fail to set New State: ["+newState.getValue()+"] in CurrentState: ["+currentState.getValue()+"]");
                return null;
            case RINGING:
                if (newState == StateEnum.CANCEL || newState == StateEnum.OK) {
                    newState.setPredecessor(currentState);
                    this.currentState = newState;
                    return newState;
                }
                System.out.println("Fail to set New State: ["+newState.getValue()+"] in CurrentState: ["+currentState.getValue()+"]");
                return null;
            case OK:
                if (newState == StateEnum.CANCEL || newState == StateEnum.ACK) {
                    newState.setPredecessor(currentState);
                    this.currentState = newState;
                    return newState;
                }
                System.out.println("Fail to set New State: ["+newState.getValue()+"] in CurrentState: ["+currentState.getValue()+"]");
                return null;
            case CANCEL:
                if (newState == StateEnum.NULL) {
                    newState.setPredecessor(currentState);
                    this.currentState = newState;
                    return newState;
                }
                System.out.println("Fail to set New State: ["+newState.getValue()+"] in CurrentState: ["+currentState.getValue()+"]");
                return null;
            case ACK:
                if (newState == StateEnum.CANCEL || newState == StateEnum.BYE) {
                    newState.setPredecessor(currentState);
                    this.currentState = newState;
                    return newState;
                }
                System.out.println("Fail to set New State: ["+newState.getValue()+"] in CurrentState: ["+currentState.getValue()+"]");
                return null;
            case BYE:
                if (newState == StateEnum.CANCEL || newState == StateEnum.OK) {
                    newState.setPredecessor(currentState);
                    this.currentState = newState;
                    return newState;
                }
                System.out.println("Fail to set New State: ["+newState.getValue()+"] in CurrentState: ["+currentState.getValue()+"]");
                return null;
            case UPDATE:
                if (newState == StateEnum.CANCEL || newState == StateEnum.OK) {
                    newState.setPredecessor(currentState);
                    this.currentState = newState;
                    return newState;
                }
                System.out.println("Fail to set New State: ["+newState.getValue()+"] in CurrentState: ["+currentState.getValue()+"]");
                return null;
        }
        return null;
    }

    public StateEnum getCurrentState() {
        return this.currentState;
    }

}
