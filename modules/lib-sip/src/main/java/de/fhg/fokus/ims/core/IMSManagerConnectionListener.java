package de.fhg.fokus.ims.core;

import java.util.EventListener;

public interface IMSManagerConnectionListener extends EventListener {
	void sendingSyncRequest(IMSManagerEvent e);
	void connected(IMSManagerEvent e);
	void disconnected(IMSManagerEvent e);
}
