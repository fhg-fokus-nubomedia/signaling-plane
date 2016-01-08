Mod_Mongo_Persistor
===

This module allows data to be saved, retrieved, searched for, and deleted in a MongoDB instance. MongoDB is a great match for persisting vert.x data since it natively handles JSON (BSON) documents.

Dependencies
---
This module requires a MongoDB server to be available on the network.

Name
---
The module name is `mod-mongo-persistor`.

Configuration
---
The mod-mongo-persistor module takes the following configuration:

    {
        "address":              <address>,
        "host":                 <host>,
        "port":                 <port>,
        "username":             <username>,
        "password":             <password>,
        "db_name":              <db_name>,
        "pool_size":            <pool_size>,
        "use_ssl":              <bool>,
        "read_preference":      <e.g. "nearest" or "primary" etecetera>,
        "use_mongo_types":      <bool>,
        "socket_timeout":       <default 60000>,
        "auto_connect_retry":   <default true>
    }

For example:
    {
        "address":          "openxsp.mongopersistor",
        "host":             "localhost",
        "port":             27017,
        "pool_size":        20,
        "db_name":          "default_db",
        "read_preference":  "nearest",
        "use_mongo_types":  false
    }

Let's take a look at each field in turn:
* "***address***":          The main address for the module. Every module has a main address. Defaults to `openxsp.mongopersistor`.
* "***host***":             Host name or ip address of the MongoDB instance. Defaults to `localhost`.
* "***port***":             Port at which the MongoDB instance is listening. Defaults to `27017`.
* "***db_name***":          Name of the database in the MongoDB instance to use. Defaults to `default_db`.
* "***pool_size***":        The number of socket connections the module instance should maintain to the MongoDB server. Default is 10.
* "***use_ssl***":          Enable SSL based connections.  See http://docs.mongodb.org/manual/tutorial/configure-ssl/ for more details. Defaults to `false`.
* "***read_preference***":  Is the read preferences, see http://docs.mongodb.org/manual/core/read-preference/. Default is "primary".
* "***use_mongo_types***":  Enable the use of mongo types such as Date, byte array, array list. Note that if enabled this will incur a performance overhead to all queries. Default is `false`.

Operations
---
The User Registry module support the following operations.

### Insert #
Insert a document in the database.
To insert a document send a JSON message to the module main address:

    {
        "action":       "insert",
        "collection":   <collection>,
        "document":     <document>,
        "credentials":  <credentials>
    }

Where:
* `collection` is the name of the MongoDB collection that you wish to insert the document in. This field is mandatory.
* `document` is the JSON document that you wish to insert. This field is mandatory.
* `credentials` is a JSON object containing the credentials of an user who has the grant to perform write operations on the `collection` specified. This field is mandatory. This object as the following format:

    {
        "admin_usr":    <admin_username>,
        "admin_pwd":    <admin_password>
    }

An example would be:

    {
        "action":       "insert",
        "collection":   "Users",
        "document":     {
            "_id":  <random_value>,
            "usr":  <username>,
            "pwd":  <password>
        },
        "credentials":  {
            "admin_usr": <admin_username>,
            "admin_pwd": <admin_password>
        }
    }

When the insert complete successfully, a reply message is sent back to the sender (for example a *UserRegistryPersistorMongo* instance) with the following data:

    {
        "status":   "ok"
    }

The reply will also contain a field `_id` if the document that was insert didn't specify an id, this will be an automatically generated UUID, for example:

    {
        "status":   "ok"
        "_id":      "ffeef2a7-5658-4905-a37c-cfb19f70471d"
    }

If you insert a document which already possesses an `_id` field, and a document with the same id already exists in the database, then an error will rise.
(The *UserRegistryPersistorMongo* class provided generates an `_id` value as the hashcode of a string obtained as `username + password`).
If an error occurs in inserting the document a reply is returned:

    {
        "status":   "error",
        "message":  <message>
    }

Where
* `message` is an error message.

