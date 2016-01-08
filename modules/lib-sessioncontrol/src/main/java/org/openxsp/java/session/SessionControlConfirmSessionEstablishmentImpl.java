package org.openxsp.java.session;

import org.vertx.java.core.json.JsonObject;

/**
 * Created by Frank Schulze on 22.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public class SessionControlConfirmSessionEstablishmentImpl extends SessionControlMessgeImpl implements SessionControlConfirmSessionEstablishment {
    
	private String contentType, content;
	private SessionControlMessageHeader header;

    public SessionControlConfirmSessionEstablishmentImpl(){}

    public SessionControlConfirmSessionEstablishmentImpl(String content, String contentType){
        this.content = content;
        this.contentType = contentType;
    }


	@Override
    public SessionControlMethodEnum getMethod() {
        return super.getMethod();
    }

    @Override
    public void setMethod(SessionControlMethodEnum methodEnum) {
        super.setMethod(methodEnum);
    }

    @Override
    public String getSessionID() {
        return super.getSessionID();
    }

    @Override
    public void setSessionID(String sessionID) {
        super.setSessionID(sessionID);
    }
    
    @Override
	public String getContent() {
		return content;
	}

	@Override
	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
		
	}

	@Override
	public SessionControlMessageHeader getHeader() {
		return header;
	}

	@Override
	public void setHeader(SessionControlMessageHeader header) {
		this.header = header;
	}

    @Override
    public SessionControlMessage parse(JsonObject jsonMessage) {

        if (jsonMessage == null)
            return null;
        this.setMethod(SessionControlMethodEnum.CONFIRM);
        super.setSessionID(jsonMessage.getString(SessionControlHeaderField.sessionID.toLowerCase()));
        super.setAddress(jsonMessage.getString(SessionControlHeaderField.address));
        this.setContent(jsonMessage.getString(SessionControlHeaderField.content.toLowerCase()));
        this.setContentType(jsonMessage.getString(SessionControlHeaderField.contentType.toLowerCase()));
        header = new SessionControlMessageHeader();
        header.parse(jsonMessage.getObject(SessionControlHeaderField.header.toLowerCase()));

        return this;
    }

    @Override
    public org.vertx.java.core.json.JsonObject create() {
        JsonObject message = new JsonObject();

        message.putString(SessionControlHeaderField.method, SessionControlMethodEnum.CONFIRM.toString().toLowerCase());
        if (super.getAddress() == null)
            return null;
        else
            message.putString(SessionControlHeaderField.address, super.getAddress());
        if (super.getSessionID() != null)
            message.putString(SessionControlHeaderField.sessionID, super.getSessionID());
        if (this.content != null)
            message.putString(SessionControlHeaderField.content, this.content);
        if (this.contentType != null)
            message.putString(SessionControlHeaderField.contentType, this.contentType);
        if (this.header != null)
            message.putObject(SessionControlHeaderField.header, header.create());

        return message;
    }

}
