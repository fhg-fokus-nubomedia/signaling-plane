package de.fhg.fokus.ims.core;

import java.util.EventListener;

public interface RegistrationListener  extends EventListener {
	void notifyRegistrationFailed();
	void notifyRegistrationSuccesfull();
}
