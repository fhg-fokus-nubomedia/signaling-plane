package nubomedia.org.openxsp.user_registry.util;

import org.vertx.java.core.json.JsonObject;

/**
 * Created by fmu on 30/03/15.
 */
public abstract class UserRegistryClientHelper {

    public static final String
            // Interface to User Registry for REQUESTs
            ADMIN_USR =         "admin_usr",
            ADMIN_PWD =         "admin_pwd",
            USR =               "usr",
            PWD =               "pwd",
            SERVICE_NAME =      "srvcName",
            SERVICE_NAMES =     "srvcNames",
            SERVICE_ADDRESS =   "srvcAddress",
            SERVICE_ADDRESSES = "srvcAddresses",

            // Interface to User Registry for RESPONSEs
            STATUS =            "status",
            STATUS_OK =         "ok",
            STATUS_ERROR =      "error",
            RESULT =            "result",
            MESSAGE =           "message";


    public static JsonObject createRegisterRequest(String usr, String pwd, String srvcName, String srvcAddress) {
        if (usr == null || usr.equals("")) throw new IllegalArgumentException("Cannot process null or empty usr");
        if (pwd == null || pwd.equals("")) throw new IllegalArgumentException("Cannot process null or empty pwd");
        if (srvcName == null || srvcName.equals("")) throw new IllegalArgumentException("Cannot process null or empty srvcName");
        if (srvcAddress == null || srvcAddress.equals("")) throw new IllegalArgumentException("Cannot process null or empty srvcAddress");

        return new JsonObject().
                putString(USR, usr).
                putString(PWD, pwd).
                putString(SERVICE_NAME, srvcName).
                putString(SERVICE_ADDRESS, srvcAddress);
    }

    public static JsonObject createUnregisterRequest(String user, String pswd, String srvcName, String srvcAddress) {
        return createRegisterRequest(user, pswd, srvcName, srvcAddress);
    }

    public static JsonObject createGetAddressRequest(String user, String srvcName) {
        if (user == null || user.equals("")) throw new IllegalArgumentException("Cannot process null or empty user");
        if (srvcName == null || srvcName.equals("")) throw new IllegalArgumentException("Cannot process null or empty srvcName");

        return new JsonObject().
                putString(USR, user).
                putString(SERVICE_NAME, srvcName);
    }

    public static JsonObject createGetServicesRequest(String user) {
        if (user == null || user.equals("")) throw new IllegalArgumentException("Cannot process null or empty user");

        return new JsonObject().
                putString(USR, user);
    }

    public static JsonObject createAddRequest(String adminUsr, String adminPwd, String usr, String pwd) {
        if (adminUsr == null || adminUsr.equals("")) throw new IllegalArgumentException("Cannot process null or empty adminUsr");
        if (adminPwd == null || adminPwd.equals("")) throw new IllegalArgumentException("Cannot process null or empty adminPwd");
        if (usr == null || usr.equals("")) throw new IllegalArgumentException("Cannot process null or empty usr");
        if (pwd == null || pwd.equals("")) throw new IllegalArgumentException("Cannot process null or empty pwd");

        return new JsonObject().
                putString(ADMIN_USR, adminUsr).
                putString(ADMIN_PWD, adminPwd).
                putString(USR, usr).
                putString(PWD, pwd);
    }

    public static JsonObject createRemoveRequest(String adminUsr, String adminPwd, String usr, String pwd) {
        if (adminUsr == null || adminUsr.equals("")) throw new IllegalArgumentException("Cannot process null or empty adminUsr");
        if (adminPwd == null || adminPwd.equals("")) throw new IllegalArgumentException("Cannot process null or empty adminPwd");
        if (usr == null || usr.equals("")) throw new IllegalArgumentException("Cannot process null or empty usr");
        if (pwd == null || pwd.equals("")) throw new IllegalArgumentException("Cannot process null or empty pwd");

        return new JsonObject().
                putString(ADMIN_USR, adminUsr).
                putString(ADMIN_PWD, adminPwd).
                putString(USR, usr).
                putString(PWD, pwd);
    }
}
