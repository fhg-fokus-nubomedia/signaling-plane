package org.openxsp.stack;

import javax.sip.*;

/**
 * Created by Frank Schulze on 17.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public interface Listener {

    public void processRequest(SipProvider sipProvider,
                              SessionControl sessionControl, RequestEvent requestEvent);

    public void processResponse(SipProvider sipProvider,
                               SessionControl sessionControl, ResponseEvent responseEvent);

    public void processTimeout(SipProvider sipProvider,
                              SessionControl sessionControl, TimeoutEvent timeoutEvent);

    public void processIOException(SipProvider sipProvider,
                                  SessionControl sessionControl, IOExceptionEvent exceptionEvent);

    public void processTransactionTerminated(SipProvider sipProvider,
                                             SessionControl sessionControl, TransactionTerminatedEvent transactionTerminatedEvent);

    public void processDialogTerminated(SipProvider sipProvider,
                                       SessionControl sessionControl, DialogTerminatedEvent dialogTerminatedEvent);

}
