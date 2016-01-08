package nubomedia.org.openxsp.user_registry.test;

import nubomedia.org.openxsp.user_registry.log.Logger;
import nubomedia.org.openxsp.user_registry.log.LoggerFactory;
import nubomedia.org.openxsp.user_registry.main.UserRegistry;
import nubomedia.org.openxsp.user_registry.util.UserRegistryClientHelper;
import org.junit.*;
import org.openxsp.java.Verticle;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;


import java.util.HashMap;
import java.util.Map;

public class UserRegistryVerticleTest extends TestVerticle {

    private static Logger logger = LoggerFactory.getLogger("UserRegistryVerticleTest");

    public static final String
            ADMIN_USR =             "uaAdmin_default_db",
            ADMIN_PWD =             "uaAdmin_default_db_pswd",
            MOCK_USR =              "mockUser",
            MOCK_PWD =              "mockPswd",
            MOCK_SERVICE_NAME =     "mockServiceName",
            MOCK_SERVICE_ADDRESS =  "mockServiceAddress";

    private static String MOCK_PERSISTENCE_MODULE_ADDRESS = "test.mock_persistence_module";
    private static Map<String, String> storage;

    // Creates a mock persistence module that receives the persistence events and replies mocked answers
    @Test
    public void createMockPersistenceModule() {
        //test with an invalid config
        container.deployVerticle(MockPersistenceVerticle.class.getName(), new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> res) {
                
            	//check if the module has been started correctly
            	
            	if(res.failed() && res.cause()!=null)res.cause().printStackTrace();
            	
                Assert.assertFalse(res.failed());
<<<<<<< Updated upstream

                userRegistryStartTest();
=======
                
                
>>>>>>> Stashed changes
            }
        });
    }


	@Test
    @Ignore //will be triggered manually
	public void userRegistryStartTest(){
		//start the user registry module with an invalid configuration
		JsonObject invalidConfig = new JsonObject();
		/*container.deployVerticle(UserRegistry.class.getName(), invalidConfig, new Handler<AsyncResult<String>>(){
			@Override
			public void handle(AsyncResult<String> res) {
				//check if the module has been started correctly
				//Assert.assertTrue(res.failed());
			}
		});*/

        //start the user registry module with an valid configuration
        JsonObject validConfig = new JsonObject();
        validConfig.putString("persistence_module_address","test.mock_persistence_module");
        validConfig.putString("persistor_class_full_name","nubomedia.org.openxsp.user_registry.persistence.mongodb.UserRegistryPersistorMongo");
        validConfig.putObject("persistor_class_operation_addresses", new JsonObject().
                        putString("op_address_register_address","test.register_address").
                        putString("op_address_unregister_address","test.unregister_address").
                        putString("op_address_get_address","test.get_address").
                        putString("op_address_get_services","test.get_services").
                        putString("op_address_add_user_address","test.add_user").
                        putString("op_address_remove_user_address","test.remove_user")
        );
        container.deployVerticle(UserRegistry.class.getName(), validConfig, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> res) {
                //logger.i(res.cause().getMessage());
                //VertxAssert.testComplete();

                Assert.assertFalse(res.failed());
                logger.i("Deployed!!");

                //run the tests
                addUser();
            }
        });
	}

    public void addUser(){
        // Adding a new user
        JsonObject addRequest = UserRegistryClientHelper.createAddRequest(ADMIN_USR, ADMIN_PWD, MOCK_USR, MOCK_PWD);
        vertx.eventBus().sendWithTimeout("test.add_user", addRequest, 5000, new Handler<AsyncResult<Message<JsonObject>>>() {
            @Override
            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                Assert.assertFalse(asyncResultMessage.failed());
                String status = asyncResultMessage.result().body().getString(UserRegistryClientHelper.STATUS);
                Assert.assertEquals(status, UserRegistryClientHelper.STATUS_OK);
                JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryClientHelper.RESULT);
                if (result != null) logger.d("ADD USER - Result: " + result.toString());

                // Remove the previously added user
                JsonObject removeRequest = UserRegistryClientHelper.createRemoveRequest(ADMIN_USR, ADMIN_PWD, MOCK_USR, MOCK_PWD);
                vertx.eventBus().sendWithTimeout("test.remove_user", removeRequest, 5000, new Handler<AsyncResult<Message<JsonObject>>>() {
                    public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                        Assert.assertFalse(asyncResultMessage.failed());
                        String status = asyncResultMessage.result().body().getString(UserRegistryClientHelper.STATUS);
                        Assert.assertEquals(status, UserRegistryClientHelper.STATUS_OK);
                        JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryClientHelper.RESULT);
                        if (result != null) logger.d("REMOVE USER - Result: User removed are: " + result.toString());

                        // Add again a user to continue the tests
                        JsonObject addRequest = UserRegistryClientHelper.createAddRequest(ADMIN_USR, ADMIN_PWD, MOCK_USR, MOCK_PWD);
                        vertx.eventBus().sendWithTimeout("test.add_user", addRequest, 5000, new Handler<AsyncResult<Message<JsonObject>>>() {
                            @Override
                            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                                Assert.assertFalse(asyncResultMessage.failed());
                                String status = asyncResultMessage.result().body().getString(UserRegistryClientHelper.STATUS);
                                Assert.assertEquals(status, UserRegistryClientHelper.STATUS_OK);
                                JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryClientHelper.RESULT);
                                if (result != null) logger.d("ADD USER - Result: " + result.toString());

                                // Register an address for the user added
                                JsonObject registerRequest = UserRegistryClientHelper.createRegisterRequest(MOCK_USR, MOCK_PWD, MOCK_SERVICE_NAME, MOCK_SERVICE_ADDRESS);
                                vertx.eventBus().sendWithTimeout("test.register_address", registerRequest, 5000, new Handler<AsyncResult<Message<JsonObject>>>() {
                                    @Override
                                    public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                                        Assert.assertFalse(asyncResultMessage.failed());
                                        String status = asyncResultMessage.result().body().getString(UserRegistryClientHelper.STATUS);
                                        Assert.assertEquals(status, UserRegistryClientHelper.STATUS_OK);
                                        JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryClientHelper.RESULT);
                                        if (result != null) logger.d("REGISTER ADDRESS - Result: " + result.toString());

                                        // Get the address just registered
                                        JsonObject getAddressRequest = UserRegistryClientHelper.createGetAddressRequest(MOCK_USR, MOCK_SERVICE_NAME);
                                        vertx.eventBus().sendWithTimeout("test.get_address", getAddressRequest, 5000, new Handler<AsyncResult<Message<JsonObject>>>() {
                                            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                                                Assert.assertFalse(asyncResultMessage.failed());
                                                String status = asyncResultMessage.result().body().getString(UserRegistryClientHelper.STATUS);
                                                Assert.assertEquals(status, UserRegistryClientHelper.STATUS_OK);
                                                JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryClientHelper.RESULT);
                                                if (result != null) {
                                                    JsonArray addresses = result.getArray(UserRegistryClientHelper.SERVICE_ADDRESSES);
                                                    logger.d("REGISTERED ADDRESS - Result: " + addresses.toString());
                                                }

                                                // Get the service names of the previously added user
                                                JsonObject getServiceNamesRequest = UserRegistryClientHelper.createGetServicesRequest(MOCK_USR);
                                                vertx.eventBus().sendWithTimeout("test.get_services", getServiceNamesRequest, 5000, new Handler<AsyncResult<Message<JsonObject>>>() {
                                                    public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                                                        Assert.assertFalse(asyncResultMessage.failed());
                                                        String status = asyncResultMessage.result().body().getString(UserRegistryClientHelper.STATUS);
                                                        Assert.assertEquals(status, UserRegistryClientHelper.STATUS_OK);
                                                        JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryClientHelper.RESULT);
                                                        if (result != null) {
                                                            JsonArray services = result.getArray(UserRegistryClientHelper.SERVICE_NAMES);
                                                            logger.d("USER SERVICE NAMES - Result: " + services.toString());
                                                        }

                                                        // Unregister the user's address just registered
                                                        JsonObject unregisterRequest = UserRegistryClientHelper.createUnregisterRequest(MOCK_USR, MOCK_PWD, MOCK_SERVICE_NAME, MOCK_SERVICE_ADDRESS);
                                                        vertx.eventBus().sendWithTimeout("test.unregister_address", unregisterRequest, 5000, new Handler<AsyncResult<Message<JsonObject>>>() {
                                                            @Override
                                                            public void handle(AsyncResult<Message<JsonObject>> asyncResultMessage) {
                                                                Assert.assertFalse(asyncResultMessage.failed());
                                                                String status = asyncResultMessage.result().body().getString(UserRegistryClientHelper.STATUS);
                                                                Assert.assertEquals(status, UserRegistryClientHelper.STATUS_OK);
                                                                JsonObject result = asyncResultMessage.result().body().getObject(UserRegistryClientHelper.RESULT);
                                                                if (result != null) logger.d("UNREGISTER ADDRESS - Result: " + result.toString());

                                                                VertxAssert.testComplete();
                                                            }
                                                        }); // untegister the address

                                                    }
                                                }); // get service names

                                            }
                                        }); // get addresses

                                    }
                                }); // register

                            }
                        }); // second add

                    }
                }); // remove

            }
        }); // first add
    }

	public void testRegister(){
		JsonObject msg = new JsonObject();
		vertx.eventBus().send("user_registry_event", msg, new Handler<Message<String>>(){

			@Override
			public void handle(Message<String> arg0) {

			}
		});
	}
