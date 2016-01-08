package nubomedia.org.openxsp.user_registry.persistence.mongodb;

import org.vertx.java.core.json.JsonObject;

/**
 * Created by fmu on 30/03/15.
 */
public abstract class UserRegistryServerMongoHelper {


    public static final String
            // Interface to UAs
            ADMIN_USR =         "admin_usr",
            ADMIN_PWD =         "admin_pwd",
            USR =               "usr",
            PWD =               "pwd",
            SERVICE_NAME =      "srvcName",
            SERVICE_NAMES =     "srvcNames",
            SERVICE_ADDRESS =   "srvcAddress",
            SERVICE_ADDRESSES = "srvcAddresses",

            // Interface to mongoDB module
            _ID =                            "_id",
            ACTION =                        "action",
                ACTION_DROP_COLLECTION =    "drop_collection",
                ACTION_GET_COLLECTIONS =    "get_collections",
                ACTION_SAVE =               "save",
                ACTION_INSERT =             "insert",
                ACTION_UPDATE =             "update",
                ACTION_DELETE =             "delete",
                ACTION_FIND =               "find",
                ACTION_FINDONE =            "findone",
            COLLECTION =                    "collection",
            DOCUMENT =                      "document",
            CREDENTIALS =                   "credentials",
            MATCHER =                       "matcher",

            // Both Interfaces
            STATUS =                "status",
                STATUS_OK =         "ok",
                STATUS_ERROR =      "error",
            RESULT =                "result",
            MESSAGE =               "message";



    public static JsonObject createDropCollectionCommand(String collectionName){
        if (collectionName == null || collectionName.equals("")) throw new IllegalArgumentException("Cannot process null or empty collectionName");

        return new JsonObject()
                .putString(ACTION, ACTION_DROP_COLLECTION)
                .putString(COLLECTION, collectionName);
    }

    public static JsonObject createGetCollectionsCommand(){
        return new JsonObject().
                putString(ACTION, ACTION_GET_COLLECTIONS);
    }

    public static JsonObject createSaveDocumentInCollection(String collectionName, JsonObject document, JsonObject credentialsDocument) {
        if (collectionName == null || collectionName.equals("")) throw new IllegalArgumentException("Cannot process null or empty collectionName");
        if (document == null || document.size()==0) throw new IllegalArgumentException("Cannot process null or empty document");

        return new JsonObject().
                putString(ACTION, ACTION_SAVE).
                putString(COLLECTION, collectionName).
                putObject(DOCUMENT, document).
                putObject(CREDENTIALS, credentialsDocument);
    }

    public static JsonObject createInsertDocumentInCollection(String collectionName, JsonObject document, JsonObject credentialsDocument) {
        if (collectionName == null || collectionName.equals("")) throw new IllegalArgumentException("Cannot process null or empty collectionName");
        if (document == null || document.size()==0) throw new IllegalArgumentException("Cannot process null or empty document");

        return new JsonObject().
                putString(ACTION, ACTION_INSERT).
                putString(COLLECTION, collectionName).
                putObject(DOCUMENT, document).
                putObject(CREDENTIALS, credentialsDocument);
    }

    public static JsonObject createDeleteDocumentInCollection(String collectionName, JsonObject matcher, JsonObject credentialsDocument) {
        if (collectionName == null || collectionName.equals("")) throw new IllegalArgumentException("Cannot process null or empty collectionName");
        if (matcher == null || matcher.size()==0) throw new IllegalArgumentException("Cannot process null or empty matcher");

        return new JsonObject().
                putString(ACTION, ACTION_DELETE).
                putString(COLLECTION, collectionName).
                putObject(MATCHER, matcher).
                putObject(CREDENTIALS, credentialsDocument);
    }

    public static JsonObject createFindOneDocumentInCollection(String collectionName, JsonObject document) {
        if (collectionName == null || collectionName.equals("")) throw new IllegalArgumentException("Cannot process null or empty collectionName");
        if (document == null || document.size()==0) throw new IllegalArgumentException("Cannot process null or empty document");

        return new JsonObject().
                putString(ACTION, ACTION_FINDONE).
                putString(COLLECTION, collectionName).
                putObject(MATCHER, document);
    }

}
