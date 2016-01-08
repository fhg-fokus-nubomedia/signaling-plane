package javax.ims;

import java.io.IOException;

import de.fhg.fokus.microedition.io.Connection;



public interface Service extends Connection
{

    /**
     * Returns the application id string that this Service was created with.
     * 
     * @return the application id of this Service
     */
    public abstract String getAppId();   
    
    /**
     * Returns the scheme used for this Service.
     * 
     * @return the scheme used for this Service
     */
    public String getScheme();
}