### Update #
Update a document in the database.
To update a document send a JSON message to the module main address:

    {
        "action":       "update",
        "collection":   <collection>,
        "document":     <document>,
        "credentials":  <credentials>
    }

Where:
* `collection` is the name of the MongoDB collection that you wish to update the document in. This field is mandatory.
* `document` is the JSON document that you wish to update. This field is mandatory.
* `credentials` is a JSON object containing the credentials of an user who has the grant to perform write operations on the `collection` specified. This field is mandatory. This object as the following format:

    {
        "admin_usr":    <admin_username>,
        "admin_pwd":    <admin_password>
    }

An example would be:

    {
        "action":       "update",
        "collection":   "Users",
        "document":     {
            "_id":  <random_value>,
            "usr":  <username>,
            "pwd":  <password>
        },
        "credentials":  {
            "admin_usr": <admin_username>,
            "admin_pwd": <admin_password>
        }
    }

When the update complete successfully, a reply message is sent back to the sender (for example a *UserRegistryPersistorMongo* instance) with the following data:

    {
        "status":   "ok"
    }


The reply will also contain an object-field `result` containing a field `number` which represents the number of entries updated. For example:

    {
        "status": "ok"
        "result": {
            "number":   1
        }
    }

If an error occurs in updating the document a reply is returned:

    {
        "status":   "error",
        "message":  <message>
    }

Where
* `message` is an error message.

### Delete #
Deletes a matching documents in the database.

To delete documents send a JSON message to the module main address:

    {
        "action":       "delete",
        "collection":   <collection>,
        "matcher":      <matcher>,
        "justOne":      true
        "credentials":  <credentials>
    }

Where:
* `collection` is the name of the MongoDB collection that you wish to delete from. This field is mandatory.
* `matcher` is a JSON object that you want to match against to delete matching documents. This obeys the normal MongoDB matching rules.
* `justOne` is a flag which determines if one or all the documents matching have to be deleted
* `credentials` is a JSON object containing the credentials of an user who has the grant to perform write operations on the `collection` specified. This field is mandatory. This object as the following format:

All documents that match will be deleted.

An example would be:

    {
        "action":       "delete",
        "collection":   "Users",
        "matcher": {
            "_id": "ffeef2a7-5658-4905-a37c-cfb19f70471d"
        },
        "justOne": true,
        "credentials":  {
                    "admin_usr": <admin_username>,
                    "admin_pwd": <admin_password>
        }
    }

This would delete the item with the specified id.

When the delete complete successfully, a reply message is sent back to the sender with the following data:

    {
        "status": "ok",
        "number": <number>
    }

Where
* `number` is the number of documents deleted.

If an error occurs in finding the documents a reply is returned:

    {
        "status": "error",
        "message": <message>
    }

Where
* `message` is an error message.

### Find One
Finds a single matching document in the database.

To find a document send a JSON message to the module main address:

    {
        "action":       "findone",
        "collection":   <collection>,
        "matcher":      <matcher>,
        "keys":         <keys>
    }

Where:
* `collection` is the name of the MongoDB collection that you wish to search in in. This field is mandatory.
* `matcher` is a JSON object that you want to match against to find a matching document. This obeys the normal MongoDB matching rules.
* `keys` is an optional JSON object that contains the fields that should be returned for matched documents. See MongoDB manual for more information. Example: { "usr": 1 } will only return objects with _id and the `usr` field

If more than one document matches, just the first one will be returned.

An example would be:

    {
        "action":       "findone",
        "collection":   "Users",
        "matcher": {
            "_id": "ffeef2a7-5658-4905-a37c-cfb19f70471d"
        }
    }

This would return the item with the specified id.

When the find complete successfully, a reply message is sent back to the sender with the following data:

    {
        "status": "ok",
        "result": <result>
    }

If an error occurs in finding the documents a reply is returned:

    {
        "status": "error",
        "message": <message>
    }

Where
*`message` is an error message.
