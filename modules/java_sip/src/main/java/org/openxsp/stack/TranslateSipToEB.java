package org.openxsp.stack;

import org.openxsp.java.session.*;
import org.vertx.java.core.json.JsonObject;

import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.header.ContentTypeHeader;

/**
 * Created by Frank Schulze on 24.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public class TranslateSipToEB {

    public JsonObject inviteToInit(RequestEvent requestEvent, Session sessionProvider) {
        SessionControlCreateSession init = new SessionControlCreateSessionImpl();
        SessionControlMessageHeader header = new SessionControlMessageHeader();

        init.setAddress(sessionProvider.getLocalAddress());

        init.setFrom(sessionProvider.getFrom().getSipURIAddress().toString());
        init.setTo(sessionProvider.getTo().getSipURIAddress().toString());

        byte[] content = requestEvent.getRequest().getRawContent();
        if (content != null) {
            init.setContent(new String(content));

        } else {
            init.setContent(null);
        }
        ContentTypeHeader type = (ContentTypeHeader) requestEvent.getRequest().getHeader("Content-Type");
        if (type != null) {
            init.setContentType(type.getContentType() + "/" + type.getContentSubType());
        } else {
            init.setContentType(null);
        }

        if (requestEvent.getDialog() != null) {
            if (requestEvent.getDialog().getApplicationData() != null)
                header.addHeader(EBHeader.ApplicationData.getValue(),
                        requestEvent.getDialog().getApplicationData().toString());
        }
        if (requestEvent.getRequest().getHeader(EBHeader.UserAgent.getValue()) != null)
            header.addHeader(EBHeader.UserAgent.getValue(),
                    requestEvent.getRequest().getHeader(EBHeader.UserAgent.getValue()).toString());

        init.setMethod(SessionControlMethodEnum.INIT);
        init.setSessionID(sessionProvider.getEBSessionID());
        return init.create();
    }

    public JsonObject byeToEnd(RequestEvent requestEvent, Session sessionProvider) {
        SessionControlEndSession end = new SessionControlEndSessionImpl();
        end.setAddress(SessionControlEventAddress.SIP_ADDRESS.getEvent()+sessionProvider.getEBSessionID());
        end.setSessionID(sessionProvider.getEBSessionID());
        return end.create();
    }

    public JsonObject cancelToCancel(RequestEvent requestEvent, Session sessionProvider) {
        SessionControlCancelSession cancel = new SessionControlCancelSessionImpl();
        cancel.setAddress(SessionControlEventAddress.SIP_ADDRESS.getEvent()+sessionProvider.getEBSessionID());
        cancel.setSessionID(sessionProvider.getEBSessionID());
        return cancel.create();
    }

    public JsonObject cancelToCancel(Session sessionProvider) {
        SessionControlCancelSession cancel = new SessionControlCancelSessionImpl();
        cancel.setAddress(SessionControlEventAddress.SIP_ADDRESS.getEvent()+sessionProvider.getEBSessionID());
        cancel.setSessionID(sessionProvider.getEBSessionID());
        return cancel.create();
    }

    public JsonObject updateToUpdate(RequestEvent requestEvent, Session sessionProvider) {
        SessionControlUpdateSession update = new SessionControlUpdateSessionImpl();
        update.setSessionID(sessionProvider.getEBSessionID());
        update.setAddress(SessionControlEventAddress.SIP_ADDRESS.getEvent()+sessionProvider.getEBSessionID());
        Object content = requestEvent.getRequest().getContent();
        if (content != null) {
            update.setContent(requestEvent.getRequest().getContent().toString());
        } else {
            update.setContent(null);
        }

        update.setContentType(requestEvent.getRequest().getContentDisposition().getDispositionType());
        return update.create();
    }

    public JsonObject ackToConfirm(RequestEvent requestEvent, Session sessionProvider) {
        SessionControlConfirmSessionEstablishment ack = new SessionControlConfirmSessionEstablishmentImpl();
        SessionControlMessageHeader header = new SessionControlMessageHeader();
        ack.setSessionID(sessionProvider.getEBSessionID());
        ack.setAddress(SessionControlEventAddress.SIP_ADDRESS.getEvent()+sessionProvider.getEBSessionID());

        byte[] content = requestEvent.getRequest().getRawContent();
        if (content != null) {
            ack.setContent(new String(content));
        } else {
            ack.setContent(null);
        }

        ContentTypeHeader type = (ContentTypeHeader) requestEvent.getRequest().getHeader("Content-Type");
        if (type != null) {
            ack.setContentType(type.getContentType());
        } else {
            ack.setContentType(null);
        }

        if (requestEvent.getDialog() != null) {
            if (requestEvent.getDialog().getApplicationData() != null)
                header.addHeader(EBHeader.ApplicationData.getValue(),
                        requestEvent.getDialog().getApplicationData().toString());
        }
        if (requestEvent.getRequest().getHeader(EBHeader.UserAgent.getValue()) != null)
            header.addHeader(EBHeader.UserAgent.getValue(),
                    requestEvent.getRequest().getHeader(EBHeader.UserAgent.getValue()).toString());

        ack.setHeader(header);
        return ack.create();
    }

    public JsonObject ackToConfirm(ResponseEvent requesresponseEventEvent, Session sessionProvider) {
        SessionControlConfirmSessionEstablishment confirm = new SessionControlConfirmSessionEstablishmentImpl();
        SessionControlMessageHeader header = new SessionControlMessageHeader();
        confirm.setAddress(SessionControlEventAddress.SIP_ADDRESS.getEvent() + sessionProvider.getEBSessionID());
        confirm.setSessionID(sessionProvider.getEBSessionID());

        if (requesresponseEventEvent.getResponse().getContent() != null) {
            confirm.setContent(requesresponseEventEvent.getResponse().getContent().toString());
        }
        if (requesresponseEventEvent.getResponse().getContentDisposition() != null) {
            if (requesresponseEventEvent.getResponse().getContentDisposition().getDispositionType() != null)
                confirm.setContentType(requesresponseEventEvent.getResponse().getContentDisposition().getDispositionType());
        }
        if (requesresponseEventEvent.getDialog().getApplicationData() != null) {
            header.addHeader(EBHeader.ApplicationData.getValue(), requesresponseEventEvent.getDialog().getApplicationData().toString());
        }

        if (requesresponseEventEvent.getResponse().getHeader(EBHeader.UserAgent.getValue()) != null) {
            header.addHeader(EBHeader.UserAgent.getValue(),
                    requesresponseEventEvent.getResponse().getHeader(EBHeader.UserAgent.getValue()).toString());
        }
        confirm.setHeader(header);
        return confirm.create();
    }

    public JsonObject okToAccept(ResponseEvent responseEvent,  Session sessionProvider) {
        SessionControlAcceptSession accept = new SessionControlAcceptSessionImpl();

        accept.setAddress(sessionProvider.getLocalAddress());
        accept.setSessionID(sessionProvider.getEBSessionID());
        return accept.create();
    }

}
