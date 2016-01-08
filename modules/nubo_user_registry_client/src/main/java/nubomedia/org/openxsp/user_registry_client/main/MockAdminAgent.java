package nubomedia.org.openxsp.user_registry_client.main;

import nubomedia.org.openxsp.user_registry.main.UserRegistry;
import nubomedia.org.openxsp.user_registry.util.UserRegistryClientHelper;
import nubomedia.org.openxsp.user_registry_client.log.Logger;
import nubomedia.org.openxsp.user_registry_client.log.LoggerFactory;
import org.openxsp.java.EventBus;
import org.openxsp.java.Verticle;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by fmu on 31/03/15.
 */
public class MockAdminAgent extends Verticle {

    public static final String
            ADMIN_USR =            "uaAdmin_default_db",
            ADMIN_PWD =            "uaAdmin_default_db_pswd",
            MOCK_USR =             "mockUser",
            MOCK_PWD =             "mockPswd";

    private Logger logger = LoggerFactory.getLogger("MockAdminAgent");

    private EventBus eb;
    private String addressOpAddUserAddress;
    private String addressOpRemoveUserAddress;


    @Override
    public void start() {
        logger.i("Deploying MockAdminAgent..");
        eb = openxsp.eventBus();

        JsonObject config = container.config();
        readUserRegistryOperationAddresses(config);

        executeAdd();
        //executeRemove();
    }

    private void readUserRegistryOperationAddresses(JsonObject config) {
        JsonObject operationHandlersAddresses = config.getObject(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESSES, null);
        addressOpAddUserAddress = operationHandlersAddresses.getString(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESS_ADD_USER, null);
        addressOpRemoveUserAddress = operationHandlersAddresses.getString(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESS_REMOVE_USER, null);
    }

    private void executeAdd() {
        JsonObject addRequest = UserRegistryClientHelper.createAddRequest(ADMIN_USR, ADMIN_PWD, MOCK_USR, MOCK_PWD);
        eb.send(addressOpAddUserAddress, addRequest, new Handler<AsyncResult<Message<JsonObject>>>() {
            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                if (asyncResultMessage.succeeded()) {
                    String status = asyncResultMessage.result().body().getString(UserRegistryClientHelper.STATUS);
                    if (status != null && status.equalsIgnoreCase(UserRegistryClientHelper.STATUS_OK)) {
                        logger.d("Adding of the user executed successfully.");
                        JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryClientHelper.RESULT);
                        if (result != null)
                            logger.d("Result: " + result.toString());
                    }
                    else {
                        if (status != null && status.equalsIgnoreCase(UserRegistryClientHelper.STATUS_ERROR)) {
                            logger.e("Adding of the user failed.");
                            logger.e("Error: " + asyncResultMessage.result().body().getString(UserRegistryClientHelper.MESSAGE));
                        }
                    }
                }
                else {
                    logger.e("Adding of the user failed.");
                    logger.e("FailureType: " + ((ReplyException) asyncResultMessage.cause()).failureType().toString());
                }
            }
        });
    }

    private void executeRemove() {
        //JsonObject matcher = new JsonObject().putString("usr", MOCK_USR).putString("pwd",MOCK_PWD);
        JsonObject removeRequest = UserRegistryClientHelper.createRemoveRequest(ADMIN_USR, ADMIN_PWD, MOCK_USR, MOCK_PWD);
        eb.send(addressOpRemoveUserAddress, removeRequest, new Handler<AsyncResult<Message<JsonObject>>>() {
            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                if (asyncResultMessage.succeeded()) {
                    String status = asyncResultMessage.result().body().getString(UserRegistryClientHelper.STATUS);
                    if (status != null && status.equalsIgnoreCase(UserRegistryClientHelper.STATUS_OK)) {
                        logger.d("Removing of the user executed successfully.");
                        JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryClientHelper.RESULT);
                        if (result != null)
                            logger.d("User removed: " + result.getNumber("number"));
                    }
                    else {
                        if (status != null && status.equalsIgnoreCase(UserRegistryClientHelper.STATUS_ERROR)) {
                            logger.e("Removing of the user failed.");
                            logger.e("Error: " + asyncResultMessage.result().body().getString(UserRegistryClientHelper.MESSAGE));
                        }
                    }
                }
                else {
                    logger.e("Removing of the user failed.");
                    logger.e("FailureType: " + ((ReplyException) asyncResultMessage.cause()).failureType().toString());
                }
            }
        });
    }
}
