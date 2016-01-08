package org.openxsp.stack;

import javax.sip.*;

/**
 * Created by Frank Schulze on 17.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public class Receiver implements SipListener {


    private SipProvider sipProvider;
    private SessionControl sessionControl;

    public Receiver (SipProvider sipProvider, SessionControl sessionControl) {
        this.sipProvider = sipProvider;
        this.sessionControl = sessionControl;
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        sessionControl.processRequest(sipProvider, sessionControl,requestEvent);
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        sessionControl.processResponse(sipProvider, sessionControl,responseEvent);
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        sessionControl.processTimeout(sipProvider,sessionControl,timeoutEvent);
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        sessionControl.processIOException(sipProvider, sessionControl,exceptionEvent);
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        sessionControl.processTransactionTerminated(sipProvider,sessionControl,transactionTerminatedEvent);
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        sessionControl.processDialogTerminated(sipProvider,sessionControl,dialogTerminatedEvent);
    }
}
