package de.fhg.fokus.ims.core;

import gov.nist.core.CommonLogger;
import gov.nist.core.StackLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.ims.core.ServiceMethod;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

public abstract class Container
{	
	private static Logger LOGGER = LoggerFactory.getLogger(Container.class);

	private HashMap<Object, ServiceMethodImpl> methods = new HashMap<Object, ServiceMethodImpl>();

	public void put(ServiceMethodImpl serviceMethod)
	{
		Object key = getKey(serviceMethod);
		this.methods.put(key, serviceMethod);
		LOGGER.debug("Value added: {"+key+"}={"+serviceMethod+"}, remaining objects: {"+ new Integer(methods.size())+"}");
	}

	public int size()
	{
		return methods.size();
	}

	public ServiceMethodImpl remove(ServiceMethodImpl serviceMethod)
	{
		Object key = getKey(serviceMethod);
		ServiceMethodImpl result = (ServiceMethodImpl) this.methods.remove(key);
		LOGGER.debug("Value removed: {"+key+"}={"+result+"}, remaining objects: {"+ new Integer(methods.size())+"}");
		return result;
	}

	public ServiceMethodImpl remove(Object key)
	{
		ServiceMethodImpl result = (ServiceMethodImpl) this.methods.remove(key);
		LOGGER.debug("Value removed: {"+key+"}={"+result+"}, remaining objects: {"+ new Integer(methods.size())+"}");
		return result;
	}

	public Collection<ServiceMethodImpl> getMethods()
	{
		return methods.values();
	}

	protected Object getKey(ServiceMethodImpl serviceMethod)
	{
		return serviceMethod;
	}

	protected ServiceMethod getMethod(Object key)
	{
		return (ServiceMethod) methods.get(key);
	}

	protected Iterator<ServiceMethodImpl> iterator()
	{
		return methods.values().iterator();
	}

	public abstract void dispatch(Request request);

	public abstract void dispatch(Response response, Request request);

	public abstract void timeout(Request request);
}
