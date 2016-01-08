package nubomedia.org.openxsp.user_registry_client.main;

import org.openxsp.java.EventBus;
import org.openxsp.java.Verticle;
import nubomedia.org.openxsp.user_registry_client.log.Logger;
import nubomedia.org.openxsp.user_registry_client.log.LoggerFactory;
import nubomedia.org.openxsp.user_registry.main.UserRegistry;
import nubomedia.org.openxsp.user_registry.util.UserRegistryClientHelper;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.ReplyException;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by fmu on 31/03/15.
 */
public class MockUserAgent extends Verticle {

    public static final String
            MOCK_USR =               "mockUser",
            MOCK_PWD =               "mockPswd",
            MOCK_SERVICE_NAME =      "mockServiceName",
            MOCK_SERVICE_ADDRESS =   "mockServiceAddress";

    private Logger logger = LoggerFactory.getLogger("MockUserAgent");

    private EventBus eb;
    private String addressOpRegisterServiceAddress;
    private String addressOpUnregisterAddress;

    @Override
    public void start() {
        logger.i("Deploying MockUserAgent..");
        eb = openxsp.eventBus();

        JsonObject config = container.config();
        readUserRegistryOperationAddresses(config);

        //executeRegistration();
        executeUnregistration();
    }

    private void readUserRegistryOperationAddresses(JsonObject config) {
        JsonObject operationHandlersAddresses = config.getObject(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESSES, null);
        addressOpRegisterServiceAddress = operationHandlersAddresses.getString(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESS_REGISTER_ADDRESS, null);
        addressOpUnregisterAddress = operationHandlersAddresses.getString(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESS_UNREGISTER_ADDRESS, null);
    }

    private void executeRegistration() {
        JsonObject registerRequest = UserRegistryClientHelper.createRegisterRequest(MOCK_USR, MOCK_PWD, MOCK_SERVICE_NAME, MOCK_SERVICE_ADDRESS);
        eb.send(addressOpRegisterServiceAddress, registerRequest, new Handler<AsyncResult<Message<JsonObject>>>() {
            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                if (asyncResultMessage.succeeded()) {
                    String status = asyncResultMessage.result().body().getString(UserRegistryClientHelper.STATUS);
                    if (status != null && status.equalsIgnoreCase(UserRegistryClientHelper.STATUS_OK)) {
                        logger.d("Registration of the service address executed successfully.");
                    }
                    else {
                        if (status != null && status.equalsIgnoreCase(UserRegistryClientHelper.STATUS_ERROR)) {
                            logger.e("Registration of the service address failed.");
                            logger.e("Error: " + asyncResultMessage.result().body().getString(UserRegistryClientHelper.MESSAGE));
                        }
                    }
                }
                else {
                    logger.e("Registration of the service address failed.");
                    logger.e("FailureType: " + ((ReplyException) asyncResultMessage.cause()).failureType().toString());
                }
            }
        });
    }

    private void executeUnregistration() {
        JsonObject unregisterRequest = UserRegistryClientHelper.createUnregisterRequest(MOCK_USR, MOCK_PWD, MOCK_SERVICE_NAME, MOCK_SERVICE_ADDRESS);
        eb.send(addressOpUnregisterAddress, unregisterRequest, new Handler<AsyncResult<Message<JsonObject>>>() {
            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                if (asyncResultMessage.succeeded()) {
                    String status = asyncResultMessage.result().body().getString(UserRegistryClientHelper.STATUS);
                    if (status != null && status.equalsIgnoreCase(UserRegistryClientHelper.STATUS_OK)) {
                        logger.d("Unregistration of the service address executed successfully.");
                    }
                    else {
                        if (status != null && status.equalsIgnoreCase(UserRegistryClientHelper.STATUS_ERROR)) {
                            logger.e("Unregistration of the service address failed.");
                            logger.e("Error: " + asyncResultMessage.result().body().getString(UserRegistryClientHelper.MESSAGE));
                        }
                    }
                }
                else {
                    logger.e("Unregistration of the service address failed.");
                    logger.e("FailureType: " + ((ReplyException) asyncResultMessage.cause()).failureType().toString());
                }
            }
        });
    }
}
