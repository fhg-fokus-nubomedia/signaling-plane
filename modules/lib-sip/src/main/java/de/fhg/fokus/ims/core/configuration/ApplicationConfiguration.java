package de.fhg.fokus.ims.core.configuration;



/**
 * 
 * @author Alice Motanga
 * /**
 * 
 * Contains a configuration for an IMS application.
 * <p>
 * The configuration is also known as the registry. The registry contain
 * information about what the application is capable of doing and is normally
 * set at installation time. This is explained in detail in the JSR281
 * documentation of the {@link javax.microedition.ims.Configuration} class.
 * <p>
 * 
 */
public class ApplicationConfiguration 
{/*
	
	*//** Identifies the session sector of the capabilities config. *//*
    public static final int CAPABILITIES_SDP_SECTOR_SESSION = 0;
    *//** Identifies the framed media sector of the capabilities config. *//*
    public static final int CAPABILITIES_SDP_SECTOR_FRAMED = 1;
    *//** Identifies the stream audio media sector of the capabilities config. *//*
    public static final int CAPABILITIES_SDP_SECTOR_STREAM_AUDIO = 2;
    *//** Identifies the stream video media of the capabilities config. *//*
    public static final int CAPABILITIES_SDP_SECTOR_STREAM_VIDEO = 3;

    *//** Identifies the request part of the capabilities config. *//*
    public static final int CAPABILITIES_SDP_MESSAGE_TYPE_REQUEST = 0;
    *//** Identifies the response part of the capabilities config. *//*
    public static final int CAPABILITIES_SDP_MESSAGE_TYPE_RESPONSE = 1;

    private String className;
    private boolean streamAudioSupported = false;
    private boolean streamVideoSupported = false;
    private boolean framedMediaSupported = false;
    private String[] framedMediaMimeTypes = new String[0];
    private boolean basicMediaSupported = false;
    private String[] basicMediaMimeTypes = new String[0];
    private String[] eventPackages = new String[0];
    private String[] readHeaders = new String[0];
    private String[] writeHeaders = new String[0];
    private Hashtable capabilitiesConfiguration = new Hashtable();

    
    *//**
     * Hash table of core service configurations. The key is the core service ID
     * (a <code>String</code>). The value is a {@link CoreServiceConfig}.
     *//*
    private Hashtable coreServiceConfigs = new Hashtable();

    *//**
     * Creates an empty IMS application configuration tied to a given class.
     * 
     * @param className The class name.
     *//*
    public ApplicationConfiguration(String className) {
        this.className = className;
    }

    *//**
     * Creates a copy of a <code>AppConfig</code> object.
     * 
     * @param original the <code>AppConfig</code> object to copy
     *//*
    public ApplicationConfiguration(ApplicationConfiguration original) {
        className = original.className;
        streamAudioSupported = original.streamAudioSupported;
        streamVideoSupported = original.streamVideoSupported;
        framedMediaSupported = original.framedMediaSupported;
        framedMediaMimeTypes = CopyUtil.copy(original.framedMediaMimeTypes);
        basicMediaSupported = original.basicMediaSupported;
        basicMediaMimeTypes = CopyUtil.copy(original.basicMediaMimeTypes);
        eventPackages = CopyUtil.copy(original.eventPackages);
        readHeaders = CopyUtil.copy(original.readHeaders);
        writeHeaders = CopyUtil.copy(original.writeHeaders);

        capabilitiesConfiguration =
            new Hashtable(original.capabilitiesConfiguration.size());
        Enumeration keys = original.capabilitiesConfiguration.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Vector vector = (Vector)original.capabilitiesConfiguration.get(key);
            capabilitiesConfiguration.put(key, CopyUtil.copy(vector));
        }

        coreServiceConfigs = new Hashtable(original.coreServiceConfigs.size());
        keys = original.coreServiceConfigs.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            CoreServiceConfig config =
                (CoreServiceConfig)original.coreServiceConfigs.get(key);
            coreServiceConfigs.put(key, new CoreServiceConfig(config));
        }
    }

    *//**
     * Returns the class name this configuration is tied to.
     * 
     * @return the class name
     *//*
    public String getClassName() {
        return className;
    }

    *//**
     * Sets if streamed media is supported.
     * 
     * @param audioSupported <code>true</code> if streamed audio is supported.
     * @param videoSupported <code>true</code> if streamed video is supported.
     *//*
    public void setStreamMediaSupported(boolean audioSupported,
        boolean videoSupported) {
        streamAudioSupported = audioSupported;
        streamVideoSupported = videoSupported;
    }

    *//**
     * Returns <code>true</code> if streamed audio is supported.
     * 
     * @return <code>true</code> if streamed audio is supported.
     *//*
    public boolean isStreamMediaAudioSupported() {
        return streamAudioSupported;
    }

    *//**
     * Returns <code>true</code> if streamed video is supported.
     * 
     * @return <code>true</code> if streamed video is supported.
     *//*
    public boolean isStreamMediaVideoSupported() {
        return streamVideoSupported;
    }

    *//**
     * Sets if framed media is supported.
     * 
     * @param framedMediaSupported <code>true</code> if framed media is
     *        supported.
     *//*
    public void setFramedMediaSupported(boolean framedMediaSupported) {
        this.framedMediaSupported = framedMediaSupported;
    }

    *//**
     * Returns <code>true</code> if framed media is supported.
     * 
     * @return <code>true</code> if framed media is supported.
     *//*
    public boolean isFramedMediaSupported() {
        return framedMediaSupported;
    }

    *//**
     * Sets the MIME types supported by framed media.
     * 
     * @param mimeTypes the mime types
     *//*
    public void setFramedMediaMimeTypes(String[] mimeTypes) {
        framedMediaMimeTypes = CopyUtil.copy(mimeTypes);
    }

    *//**
     * Gets the MIME types supported by framed media.
     * 
     * @return the mime types
     *//*
    public String[] getFramedMediaMimeTypes() {
        return CopyUtil.copy(framedMediaMimeTypes);
    }

    *//**
     * Sets if basic media is supported.
     * 
     * @param basicMediaSupported <code>true</code> if basic media is
     *        supported.
     *//*
    public void setBasicMediaSupported(boolean basicMediaSupported) {
        this.basicMediaSupported = basicMediaSupported;
    }

    *//**
     * Returns <code>true</code> if basic media is supported.
     * 
     * @return <code>true</code> if basic media is supported.
     *//*
    public boolean isBasicMediaSupported() {
        return basicMediaSupported;
    }

    *//**
     * Sets the MIME types supported by basic media.
     * 
     * @param mimeTypes the mime types
     *//*
    public void setBasicMediaMimeTypes(String[] mimeTypes) {
        basicMediaMimeTypes = CopyUtil.copy(mimeTypes);
    }

    *//**
     * Gets the MIME types supported by basic media.
     * 
     * @return the mime types
     *//*
    public String[] getBasicMediaMimeTypes() {
        return CopyUtil.copy(basicMediaMimeTypes);
    }

    *//**
     * Sets the supported event packages.
     * 
     * @param eventPackages the event packages
     *//*
    public void setSupportedEventPackages(String[] eventPackages) {
        this.eventPackages = CopyUtil.copy(eventPackages);
    }

    *//**
     * Gets the supported event packages.
     * 
     * @return the event packages
     *//*
    public String[] getSupportedEventPackages() {
        return CopyUtil.copy(eventPackages);
    }

    *//**
     * Returns <code>true</code> if a particular event is supported by the
     * application.
     * 
     * @param event the event
     * @return <code>true</code> if the event is supported
     *//*
    public boolean isEventPackageSupported(String event) {
        return arrayContainsString(eventPackages, event, true);
    }

    *//**
     * Sets the readable headers.
     * 
     * @param readHeaders the readable headers
     *//*
    public void setReadableHeaders(String[] readHeaders) {
        this.readHeaders = CopyUtil.copy(readHeaders);
    }

    *//**
     * Gets the readable headers.
     * 
     * @return the readable headers
     *//*
    public String[] getReadableHeaders() {
        return CopyUtil.copy(readHeaders);
    }

    *//**
     * Returns <code>true</code> if a particular header is readable by the
     * application.
     * 
     * @param header the header
     * @return <code>true</code> if the header is readable
     *//*
    public boolean isHeaderReadable(String header) {
        return arrayContainsString(readHeaders, header, false);
    }

    *//**
     * Sets the writable headers.
     * 
     * @param writeHeaders the writable headers
     *//*
    public void setWritableHeaders(String[] writeHeaders) {
        this.writeHeaders = CopyUtil.copy(writeHeaders);
    }

    *//**
     * Gets the writable headers.
     * 
     * @return the writable headers
     *//*
    public String[] getWritableHeaders() {
        return CopyUtil.copy(writeHeaders);
    }

    *//**
     * Returns <code>true</code> if a particular header is writable by the
     * application.
     * 
     * @param header the header
     * @return <code>true</code> if the header is writable
     *//*
    public boolean isHeaderWritable(String header) {
        return arrayContainsString(writeHeaders, header, false);
    }

    *//**
     * Gets the capabilities configuration, the SDP headers to include in a
     * particular sector of the capabilities SDP for either request or response.
     * 
     * @param sector a constant identifying the SDP sector
     * @param messageType a constant identifying the message type
     * @return the SDP headers
     *//*
    public String[] getCapabilityHeaders(int sector, int messageType) {
        Object key = createCapabilityConfigTableKey(sector, messageType);
        if (capabilitiesConfiguration.containsKey(key)) {
            Vector headers = (Vector)capabilitiesConfiguration.get(key);
            String[] result = new String[headers.size()];
            headers.copyInto(result);
            return result;
        }
        return new String[0];
    }

    *//**
     * Modifies the capabilities configuration, adding an SDP header to a
     * particular SDP sector for either capabilities requests or responses.
     * 
     * @param sector a constant identifying the SDP sector
     * @param messageType a constant identifying the message type
     * @param header the SDP header to add
     *//*
    public void addCapabilityHeader(int sector, int messageType, String header) {
        Object key = createCapabilityConfigTableKey(sector, messageType);
        Vector headers;
        if (capabilitiesConfiguration.containsKey(key)) {
            headers = (Vector)capabilitiesConfiguration.get(key);
        } else {
            headers = new Vector();
            capabilitiesConfiguration.put(key, headers);
        }
        headers.addElement(header);
    }

    private Object createCapabilityConfigTableKey(int sector, int messageType) {
        return new Integer(sector + (messageType << 4));
    }

    *//**
     * Returns a {@link CoreServiceConfig} for a particular service ID.
     * 
     * @param serviceId the service ID
     * @return The service configuration, or <code>null</code> if the service
     *         is not configured.
     *//*
    public CoreServiceConfig getCoreServiceConfig(String serviceId) {
        return (CoreServiceConfig)coreServiceConfigs.get(serviceId);
    }

    *//**
     * Returns the IDs of all registered core services.
     * 
     * @return the IDs of all registered core services
     *//*
    public Vector getCoreServiceIds() {
        Vector result = new Vector();
        Enumeration ids = coreServiceConfigs.keys();
        while (ids.hasMoreElements()) {
            result.addElement(ids.nextElement());
        }
        return result;
    }

    *//**
     * Registers a core service.
     * <p>
     * Use {@link #getRegisteredMediaCapabilityProperties()} for media
     * capability properties and {@link #registerProperty(String, String)} for
     * other types of properties.
     * <p>
     * 
     * @param serviceId the core service ID
     * @param config the core service configuration
     *//*
    public void registerCoreServiceConfig(String serviceId,
        CoreServiceConfig config) {
        coreServiceConfigs.put(serviceId, config);
    }

    private boolean arrayContainsString(String[] array, String string,
        boolean caseSensitive) {
        for (int i = 0; i < array.length; ++i) {
            boolean equal;
            if (caseSensitive) {
                equal = array[i].equals(string);
            } else {
                equal = array[i].equalsIgnoreCase(string);
            }
            if (equal) {
                return true;
            }
        }
        return false;
    }
*/}
