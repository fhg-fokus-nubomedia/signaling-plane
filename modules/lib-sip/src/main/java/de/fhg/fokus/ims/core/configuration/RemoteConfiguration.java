package de.fhg.fokus.ims.core.configuration;

import java.util.Vector;

import de.fhg.fokus.ims.core.utils.Utils;

public class RemoteConfiguration 
{

    private Vector icsiIaris = new Vector();
    private String[] eventsarray;
    private Vector appSubtypes = new Vector();
    private Vector featureTags = new Vector();
    private boolean framedSupported = false;
    private boolean audioSupported = false;
    private boolean videoSupported = false;
    private boolean applicationSupported = false;

    /**
     * RemoteCapabilities Constructor.
     * 
     * @param capabilitesArray An array containing the different capabilities
     */
    public RemoteConfiguration(String[] capabilitesArray) {
        String temp;
        int startIndex = 0;

        for (int i = 0; i < capabilitesArray.length; i++) {
            temp = capabilitesArray[i];

            if (temp.toLowerCase().startsWith("+g.3gpp.app_ref")) {
                startIndex = temp.indexOf('=') + 1;
                String[] tokens =
                    Utils.tokenize(
                        trimQuotationMarks(temp.substring(startIndex)), ',');
                for (int j = 0; j < tokens.length; ++j) {
                    icsiIaris.addElement(tokens[j].trim().toLowerCase());
                }
            } else if (temp.equalsIgnoreCase("message")) {
                framedSupported = true;
            } else if (temp.equalsIgnoreCase("audio")) {
                audioSupported = true;
            } else if (temp.equalsIgnoreCase("video")) {
                videoSupported = true;
            } else if (temp.equalsIgnoreCase("application")) {
                applicationSupported = true;
            } else if (temp.toLowerCase().startsWith("app_subtype")) {
                startIndex = temp.indexOf('=') + 1;
                appSubtypes.addElement(trimQuotationMarks(temp.substring(
                    startIndex).toLowerCase()));
            } else if (temp.toLowerCase().startsWith("events")) {
                startIndex = temp.indexOf('=') + 1;
                eventsarray =
                		Utils.tokenize(
                        trimQuotationMarks(temp.substring(startIndex)), ',');
            } else {
                featureTags.addElement(temp.substring(startIndex).toLowerCase());
            }
        }
    }

    /**
     * Checks if a certain icsi or iari is supported by the remote device.
     * 
     * @param icsiIari to be checked
     * @return true if the the icsi/iari is supported
     */
    public boolean isIcsiIariSupported(String icsiIari) {
        return icsiIaris.contains(icsiIari.toLowerCase());
    }

    /**
     * Checks if a certain event is supported by the remote device.
     * 
     * @param event to be checked
     * @return true if the the event is supported
     */
    public boolean isEventSupported(String event) {
        boolean found = false;
        for (int i = 0; i < eventsarray.length; i++) {
            if (eventsarray[i].trim().equalsIgnoreCase(event)) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Checks if a certain application subtype is supported by the remote
     * device.
     * 
     * @param appSubType application subtype to be checked
     * @return true if the the application subtype is supported
     */
    public boolean isAppSubTypeSupported(String appSubType) {
        return appSubtypes.contains(appSubType.toLowerCase());
    }

    /**
     * Checks if a certain FeatureTag is supported by the remote device.
     * 
     * @param featureTag application subtype to be checked
     * @return true if the the featureTag is supported
     */
    public boolean isFeatureTagSupported(String featureTag) {
        return featureTags.contains(featureTag.toLowerCase());
    }

    /**
     * Checks if the remote device supports Framed Media, MSRP.
     * 
     * @return true if the remote device supports Framed Media
     */
    public boolean isFramedMediaSupported() {
        return framedSupported;
    }

    /**
     * Checks if the remote device supports streaming audio or
     * audio content-type.
     * 
     * @return true if audio is supported
     */
    public boolean isAudioSupported() {
        return audioSupported;
    }

    /**
     * Checks if the remote device supports streaming video or
     * video content-type.
     * 
     * @return true if video is supported.
     */
    public boolean isVideoSupported() {
        return videoSupported;
    }

    /**
     * Checks if the remote device supports the application specific content-types.
     * 
     * @return true if application specific content-types is supported.
     */
    public boolean isApplicationSupported() {
        return applicationSupported;
    }

    /**
     * Removes quotation marks from a string.
     * 
     * @param value
     * @return the value without quotationmarks
     */
    private String trimQuotationMarks(String value) {
        String trimmedValue = null;
        if (value.startsWith("\"") && value.endsWith("\"")) {
            trimmedValue = value.substring(1, value.length() - 1);
        }
        return trimmedValue;
    }


}
