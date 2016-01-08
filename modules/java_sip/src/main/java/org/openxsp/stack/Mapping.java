package org.openxsp.stack;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Frank Schulze on 26.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */
public class Mapping {

    // EB session Id -> SIP CallId
    private ConcurrentHashMap<String, String> EBSessionIdCallIdMap;
    
    //call id -> session
    private ConcurrentHashMap<String, Session> callIdSessionMap;

    public Mapping() {
        this.EBSessionIdCallIdMap = new ConcurrentHashMap<String, String>();
        this.callIdSessionMap = new ConcurrentHashMap<String, Session>();
    }

    public boolean addSession(Session session) {
        if (session.getEBSessionID() == null || session.getCallID() == null) {
        	System.out.println("SipStack Failed to save Session EB: [" +session.getEBSessionID() + "] CallId: [" + session.getCallID() +"]");
            return false;
        }

        this.EBSessionIdCallIdMap.put(session.getEBSessionID(), session.getCallID());
        this.callIdSessionMap.put(session.getCallID(), session);
        
        System.out.println("SipStack Session EB: [" +session.getEBSessionID() + "] CallId: [" + session.getCallID() +"]");

        return true;
    }

    public Session getSessionByEBSessionId(String EBSessionID) {
        String callId;
        if (EBSessionID == null)
            return null;
        if ((callId = this.EBSessionIdCallIdMap.get(EBSessionID)) == null) {
            return null;
        }
        return this.callIdSessionMap.get(callId);
    }

    public Session getSessionByCallId(String callId) {
        if (callId == null)
            return null;
        return this.callIdSessionMap.get(callId);
    }

    public boolean removeSession(Session session) {
        if (session == null)
            return false;
        if (session.getEBSessionID() == null || session.getCallID() == null) {
            return false;
        }
        if (this.EBSessionIdCallIdMap.remove(session.getEBSessionID()) == null) {
            System.out.println("Error, no EBSessionID found");
            return false;
        }
        if (this.callIdSessionMap.remove(session.getCallID()) == null) {
            System.out.println("Error, no CallId found");
            return false;
        }
        return true;
    }

    public void clearAll() {
        this.callIdSessionMap.clear();
        this.EBSessionIdCallIdMap.clear();
    }

    public void close() {
        this.clearAll();
        try {
            finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
