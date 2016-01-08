package de.fhg.fokus.ims;

import javax.ims.Service;

public class ServiceImpl implements Service 
{
	private String appID;
	private String scheme;

	public ServiceImpl(String appID, String scheme)
	{
		this.appID = appID;
		this.scheme = scheme;		
	}
	
	public String getAppId() {
		return appID;
	}

	public String getScheme() {
		return scheme;
	}

	public void close()
	{
		
	}

}
