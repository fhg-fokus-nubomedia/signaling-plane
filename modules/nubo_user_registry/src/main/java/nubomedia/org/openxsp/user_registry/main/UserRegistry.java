package nubomedia.org.openxsp.user_registry.main;

import nubomedia.org.openxsp.user_registry.log.Logger;
import nubomedia.org.openxsp.user_registry.log.LoggerFactory;
import nubomedia.org.openxsp.user_registry.persistence.UserRegistryPersistor;
import org.openxsp.java.EventBus;
import org.openxsp.java.Verticle;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 *  This class permit to deploy a UserRegistry Verticle
 *
 *  Created by fmu on 4/25/14.
 */
public class UserRegistry extends Verticle {

    // ------------- USER REGISTRY CONFIG FIELDS -------------
    public static final String
            PERSISTENCE_MODULE_ADDRESS =                            "persistence_module_address",
            PERSISTOR_CLASS_FULL_NAME =                             "persistor_class_full_name",
            PERSISTOR_CLASS_OPERATION_ADDRESSES =                   "persistor_class_operation_addresses",
            PERSISTOR_CLASS_OPERATION_ADDRESS_REGISTER_ADDRESS =    "op_address_register_address",
            PERSISTOR_CLASS_OPERATION_ADDRESS_UNREGISTER_ADDRESS =  "op_address_unregister_address",
            PERSISTOR_CLASS_OPERATION_ADDRESS_GET_ADDRESS =         "op_address_get_address",
            PERSISTOR_CLASS_OPERATION_ADDRESS_GET_SERVICES =        "op_address_get_services",
            PERSISTOR_CLASS_OPERATION_ADDRESS_ADD_USER =            "op_address_add_user_address",
            PERSISTOR_CLASS_OPERATION_ADDRESS_REMOVE_USER =         "op_address_remove_user_address",
            PERSISTOR_CLASS_RESET_USER_DB =                         "persistor_class_reset_user_db";


    private Logger logger = LoggerFactory.getLogger("UserRegistry");

    private EventBus eb;
    private UserRegistryPersistor persistorInstance;


    /**
     * Start the execution of the verticle
     */
    @Override
    public void start() {
        logger.i("Deploying UserRegistry.. ");
        eb = openxsp.eventBus();
        initialize();
    }


    private void initialize() {
        JsonObject config = container.config();
        if ( (config == null) || (config.size() < 0) ) {
            logger.e("Configuration data must be provided.");
            logger.e("UserRegistry cannot be deployed. Exiting.");
            getContainer().exit();
            return;
        }

        // -------- Reading and checking Configuration file --------
        String persistorClassName = config.getString(PERSISTOR_CLASS_FULL_NAME, null);
        if (persistorClassName == null) {
            logger.e("A Persistor Class Name must be provided in the \"persistor_class_full_name\" field of the given configuration file.");
            logger.e("UserRegistry cannot be deployed. Exiting.");
            getContainer().exit();
            return;
        }

        String persistenceModuleAddress = config.getString(PERSISTENCE_MODULE_ADDRESS, null);
        if (persistenceModuleAddress == null) {
            logger.e("The Persistence Module Address must be provided in the \"persistence_module_address\" field of the given configuration file.");
            logger.e("UserRegistry cannot be deployed. Exiting.");
            getContainer().exit();
            return;
        }

        JsonObject persistorClassOperationAddresses = config.getObject(PERSISTOR_CLASS_OPERATION_ADDRESSES, null);
        if (persistorClassOperationAddresses == null) {
            logger.e("The Persistor Operation Addresses must be provided in the \"persistor_class_operation_addresses\" field of the given configuration file.");
            logger.e("UserRegistry cannot be deployed. Exiting.");
            getContainer().exit();
            return;
        }
        // ---------------------------------------------------------


        /*
        * Instantiate a "UserRegistryPersistor" instance that implements the methods to initialize the
        * UserRegistry tables, to register/unregister a user, and to retrieve the user data information
        * (using the default persistor class implementations or searching for the class specified
        * in the given configuration file)
        */
        try {
            Class<?> clazz = Class.forName(persistorClassName);
            Constructor<?> constructor;
            constructor = clazz.getConstructor(Container.class, EventBus.class, JsonObject.class);
            persistorInstance = (UserRegistryPersistor) constructor.newInstance(container, eb, config);
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException  e) {
            logger.e("Error when instantiating the UserRegistry Persistor Class.");
            e.printStackTrace();
            logger.e("UserRegistry cannot be deployed. Exiting.");
            getContainer().exit();  // todo with cluster mode doesn't work
            return;
        }

    }
}
