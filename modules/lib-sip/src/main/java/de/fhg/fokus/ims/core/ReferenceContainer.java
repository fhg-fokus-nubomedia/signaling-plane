package de.fhg.fokus.ims.core;

import java.util.Iterator;

import javax.sip.Dialog;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class ReferenceContainer extends Container
{
	public ReferenceImpl get(Dialog dialog)
	{
		for (Iterator iter = iterator(); iter.hasNext();)
		{
			ReferenceImpl referenceImpl = (ReferenceImpl) iter.next();
			if (referenceImpl.getDialog() == dialog)
			{
				return referenceImpl;
			}
		}
		return null;
	}

	public void dispatch(Request request)
	{
		// TODO Auto-generated method stub

	}

	public void dispatch(Response response, Request request)
	{
		// TODO Auto-generated method stub

	}

	public void timeout(Request request)
	{
		// TODO Auto-generated method stub

	}

}
