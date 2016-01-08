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
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by fmu on 31/03/15.
 */
public class MockServiceAgent extends Verticle {

    public static final String
            MOCK_USER =              "mockUser",
            MOCK_SERVICE_NAME =      "mockServiceName";

    private Logger logger = LoggerFactory.getLogger("MockServiceAgent");

    private EventBus eb;
    private String addressOpGetServiceAddress;
    private String addressOpGetServices;

    @Override
    public void start() {
        logger.i("Deploying MockUserAgent..");
        eb = openxsp.eventBus();

        JsonObject config = container.config();
        readUserRegistryOperationAddresses(config);

        executeGetAddress();
        //executeGetServices();
    }

    private void readUserRegistryOperationAddresses(JsonObject config) {
        JsonObject operationHandlersAddresses = config.getObject(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESSES, null);
        addressOpGetServiceAddress = operationHandlersAddresses.getString(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESS_GET_ADDRESS, null);
        addressOpGetServices = operationHandlersAddresses.getString(UserRegistry.PERSISTOR_CLASS_OPERATION_ADDRESS_GET_SERVICES, null);
    }

    private void executeGetAddress() {
        JsonObject getAddressRequest = UserRegistryClientHelper.createGetAddressRequest(MOCK_USER, MOCK_SERVICE_NAME);
        eb.send(addressOpGetServiceAddress, getAddressRequest, new Handler<AsyncResult<Message<JsonObject>>>() {
            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                if (asyncResultMessage.succeeded()) {
                    String status = asyncResultMessage.result().body().getString(UserRegistryClientHelper.STATUS);
                    if (status != null && status.equalsIgnoreCase(UserRegistryClientHelper.STATUS_OK)) {
                        logger.d("Retrieval of the service address executed successfully.");

                        JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryClientHelper.RESULT);
                        JsonArray addresses = result.getArray(UserRegistryClientHelper.SERVICE_ADDRESSES);
                        logger.d(addresses.size() + " --- " + addresses.toString());
                    }
                    else {
                        logger.e("Retrieval of the service address failed.");
                        logger.e("Error: " + asyncResultMessage.result().body().getString(UserRegistryClientHelper.MESSAGE));
                    }
                }
                else {
                    logger.e("Retrieval of the service address failed.");
                    logger.e("FailureType: " + ((ReplyException) asyncResultMessage.cause()).failureType().toString());
                }
            }
        });
    }

    private void executeGetServices() {
        JsonObject getServiceNamesRequest = UserRegistryClientHelper.createGetServicesRequest(MOCK_USER);
        eb.send(addressOpGetServices, getServiceNamesRequest, new Handler<AsyncResult<Message<JsonObject>>>() {
            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                if (asyncResultMessage.succeeded()) {
                    String status = asyncResultMessage.result().body().getString(UserRegistryClientHelper.STATUS);
                    if (status != null && status.equalsIgnoreCase(UserRegistryClientHelper.STATUS_OK)) {
                        logger.d("Retrieval of the service names executed successfully.");

                        JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryClientHelper.RESULT);
                        JsonArray services = result.getArray(UserRegistryClientHelper.SERVICE_NAMES);
                        logger.d(services.size() + " --- " + services.toString());
                    }
                    else {
                        logger.e("Retrieval of the service names failed.");
                        logger.e("Error: " + asyncResultMessage.result().body().getString(UserRegistryClientHelper.MESSAGE));
                    }
                }
                else {
                    logger.e("Retrieval of the service names failed.");
                    logger.e("FailureType: " + ((ReplyException) asyncResultMessage.cause()).failureType().toString());
                }
            }
        });
    }

}
