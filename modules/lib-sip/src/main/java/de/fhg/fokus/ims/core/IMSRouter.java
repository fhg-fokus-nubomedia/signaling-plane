/*
 * Copyright (C) 2007 FhG FOKUS, Institute for Open Communication Systems
 *
 * This file is part of OpenIC - an IMS user endpoint implementation
 * 
 * OpenIC is proprietary software that is licensed 
 * under the FhG FOKUS "SOURCE CODE LICENSE for FOKUS IMS COMPONENTS".
 * You should have received a copy of the license along with this 
 * program; if not, write to Fraunhofer Institute FOKUS, Kaiserin-
 * Augusta Allee 31, 10589 Berlin, GERMANY 
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * It has to be noted that this software is not intended to become 
 * or act as a product in a commercial context! It is a PROTOTYPE
 * IMPLEMENTATION for IMS technology testing and IMS application 
 * development for research purposes, typically performed in IMS 
 * test-beds. See the attached license for more details. 
 *
 * For a license to use this software under conditions
 * other than those described here, please contact Fraunhofer FOKUS 
 * via e-mail at the following address:
 * 
 *    info@open-ims.org
 *
 */
package de.fhg.fokus.ims.core;

import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.stack.HopImpl;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.sip.SipException;
import javax.sip.address.Hop;
import javax.sip.address.Router;
import javax.sip.message.Request;

/** 
 * Next hop to which the request will be sent to
 * @author FhG FOKUS <a href="mailto:openic@open-ims.org">openic@open-ims.org</a>
 */
public class IMSRouter 
	implements Router
{

	Hop pcscf;
	
	public IMSRouter(javax.sip.SipStack sipStack,String outboundProxy)
	{
		pcscf = (Hop) ((SipStackImpl)sipStack).getAddressResolver().resolveAddress(
				(Hop)(new HopImpl(outboundProxy)));
	}
	
	public Hop getOutboundProxy() {
		return pcscf;
	}

	public ListIterator getNextHops(Request request) {
		LinkedList list = new LinkedList();
		list.add(pcscf);
		return list.listIterator();
	}

	public Hop getNextHop(Request request) throws SipException {
		return pcscf;
	}

}
