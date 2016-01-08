package de.fhg.fokus.ims.core;

import javax.sip.message.Request;
import javax.sip.message.Response;

public interface IMSManagerListener
{
	void request(Request request);
	
	void provisionalResponse(Response response);
	
	void finalResponse(Response response);

}
