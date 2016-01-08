package nubomedia.org.openxsp.user_registry.persistence;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

/**
 * This interface defines the method to be implemented by a class that
 * want to provide a database support to the user registry
 *
 * Created by fmu on 4/15/14.
 */
public interface UserRegistryPersistor {

    void registerUserServiceAddress(Message<JsonObject> msg);

    void unregisterUserServiceAddress(Message<JsonObject> msg);

    void getUserServiceAddress(Message<JsonObject> msg);

    void getUserServices(Message<JsonObject> msg);

    void addUser(Message<JsonObject> msg);

    void removeUser(Message<JsonObject> msg);

    //void updateUser(Message<JsonObject> msg);
}
