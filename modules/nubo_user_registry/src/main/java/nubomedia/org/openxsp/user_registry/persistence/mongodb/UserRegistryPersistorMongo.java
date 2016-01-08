package nubomedia.org.openxsp.user_registry.persistence.mongodb;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
import org.openxsp.java.EventBus;
import nubomedia.org.openxsp.user_registry.log.Logger;
import nubomedia.org.openxsp.user_registry.log.LoggerFactory;
import nubomedia.org.openxsp.user_registry.main.UserRegistry;
import nubomedia.org.openxsp.user_registry.persistence.UserRegistryPersistor;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import java.util.Collection;


/**
 * Created by fmu on 26/03/15.
 */
public class UserRegistryPersistorMongo implements UserRegistryPersistor {

    private static String HZ_INSTANCE_NAME = "UserRegistryHCInstance";
    private static String DATABASE_USERS = "default_db";
    private static String COLLECTION_USERS = "Users";

    private Logger logger = LoggerFactory.getLogger("UserRegistryPersistorMongo");

    private Container container;
    private EventBus eb;
    private String mongoModuleAddress;

    private HazelcastInstance hc;
    private MultiMap<String,String> userServices;
    private MultiMap<String,String> userServiceAddresses;

    public UserRegistryPersistorMongo(Container container, EventBus eb, JsonObject config) throws InstantiationException {
        this.container = container;
        this.eb = eb;

        initialize(config);
    }

    private void initialize(JsonObject config) throws InstantiationException {
        logger.setSubTag("initialize");
        mongoModuleAddress = config.getString(UserRegistry.PERSISTENCE_MODULE_ADDRESS);

        // Registers the handlers for the User Registry operations
        registerUserRegistryOperationHandlers(config);

        Config hzConfig = new Config();

        /*MapConfig multiMapConfig = new MapConfig();
        multiMapConfig.setName("Client_Map1");
        multiMapConfig.setBackupCount(2);
        multiMapConfig.getMaxSizeConfig().setSize(100000);
        multiMapConfig.setTimeToLiveSeconds(0);
        multiMapConfig.setMaxIdleSeconds(0);
        multiMapConfig.setEvictionPolicy("NONE");
        hzConfig.addMultiMapConfig(multiMapConfig);*/

        hzConfig.setInstanceName(HZ_INSTANCE_NAME);
        hc = Hazelcast.getHazelcastInstanceByName(HZ_INSTANCE_NAME);
        if (hc == null)
            hc = Hazelcast.newHazelcastInstance(hzConfig);
        logger.i(hc.getName());
    }

    private void updateHCMultiMaps() {
        userServices = hc.getMultiMap("UserServices");
        userServiceAddresses = hc.getMultiMap("UserServiceAddresses");
    }

    /*
     *  Permit to register on the event bus the handlers for the user registry operations
     */
    private void registerUserRegistryOperationHandlers(JsonObject config) throws InstantiationException {
        logger.d("Registering operation handlers..");
        JsonObject operationHandlersAddresses = config.getObject(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESSES, null);

        if (operationHandlersAddresses != null) {
            // OPERATION REGISTER_ADDRESS
            String operationAddress = operationHandlersAddresses.getString(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESS_REGISTER_ADDRESS, null);
            if (operationAddress == null) {
                throw new InstantiationException("The 'RegisterAddress' Operation Handler address is missing.");
            }
            logger.i("ADDRESS: " + operationAddress + " - OPERATION: register_address");
            eb.registerHandler(operationAddress, new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> message) {
                    registerUserServiceAddress(message);
                }
            });

