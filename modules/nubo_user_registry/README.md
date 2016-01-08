Nubo\_User\_Registry
===
This module is intended to be used, through the openXSP Event Bus, by three different types of application entities. Such entities are ***User Agents***,
***Admin Agents***, and ***Service Agents***.

A typical scenario might be as follows.
An *Admin Agent* sends to the User Registry a request to add a new user on the system.
An *User Agent* sends to the User Registry a request to register a pair <service name, openXSP address> as it will listen on the openXSP Event Bus at that
specific openXSP address for the related service.
A *Service Agent* sends to the User Registry a request to know at which address a given User Agent is listening for a specific service.

Therefore, this module allows user data to be saved, retrieved, searched for, and deleted.
In particular each *User Agent* can register addresses for various services (identified by their names). For every service more than one address for the same
*User Agent* can be registered and an *User Agent* can also register the same address for different services.

***

Configuration
---
The **nubo\_user\_registry** module takes the following configuration:

    {
        "persistence_module_address":           <address>,
        "persistor_class_full_name":            <implementation of 'UserRegistryPersistor' interface>,
        "persistor_class_operation_addresses":{
            "op_address_register_address":      <address>,
            "op_address_unregister_address":    <address>,
            "op_address_get_address":           <address>,
            "op_address_get_services":          <address>,
            "op_address_add_user_address":      <address>,
            "op_address_remove_user_address":   <address>,
        }
    }

For example:

    {
        "persistence_module_address":           "openxsp.mongopersistor",
        "persistor_class_full_name":
            "nubomedia.org.openxsp.user_registry.persistence.mongodb.UserRegistryPersistorMongo",
        "persistor_class_operation_addresses":  {
            "op_address_register_address":
                "nubomedia.org.openxsp.user_registry.mongo_persistor.register_address",
            "op_address_unregister_address":
                "nubomedia.org.openxsp.user_registry.mongo_persistor.unregister_address",
            "op_address_get_address":
                "nubomedia.org.openxsp.user_registry.mongo_persistor.get_addresses",
            "op_address_get_services":
                "nubomedia.org.openxsp.user_registry.mongo_persistor.get_services",
            "op_address_add_user_address":
                "nubomedia.org.openxsp.user_registry.mongo_persistor.add_user",
            "op_address_remove_user_address":
                "nubomedia.org.openxsp.user_registry.mongo_persistor.remove_user"
        }
    }

Let's take a look at each field in turn:
 * "***persistence_module_address***":          The openXSP address of the persistence module which takes care to really persist on a DB the information passed to it by the *Nubo User Registry*
 * "***persistor_class_full_name***":           The java full name of the class implementing the *UserRegistryPersistor* interface
 * "***persistor_class_operation_addresses***": The json object containing the addresses of all the operation provided by the *Nubo\_User\_Registry*
 * "***op_address_register_address***":         The openXSP address for the `Register Address` operation that allow an *User Agent* to register an openXSP address at which it is listening for a specific service
 * "***op_address_unregister_address***":       The openXSP address for the `Unregister Address` operation that allow an *User Agent* to unregister an openXSP address at which it is listening for a specific service
 * "***op_address_get_address***":              The openXSP address for the `Get Address` operation that allow an *User Agent* or a *Service Agent* to retrieve the address at which a third *User Agent* is listening for a given service
 * "***op_address_get_services***":             The openXSP address for the `Get Services` operation that allow an *User Agent* or a *Service Agent* to retrieve the service names for which an *User Agent* is listening at some address
 * "***op_address_add_user***":                 The openXSP address for the `Add User` operation that allow an *Admin Agent* to add an user in the system (and persist its credentials on the underlying DB)
 * "***op_address_remove_user***":              The openXSP address for the `Remove User` operation that allow an *Admin Agent* to remove an user in the system (and delete its credentials from the underlying DB)

***

Operations
---
The User Registry module support the following operations. Only an administrative operation such as `Add User` and `Remove User` are persisted in a real DB,
so the `Register Address` and `Unregister Address` operation's effects are valid just until the *Nubo\_User\_Registry* is running.

### Register Address #
Register a pair an openXSP address related to a specific service name.
To register an address send a JSON message to the relative module operation-related address:

    {
        "usr":          <username>,
        "pwd":          <password>,
        "srvcName":     <service name>,
        "srvcAddress":  <service address>
    }

Where:
 * `username` and `password` are the user credential of an user which should be already existing in the system for the successfully execution of the operation.
 * `service name` and `service address` are the service name and the service openXSP address pair being registered.

An example would be:

    {
        "usr":          "mock_username",
        "pwd":          "mock_password",
        "srvcName":     "mock_service_name",
        "srvcAddress":  "mock_service_address"
    }