<<<<<<< Updated upstream

    public static class MockPersistenceVerticle extends Verticle implements Handler<Message<JsonObject>> {

=======
//
//	@Test
//	@Ignore //will be triggered manually
//	public void testRegister(){
//		//TODO send events to the user registry module and check with assertions the correct execution
//		JsonObject msg = new JsonObject();
//
//		vertx.eventBus().send("user_registry_event", msg, new Handler<Message<String>>(){
//
//			@Override
//			public void handle(Message<String> arg0) {
//				// TODO check if the operation was successful
//				/*
//				Assert.assertEquals("expected", "actual");
//				Assert.assertNotNull(null);
//				Assert.assertFalse(true);
//				...
//				*/
//				runTestXYZ();
//			}
//
//		});
//	}
//
//	@Test
//	@Ignore //will be triggered manually in order to preserve the order
//	public void runTestXYZ(){
//		//TODO
//	}


    public static class MockPersistenceVerticle extends Verticle {

        /*
         * Start the execution of the verticle
         */
>>>>>>> Stashed changes
        @Override
        public void start() {
            logger.i("Deploying MockPersistenceVerticle.. ");
            storage = new HashMap<>();

            openxsp.eventBus().registerHandler(MOCK_PERSISTENCE_MODULE_ADDRESS, this);
        }

        @Override
        public void handle(Message<JsonObject> message) {
            String action = message.body().getString("action");

            System.out.println(message.body());
            if (action == null) {
                logger.e("action must be specified");
                return;
            }

            switch (action) {
                case "insert":
                    doInsert(message);
                    break;
                case "delete":
                    doDelete(message);
                    break;
                case "findone":
                    doFindOne(message);
                    break;
                default:
                    logger.e("Invalid action: " + action);
            }
        }

        private void doInsert(Message<JsonObject> message) {
            String collectionName = message.body().getString("collection", null);
            if (collectionName == null) {
                sendERROR(message, "'collection' object is missing");
                return;
            }
            JsonObject document = message.body().getObject("document", null);
            if (document == null) {
                sendERROR(message, "'document' object is missing");
                return;
            }
            JsonObject credentials = message.body().getObject("credentials", null);
            if (credentials == null) { // todo modify it to make it try to execute the operation with the current connection with the db
                sendERROR(message, "'credentials' object is missing");
                return;
            }
            String admin_usr = credentials.getString("admin_usr", null);
            String admin_pwd = credentials.getString("admin_pwd", null);
            if (admin_usr == null || admin_pwd == null) {
                sendERROR(message, "Some admin parameter is missing");
                return;
            }

            String usr = document.getString("usr", null);
            String pwd = document.getString("pwd", null);
            if (usr == null || pwd == null) {
                sendERROR(message, "Some user parameter is missing");
                return;
            }

            storage.put(usr, pwd);
            sendOK(message, null);
        }

        private void doDelete(Message<JsonObject> message) {
            String collectionName = message.body().getString("collection", null);
            if (collectionName == null) {
                sendERROR(message, "'collection' object is missing");
                return;
            }
            JsonObject matcher = message.body().getObject("matcher", null);
            if (matcher == null) {
                sendERROR(message, "'matcher' object is missing");
                return;
            }
            JsonObject credentials = message.body().getObject("credentials", null);
            if (credentials == null) { // todo modify it to make it try to execute the operation with the current connection with the db
                sendERROR(message, "'credentials' object is missing");
                return;
            }
            String admin_usr = credentials.getString("admin_usr", null);
            String admin_pwd = credentials.getString("admin_pwd", null);
            if (admin_usr == null || admin_pwd == null) {
                sendERROR(message, "Some admin parameter is missing");
                return;
            }

            String usr = matcher.getString("usr", null);
            String pwd = matcher.getString("pwd", null);

            if (!storage.containsKey(usr))
                sendERROR(message, "usr not in memory");
            else {
                String pwd2 = storage.get(usr);
                if (! pwd.equals(pwd2))
                    sendERROR(message, "usr not in memory");
            }

            storage.remove(usr);
            sendOK(message, null);
        }


        private void doFindOne(Message<JsonObject> message) {
            String collectionName = message.body().getString("collection", null);
            if (collectionName == null) {
                sendERROR(message, "'collection' object is missing");
                return;
            }

            JsonObject matcher = message.body().getObject("matcher", null);
            if (matcher == null) {
                sendERROR(message, "'matcher' object is missing");
                return;
            }

            String usr = matcher.getString("usr", null);
            String pwd = matcher.getString("pwd", null);

            if (!storage.containsKey(usr))
                sendERROR(message, "usr not in memory");
            else {
                String pwd2 = storage.get(usr);
                if (! pwd.equals(pwd2))
                    sendERROR(message, "usr not in memory");
            }

            JsonObject result = new JsonObject().
                    putString("usr", usr).
                    putString("pwd", pwd);
            sendOK(message, result);
        }

        private void sendOK(Message<JsonObject> message, JsonObject result) {
            JsonObject reply = new JsonObject().putString("status","ok");
            if (result != null)
                reply.putObject("result",result);
            message.reply(reply);
        }

        private void sendERROR(Message<JsonObject> message, String errorMessage) {
            JsonObject reply = new JsonObject().putString("status","error");
            if (errorMessage != null)
                reply.putString("message",errorMessage);
            else
                reply.putString("message", "Error not specified");
            message.reply(reply);
        }
    }
}
