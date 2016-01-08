package de.fhg.fokus.ims.core;

import org.vertx.java.core.json.JsonObject;




public class IMSProfile
{
	
	//private UserProfile userProfile;

	private JsonObject userProfile;
	
	public IMSProfile(JsonObject configuration)
	{
		this.userProfile = configuration;
	}

	public String getID()
	{
		return userProfile.getString("id");
	}
	
	public String getDomain()
	{		
		return  userProfile.getString("imsDomain");
	}

	public String getDisplayName()
	{
		return  userProfile.getString("imsDisplayName");
	}

	public String getPublicId()
	{
		return  userProfile.getString("imsPublicId");
	}

	public String getPrivateId()
	{
		return  userProfile.getString("imsPrivateId");
	}

	public String getSecretKey()
	{
		return  userProfile.getString("imsSecretKey");
	}

	public String getPCSCFAddress()
	{
		return  userProfile.getString("imsPCSCFAddress");
	}

	public String getPCSCFPort()
	{
		return  userProfile.getString("imsLocalPort");
	}

	public String getLocalInterface()
	{
		return  userProfile.getString("imsLocalInterface");
	}

	public String getLocalPort()
	{
		return  userProfile.getString("imsLocalPort");
	}

	public boolean getRegister()
	{
		return Boolean.valueOf(userProfile.getString("imsRegister"));
	}

	public String getRegisterTime()
	{
		return  userProfile.getString("imsRegisterTime");
	}

	public boolean getEnableLog()
	{
		return Boolean.valueOf(userProfile.getString("imsEnableLog"));
	}

	public boolean isSessionTimerEnabled()
	{
		return Boolean.valueOf(userProfile.getString("enableSessionTimer"));
	}
	
	public boolean isPrackSupportEnabled()
	{
		return Boolean.valueOf(userProfile.getString("imsPrack"));
	}
	

	public String getSessionExpireTimer()
	{
		return  userProfile.getString("sessionExpireTimer");
	}

	public String getPCSCFDiscovery()
	{
		return  userProfile.getString("imsPCSCFDiscoverFunction");
	}

	public boolean getSubscribeToReg()
	{
		return Boolean.valueOf(userProfile.getString("imsSubscribeToReg"));
	}
}