package de.fhg.fokus.ims.core;

import javax.ims.core.ServiceMethod;

/**
 * Interface for notifying references about responses.
 *  
 * @author Andreas Bachmann <andreas.bachmann@fokus.fraunhofer.de>
 *
 */
public interface ReferenceInformationListener
{
	int STATE_REFERENCE_ACTIVE = 1;
	
	int STATE_REFERENCE_TERMINATED = 2;

	void referenceNotification(ServiceMethod serviceMethod, int state, int expires, String notification);
}
