package org.openxsp.stack;

import javax.sip.SipStack;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;

/**
 * Created by Frank Schulze on 11.04.14.
 * frank.schulze at fokus.fraunhofer.de
 */

public class User {

    //Stack Factorys
    private SipStack sipStack;

    //User Data
    private String name;
    private String sipProvider;
    private String displayName;


    //remote user
    private int peerPort;
    private String peerHost;
    //fprivate boolean isRemoteUser;

    private String headerTag;

    //Sip
    private SipURI sipURIAddress;
    private FromHeader fromHeader;
    private ToHeader toHeader;
    private ContactHeader contactHeader;

    /*
    * Constructor
     */
    /*public User(SipStack sipStack) throws Exception {
        if (sipStack == null)
            throw new Exception("empty SipStack");
        else {
            this.sipStack = sipStack;
            isRemoteUser = false;
        }
    }*/

    public User(SipStack sipStack, String host, int port) throws Exception{
        if (host == null) {
            throw new Exception("empty Host");
        } else  if (port <=  0) {
            throw new Exception("empty Port, and"+ port +" is not a valid port");
        } else if (sipStack == null) {
            throw new Exception("empty SipStack");
        } else {
            this.peerHost = host;
            this.peerPort = port;
            this.sipStack = sipStack;
           // isRemoteUser = true;
        }
    }

    /*
    * Getter and Setter
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSipProvider() {
        return sipProvider;
    }

    public void setSipProvider(String sipProvider) {
        this.sipProvider = sipProvider;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getHeaderTag() {
        return headerTag;
    }

    public void setHeaderTag(String headerTag) {
        this.headerTag = headerTag;
    }

    public FromHeader getFromHeader() {
        return fromHeader;
    }

    public void resetFromHeader() {
        fromHeader = null;
    }

    public ToHeader getToHeader() {
        return toHeader;
    }

    public void resetToHeader() {
        toHeader = null;
    }

    public SipStack getSipStack() {
        return sipStack;
    }

    public String getPeerHostPort() {
       return peerHost + ":" + peerPort;
   }

    public SipURI getSipURIAddress() {
        if (sipURIAddress == null) {
            try {
                createSipURI();
            } catch (Exception e) {
                return null;
            }
        }
        return sipURIAddress;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public void setPeerPort(int peerPort) {
        this.peerPort = peerPort;
    }

    public String getPeerHost() {
        return peerHost;
    }

    public void setPeerHost(String peerHost) {
        this.peerHost = peerHost;
    }

    public ContactHeader getContactHeader() {
		return contactHeader;
	}

	public void setContactHeader(ContactHeader contactHeader) {
		this.contactHeader = contactHeader;
	}

    /*
    * private functions
     */
    private void createSipURI()
    throws Exception {
        AddressFactory x = sipStack.getAddressFactory();
        sipURIAddress = x.createSipURI(name, sipProvider);

    }

    /*
     * public functions
     */
    public FromHeader createFromHeader() {
        if (fromHeader != null)
            return fromHeader;
        try {
            //create own(from) SipURI
           if (sipURIAddress == null) {
               createSipURI();
           }
            //create Name Address
            Address fromNameAddress = sipStack.getAddressFactory().createAddress(sipURIAddress);
            fromNameAddress.setDisplayName(displayName);
            fromHeader = sipStack.getHeaderFactory().createFromHeader(fromNameAddress, headerTag);

        } catch (Exception ex) {
            System.out.println("Exception in createFromHeader : " +ex.toString());
            ex.printStackTrace();
        }
        return fromHeader;
    }

    public ToHeader createToHeader() {
        if (toHeader != null)
            return toHeader;
        try {
            if (sipURIAddress == null) {
                createSipURI();
            }
            Address toNameAddress = sipStack.getAddressFactory().createAddress(sipURIAddress);
            toNameAddress.setDisplayName(displayName);
            //create name Address
            toHeader = sipStack.getHeaderFactory().createToHeader(toNameAddress,null);

        } catch (Exception ex) {
            System.out.println("Exception in createToHeader : " + ex.toString());
            ex.printStackTrace();
        }
        return toHeader;
    }

	
}