When the operation complete successfully, a reply message is sent back to the sender with the following data:

    {
        "status": "ok"
    }

If an error occurs during the registration a reply is returned with the following data:

    {
        "status": "error",
        "message": <message>
    }

Where:
 * `message` is an error message.

### Unregister Address #
Unregister a pair an openXSP address related to a specific service name.
To unregister an address send a JSON message to the module operation-related address:

    {
        "usr":          <username>,
        "pwd":          <password>,
        "srvcName":     <service name>,
        "srvcAddress":  <service address>
    }

Where:
* `username` and `password` are the user credential of an user which should be already existing in the system for the successfully execution of the operation.
* `service name` and `service address` are the service name and the service openXSP address pair being unregistered.

An example would be:

    {
        "usr":          "mock_username",
        "pwd":          "mock_password",
        "srvcName":     "mock_service_name",
        "srvcAddress":  "mock_service_address"
    }

When the operation complete successfully, a reply message is sent back to the sender with the following data:
    {
        "status": "ok"
    }

If an error occurs during the registration a reply is returned with the following data:

    {
        "status": "error",
        "message": <message>
    }

Where:
* `message` is an error message.

### Get Address #
Retrieve the address at which an user is listening for a specific service.
To retrieve the addresses for a given service at which an user is listening send a JSON message to the module operation-related address:

    {
        "usr":          <username>,
        "srvcName":     <service name>
    }

Where:
* `username` is the user name of the user whom address is being retrieved.
* `service name` is the service name of the user whom address is being retrieved.

An example would be:

    {
        "usr":          "mock_username",
        "srvcName":     "mock_service_name"
    }

When the operation complete successfully, a reply message is sent back to the sender with the following data:

    {
        "status": "ok",
        "result": {
                "srvcAddresses": [ "mock_service_address_1", .. , "mock_service_address_N" ]
        }
    }

If an error occurs during the registration a reply is returned with the following data:

    {
        "status": "error",
        "message": <message>
    }

Where:
* `message` is an error message.

### Get Services #
Retrieve the service names for which an user is listening at some openXSP address.
To retrieve the service names for which a user is listening send a JSON message to the module operation-related address:

    {
        "usr":          <username>
    }

Where:
* `username` is the user name listening for the service names which are willing to be retrieved.

An example would be:

    {
        "usr":          "mock_username"
    }

When the operation complete successfully, a reply message is sent back to the sender with the following data:

    {
        "status": "ok",
        "result": {
                    "srvcNames": [ "mock_service_name_1", .. , "mock_service_name_N" ]
        }
    }

If an error occurs during the registration a reply is returned with the following data:

    {
        "status": "error",
        "message": <message>
    }

Where:
* `message` is an error message.

### Add User #
Add an user in the system.
To add an user send a JSON message to the module operation-related address:

    {
        "adminUsr":     <admin_username>,
        "adminPwd":     <admin_password>,
        "usr":          <username>,
        "pwd":          <password>
    }

Where:
* `admin_username` and `admin_password` are the user credential of an user which have the permissions to modify the state of the system (and to write on the real DB where the users are persisted).
* `username` and `password` are the user credential of an user which should be already existing in the system for the successfully execution of the operation.

An example would be:

    {
        "adminUsr":     "admin_username",
        "adminPwd":     "admin_password",
        "usr":          "mock_username",
        "pwd":          "mock_password"
    }

When the operation complete successfully, a reply message is sent back to the sender with the following data:

    {
        "status": "ok"
    }

If an error occurs during the registration a reply is returned with the following data:

    {
        "status": "error",
        "message": <message>
    }

Where:
* `message` is an error message.

### Remove User #
Remove an user from the system.
To remove an user send a JSON message to the module operation-related address:

    {
        "adminUsr":     <admin_username>,
        "adminPwd":     <admin_password>,
        "usr":          <username>,
        "pwd":          <password>
    }

Where:
* `admin_username` and `admin_password` are the user credential of an user which have the permissions to modify the state of the system (and to write on the real DB where the users are persisted).
* `username` and `password` are the user credential of an user which should be already existing in the system for the successfully execution of the operation.

An example would be:

    {
        "adminUsr":     "admin_username",
        "adminPwd":     "admin_password",
        "usr":          "mock_username",
        "pwd":          "mock_password"
    }

When the operation complete successfully, a reply a reply message is sent back to the sender with the following data:

    {
        "status": "ok"
    }

If an error occurs during the registration a reply is returned with the following data:
    {
        "status": "error",
        "message": <message>
    }

Where:
* `message` is an error message.