            // OPERATION UNREGISTER_ADDRESS
            operationAddress = operationHandlersAddresses.getString(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESS_UNREGISTER_ADDRESS, null);
            if (operationAddress == null) throw new InstantiationException("The 'UnregisterAddress' Operation Handler address is missing.");
            logger.i("ADDRESS: " + operationAddress + " - OPERATION: unregister_address");
            eb.registerHandler(operationAddress, new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> message) {
                    unregisterUserServiceAddress(message);
                }
            });

            // OPERATION GET_ADDRESS
            operationAddress = operationHandlersAddresses.getString(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESS_GET_ADDRESS, null);
            if (operationAddress == null) throw new InstantiationException("The 'GetAddress' Operation Handler address is missing.");
            logger.i("ADDRESS: " + operationAddress + " - OPERATION: get_address");
            eb.registerHandler(operationAddress, new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> message) {
                    getUserServiceAddress(message);
                }
            });

            // OPERATION GET_SERVICES
            operationAddress = operationHandlersAddresses.getString(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESS_GET_SERVICES, null);
            if (operationAddress == null) throw new InstantiationException("The 'GetServices' Operation Handler address is missing.");
            logger.i("ADDRESS: " + operationAddress + " - OPERATION: get_services");
            eb.registerHandler(operationAddress, new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> message) {
                    getUserServices(message);
                }
            });

            // OPERATION ADD_USER
            operationAddress = operationHandlersAddresses.getString(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESS_ADD_USER, null);
            if (operationAddress == null) throw new InstantiationException("The 'AddUser' Operation Handler address is missing.");
            logger.i("ADDRESS: " + operationAddress + " - OPERATION: add_user");
            eb.registerHandler(operationAddress, new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> message) {
                    addUser(message);
                }
            });

            // OPERATION REMOVE_USER
            operationAddress = operationHandlersAddresses.getString(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESS_REMOVE_USER, null);
            if (operationAddress == null) throw new InstantiationException("The 'RemoveUser' Operation Handler address is missing.");
            logger.i("ADDRESS: " + operationAddress + " - OPERATION: remove_user");
            eb.registerHandler(operationAddress, new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> message) {
                    removeUser(message);
                }
            });
        }
        else {
            throw new InstantiationException("The Operation Handler addresses are missing.");
        }
    }


    @Override
    public void registerUserServiceAddress(final Message<JsonObject> msg) {
        logger.setSubTag("registerUserServiceAddress");
        logger.d("Registration Request received.");

        // Check registration parameters
        boolean error = false;
        JsonObject body = msg.body();
        final String usr          = body.getString(UserRegistryServerMongoHelper.USR, null);
        final String pwd          = body.getString(UserRegistryServerMongoHelper.PWD, null);
        final String srvcName     = body.getString(UserRegistryServerMongoHelper.SERVICE_NAME, null);
        final String srvcAddress  = body.getString(UserRegistryServerMongoHelper.SERVICE_ADDRESS, null);
        if (usr == null)            error = true;
        if (pwd == null)           error = true;
        if (srvcName == null)       error = true;
        if (srvcAddress == null)    error = true;
        if (error) {
            JsonObject reply = createErrorReply("Some parameter is missing.");
            msg.reply(reply);
            return;
        }

        // Check the correctness of the user/password information provided by the UserAgent
        JsonObject userDocument = createUserDocument(usr, pwd);
        JsonObject findOneDocumentCmd = UserRegistryServerMongoHelper.createFindOneDocumentInCollection(COLLECTION_USERS, userDocument);
        eb.send(mongoModuleAddress, findOneDocumentCmd, new Handler<AsyncResult<Message<JsonObject>>>() {
            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                if (asyncResultMessage.succeeded()) {
                    String status = asyncResultMessage.result().body().getString(UserRegistryServerMongoHelper.STATUS);
                    if (status != null && status.equalsIgnoreCase(UserRegistryServerMongoHelper.STATUS_OK)) {
                        JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryServerMongoHelper.RESULT);
                        logger.d(asyncResultMessage.result().body().toString());
                        if (result != null) {
                            logger.d("User Found: " + result);
                            logger.d("REGISTERING - USER: " + usr + " ; SERVICE: " + srvcName + " ; ADDRESS: " + srvcAddress);

                            makeRegistration(usr, srvcName, srvcAddress);
                            printMaps();

                            logger.d("Registration executed successfully.");
                            // Sending successfully reply to the client (UserAgent, UserAgentAdmin, ServiceAgent, ..)
                            JsonObject reply = createSuccessfullyReply(null);
                            msg.reply(reply);
                        }
                        else {
                            logger.d("User Not Found.");
                            // Sending error reply to the client (UserAgent, UserAgentAdmin, ServiceAgent, ..)
                            JsonObject reply = createErrorReply("User/Password information are wrong.");
                            msg.reply(reply);
                        }
                    }
                    else {
                        logger.d("Registration failed: mongo KO.");
                        // Sending error reply to the client (UserAgent, UserAgentAdmin, ServiceAgent, ..)
                        JsonObject reply = createErrorReply("Internal (mongo) error.");
                        msg.reply(reply);
                    }
                }
                else {
                    logger.e("Registration failed: openxsp KO.");
                    logger.e("FailureType: " + ((ReplyException) asyncResultMessage.cause()).failureType().toString());
                    // Sending error reply to the client (UserAgent, UserAgentAdmin, ServiceAgent, ..)
                    JsonObject reply = createErrorReply("Internal (openxsp) error.");
                    msg.reply(reply);
                }
            }
        });
    }

    @Override
    public void unregisterUserServiceAddress(final Message<JsonObject> msg) {
        logger.setSubTag("unregisterUserServiceAddress");
        logger.d("Unregistration Request received.");

        // Check registration parameters
        boolean error = false;
        JsonObject body = msg.body();
        final String usr          = body.getString(UserRegistryServerMongoHelper.USR, null);
        final String pswd         = body.getString(UserRegistryServerMongoHelper.PWD, null);
        final String srvcName     = body.getString(UserRegistryServerMongoHelper.SERVICE_NAME, null);
        final String srvcAddress  = body.getString(UserRegistryServerMongoHelper.SERVICE_ADDRESS, null);
        if (usr == null)            error = true;
        if (pswd == null)           error = true;
        if (srvcName == null)       error = true;
        if (srvcAddress == null)    error = true;
        if (error) {
            JsonObject reply = createErrorReply("Some parameter is missing.");
            msg.reply(reply);
            return;
        }

        // Check the correctness of the user/password information provided by the UserAgent
        JsonObject userDocument = createUserDocument(usr, pswd);
        JsonObject findOneDocumentCmd = UserRegistryServerMongoHelper.createFindOneDocumentInCollection(COLLECTION_USERS, userDocument);
        eb.send(mongoModuleAddress, findOneDocumentCmd, new Handler<AsyncResult<Message<JsonObject>>>() {
            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                if (asyncResultMessage.succeeded()) {
                    String status = asyncResultMessage.result().body().getString(UserRegistryServerMongoHelper.STATUS);
                    if (status != null && status.equalsIgnoreCase(UserRegistryServerMongoHelper.STATUS_OK)) {
                        JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryServerMongoHelper.RESULT);
                        logger.d(asyncResultMessage.result().body().toString());
                        if (result != null) {
                            logger.d("User Found: " + result);
                            logger.d("UNREGISTERING - USER: " + usr + " ; SERVICE: " + srvcName + " ; ADDRESS: " + srvcAddress);

                            makeUnregistration(usr, srvcName, srvcAddress);
                            printMaps();

                            logger.d("Unregistration executed successfully.");
                            // Sending successfully reply to the client (UserAgent, UserAgentAdmin, ServiceAgent, ..)
                            JsonObject reply = createSuccessfullyReply(null);
                            msg.reply(reply);
                        }
                        else {
                            logger.d("User Not Found.");
                            // Sending error reply to the client (UserAgent, UserAgentAdmin, ServiceAgent, ..)
                            JsonObject reply = createErrorReply("User/Password information are wrong.");
                            msg.reply(reply);
                        }
                    }
                    else {
                        logger.d("Unregistration failed: mongo KO.");
                        // Sending error reply to the client (UserAgent, UserAgentAdmin, ServiceAgent, ..)
                        JsonObject reply = createErrorReply("Internal (mongo) error.");
                        msg.reply(reply);
                    }
                }
                else {
                    logger.e("Unregistration failed: openxsp KO.");
                    logger.e("FailureType: " + ((ReplyException) asyncResultMessage.cause()).failureType().toString());
                    // Sending error reply to the client (UserAgent, UserAgentAdmin, ServiceAgent, ..)
                    JsonObject reply = createErrorReply("Internal (openxsp) error.");
                    msg.reply(reply);
                }
            }
        });
    }

    @Override
    public void getUserServiceAddress(final Message<JsonObject> msg) {
        logger.setSubTag("getUserServiceAddress");
        logger.d("Get User Service Address Request received.");

        // Check registration parameters
        boolean error = false;
        JsonObject body = msg.body();
        final String usr          = body.getString(UserRegistryServerMongoHelper.USR, null);
        final String srvcName     = body.getString(UserRegistryServerMongoHelper.SERVICE_NAME, null);
        if (usr == null)            error = true;
        if (srvcName == null)       error = true;
        if (error) {
            JsonObject reply = createErrorReply("Some parameter is missing.");
            msg.reply(reply);
            return;
        }

        if (! userServices.containsKey(usr)) {
            JsonObject reply = createErrorReply("User not present");
            msg.reply(reply);
            return;
        }
        if (! userServices.containsEntry(usr, srvcName)) {
            JsonObject reply = createErrorReply("User not providing this service");
            msg.reply(reply);
            return;
        }

        updateHCMultiMaps();
        Collection<String> resultSet = userServiceAddresses.get(usr + "/" + srvcName);
        JsonArray addresses = new JsonArray(resultSet.toArray());
        JsonObject reply = createSuccessfullyReply(new JsonObject().putArray(UserRegistryServerMongoHelper.SERVICE_ADDRESSES, addresses));
        msg.reply(reply);
    }

    @Override
    public void getUserServices(Message<JsonObject> msg) {
        logger.setSubTag("getUserServices");
        logger.d("Get User Services Request received.");

        // Check registration parameters
        boolean error = false;
        JsonObject body = msg.body();
        final String usr          = body.getString(UserRegistryServerMongoHelper.USR, null);
        if (usr == null)            error = true;
        if (error) {
            JsonObject reply = createErrorReply("Some parameter is missing.");
            msg.reply(reply);
            return;
        }

        updateHCMultiMaps();
        JsonArray services = new JsonArray();
        if (userServices.containsKey(usr)) {
            Collection<String> resultSet = userServices.get(usr);
            services = new JsonArray(resultSet.toArray());
        }
        JsonObject reply = createSuccessfullyReply(new JsonObject().putArray(UserRegistryServerMongoHelper.SERVICE_NAMES, services));
        msg.reply(reply);
    }

    @Override
    public void addUser(final Message<JsonObject> msg) {
        logger.setSubTag("addUser");
        logger.d("User Add Request received.");

        // Check registration parameters
        boolean error = false;
        JsonObject body = msg.body();
        final String adminUsr     = body.getString(UserRegistryServerMongoHelper.ADMIN_USR, null);
        final String adminPwd     = body.getString(UserRegistryServerMongoHelper.ADMIN_PWD, null);
        final String usr          = body.getString(UserRegistryServerMongoHelper.USR, null);
        final String pwd          = body.getString(UserRegistryServerMongoHelper.PWD, null);
        if (adminUsr == null)       error = true;
        if (adminPwd == null)      error = true;
        if (usr == null)            error = true;
        if (pwd == null)           error = true;
        if (error) {
            JsonObject reply = createErrorReply("Some parameter is missing.");
            msg.reply(reply);
            return;
        }

        JsonObject userDocument = createUserDocument(usr, pwd);
        JsonObject adminDocument = createAdminUserDocument(adminUsr, adminPwd);
        JsonObject insertDocumentInCollectionCmd = UserRegistryServerMongoHelper.createInsertDocumentInCollection(COLLECTION_USERS, userDocument, adminDocument);

        eb.send(mongoModuleAddress, insertDocumentInCollectionCmd, new Handler<AsyncResult<Message<JsonObject>>>() {
            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                if (asyncResultMessage.succeeded()) {
                    String status = asyncResultMessage.result().body().getString(UserRegistryServerMongoHelper.STATUS);
                    if (status != null && status.equalsIgnoreCase(UserRegistryServerMongoHelper.STATUS_OK)) {
                        logger.d("Save document executed successfully.");
                        JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryServerMongoHelper.RESULT);
                        JsonObject reply = createSuccessfullyReply(null);
                        reply.putObject(UserRegistryServerMongoHelper.RESULT, result);
                        msg.reply(reply);
                    }
                    else {
                        logger.e("Save document failed: mongo ERROR.");
                        if (asyncResultMessage.result().body().getString(UserRegistryServerMongoHelper.MESSAGE) != null) {
                            String message = asyncResultMessage.result().body().getString(UserRegistryServerMongoHelper.MESSAGE);
                            logger.e("Error Message: " + message);
                            JsonObject reply = createErrorReply(message);
                            msg.reply(reply);
                        }
                    }
                }
                else {
                    logger.e("Save document failed: openxsp ERROR.");
                    String message = ((ReplyException) asyncResultMessage.cause()).failureType().toString();
                    logger.e("FailureType: " + message);
                    JsonObject reply = createErrorReply(message);
                    msg.reply(reply);
                }
            }
        });
    }

    @Override
    public void removeUser(final Message<JsonObject> msg) {
        logger.setSubTag("removeUser");
        logger.d("User Remove Request received.");

        // Check registration parameters
        boolean error = false;
        JsonObject body = msg.body();
        final String adminUsr     = body.getString(UserRegistryServerMongoHelper.ADMIN_USR, null);
        final String adminPwd     = body.getString(UserRegistryServerMongoHelper.ADMIN_PWD, null);
        final String usr          = body.getString(UserRegistryServerMongoHelper.USR, null);
        final String pwd          = body.getString(UserRegistryServerMongoHelper.PWD, null);
        if (adminUsr == null)       error = true;
        if (adminPwd == null)      error = true;
        if (usr == null)            error = true;
        if (pwd == null)           error = true;
        if (error) {
            JsonObject reply = createErrorReply("Some parameter is missing.");
            msg.reply(reply);
            return;
        }

        JsonObject userDocument = createUserDocument(usr, pwd);
        JsonObject adminDocument = createAdminUserDocument(adminUsr, adminPwd);
        JsonObject deleteDocumentInCollectionCmd = UserRegistryServerMongoHelper.createDeleteDocumentInCollection(COLLECTION_USERS, userDocument, adminDocument);

        eb.send(mongoModuleAddress, deleteDocumentInCollectionCmd, new Handler<AsyncResult<Message<JsonObject>>>() {
            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                if (asyncResultMessage.succeeded()) {
                    String status = asyncResultMessage.result().body().getString(UserRegistryServerMongoHelper.STATUS);
                    if (status != null && status.equalsIgnoreCase(UserRegistryServerMongoHelper.STATUS_OK)) {
                        logger.d("Delete document executed successfully.");
                        JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryServerMongoHelper.RESULT);
                        JsonObject reply = createSuccessfullyReply(null);
                        reply.putObject(UserRegistryServerMongoHelper.RESULT, result);
                        msg.reply(reply);
                    } else {
                        logger.e("Delete document failed: mongo ERROR.");
                        if (asyncResultMessage.result().body().getString(UserRegistryServerMongoHelper.MESSAGE) != null) {
                            String message = asyncResultMessage.result().body().getString(UserRegistryServerMongoHelper.MESSAGE);
                            logger.e("Error Message: " + message);
                            JsonObject reply = createErrorReply(message);
                            msg.reply(reply);
                        }
                    }
                } else {
                    logger.e("Delete document failed: openxsp ERROR.");
                    String message = ((ReplyException) asyncResultMessage.cause()).failureType().toString();
                    logger.e("FailureType: " + message);
                    JsonObject reply = createErrorReply(message);
                    msg.reply(reply);
                }
            }
        });
    }


    /*
     * Permit to drop the collection 'COLLECTION_USERS' from the mongo database
     */
    /*private void dropCollectionUsers(final JsonObject config) {
        JsonObject dropCollectionUserCmd = UserRegistryServerMongoHelper.createDropCollectionCommand(COLLECTION_USERS);

        logger.i("Sending to: " + mongoModuleAddress);
        eb.send(mongoModuleAddress, dropCollectionUserCmd, new Handler<AsyncResult<Message<JsonObject>>>() {
            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                if (asyncResultMessage.succeeded()) {
                    String status = asyncResultMessage.result().body().getString(UserRegistryServerMongoHelper.STATUS);
                    if (status != null && status.equalsIgnoreCase(UserRegistryServerMongoHelper.STATUS_OK)) {
                        logger.d("DropCollection for 'Users' executed successfully.");
                        try {
                            registerUserRegistryOperationHandlers(config);

                            // TODO just for testing
                            //saveMockUserDocument();
                        }
                        catch (InstantiationException e) {
                            logger.e("Error when instantiating the UserRegistry Persistor Class.");
                            e.printStackTrace();
                            logger.e("UserRegistry cannot be deployed. Exiting.");
                            container.exit();
                        }
                    }
                    else {
                        logger.d("DropCollection for 'Users' failed: mongo ERROR.");
                        if (asyncResultMessage.result().body().getString(UserRegistryServerMongoHelper.MESSAGE) != null)
                            logger.d("Error Message: " + asyncResultMessage.result().body().getString(UserRegistryServerMongoHelper.MESSAGE));
                    }
                }
                else {
                    logger.d("DropCollection for 'Users' failed: openxsp ERROR.");
                    logger.e("FailureType: " + ((ReplyException) asyncResultMessage.cause()).failureType().toString());
                }
            }
        });
    }*/


    private void makeRegistration(String usr, String srvcName, String srvcAddress) {
        updateHCMultiMaps();
        userServices.put(usr, srvcName);
        userServiceAddresses.put(usr+"/"+srvcName,srvcAddress);
    }

    private void makeUnregistration(String usr, String srvcName, String srvcAddress) {
        updateHCMultiMaps();
        userServiceAddresses.remove(usr + "/" + srvcName, srvcAddress);
        if (! userServiceAddresses.containsKey(usr+"/"+srvcName))
            userServices.remove(usr, srvcName);
    }

    private JsonObject createSuccessfullyReply(JsonObject result) {
        JsonObject r = new JsonObject().
                            putString(UserRegistryServerMongoHelper.STATUS, UserRegistryServerMongoHelper.STATUS_OK);
        if (result != null)
            r.putObject(UserRegistryServerMongoHelper.RESULT, result);
        return r;
    }

    private JsonObject createErrorReply(String message) {
        return new JsonObject().
                putString(UserRegistryServerMongoHelper.STATUS, UserRegistryServerMongoHelper.STATUS_ERROR).
                putString(UserRegistryServerMongoHelper.MESSAGE, message);
    }

    private JsonObject createUserDocument(String usr, String pwd) {
        String _id = String.valueOf((usr + pwd).hashCode());
        return new JsonObject().
                putString(UserRegistryServerMongoHelper._ID,_id).
                putString(UserRegistryServerMongoHelper.USR, usr).
                putString(UserRegistryServerMongoHelper.PWD, pwd);
    }

    private JsonObject createAdminUserDocument(String adminUsr, String adminPwd) {
        return new JsonObject().
                putString(UserRegistryServerMongoHelper.ADMIN_USR, adminUsr).
                putString(UserRegistryServerMongoHelper.ADMIN_PWD, adminPwd);
    }

    private void printMaps() {
        logger.setSubTag("printMaps");
        logger.d(userServices.entrySet().toString());
        logger.d(userServiceAddresses.entrySet().toString());
    }

}
