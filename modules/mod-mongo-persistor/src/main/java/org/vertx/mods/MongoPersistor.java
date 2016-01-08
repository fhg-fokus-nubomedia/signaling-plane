/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vertx.mods;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.net.ssl.SSLSocketFactory;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * MongoDB Persistor Bus Module<p>
 * Please see the README.md for a full description<p>
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 * @author Thomas Risberg
 * @author Richard Warburton
 */
public class MongoPersistor extends BusModBase implements Handler<Message<JsonObject>> {

    protected String address;
    protected String host;
    protected int port;
    protected String dbName;
    protected String username;
    protected String password;
    protected ReadPreference readPreference;
    protected boolean autoConnectRetry;
    protected int socketTimeout;
    protected boolean useSSL;

    protected MongoClient mongoClient;
    protected MongoDatabase mongoDB;
    private boolean useMongoTypes;

    @Override
    public void start() {
        super.start();

        address = getOptionalStringConfig("address", "openxsp.mongopersistor");

        host = getOptionalStringConfig("host", "localhost");
        port = getOptionalIntConfig("port", 27017);
        dbName = getOptionalStringConfig("db_name", "default_db");
        username = getOptionalStringConfig("username", "mp_openxspmod");
        password = getOptionalStringConfig("password", "mp_openxspmod_pswd");
        readPreference = ReadPreference.valueOf(getOptionalStringConfig("read_preference", "primary"));
        int poolSize = getOptionalIntConfig("pool_size", 10);
        autoConnectRetry = getOptionalBooleanConfig("auto_connect_retry", true);
        socketTimeout = getOptionalIntConfig("socket_timeout", 60000);
        useSSL = getOptionalBooleanConfig("use_ssl", false);
        useMongoTypes = getOptionalBooleanConfig("use_mongo_types", false);

        JsonArray seedsProperty = config.getArray("seeds");

        try {
            MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
            builder.connectionsPerHost(poolSize);
            //builder.autoConnectRetry(autoConnectRetry);
            builder.socketTimeout(socketTimeout);
            builder.readPreference(readPreference);

            if (useSSL) {
                builder.socketFactory(SSLSocketFactory.getDefault());
            }

            if (seedsProperty == null) {
                ServerAddress address = new ServerAddress(host, port);
                if (username != null && password != null) {
                    System.out.println("NO SEEDS - Authenticating..... " + username + " " + dbName + " " + password + " ");
                    MongoCredential credential = MongoCredential.createCredential(username, dbName, password.toCharArray());
                    mongoClient = new MongoClient(address, Arrays.asList(credential), builder.build());
                }
                else
                    mongoClient = new MongoClient(address, builder.build());
            }
            else {
                List<ServerAddress> seeds = makeSeeds(seedsProperty);
                if (username != null && password != null) {
                    System.out.println("SEEDS - Authenticating..... " + username + " " + dbName + " " + password + " ");
                    MongoCredential credential = MongoCredential.createCredential(username, dbName, password.toCharArray());
                    mongoClient = new MongoClient(seeds, Arrays.asList(credential), builder.build());
                }
                else
                    mongoClient = new MongoClient(seeds, builder.build());
            }

            mongoDB = mongoClient.getDatabase(dbName);
        } catch (UnknownHostException e) {
            logger.error("Failed to connect to mongo server", e);
        }

        System.out.println("Registering to: " + address);
        eb.registerHandler(address, this);
    }

    private List<ServerAddress> makeSeeds(JsonArray seedsProperty) throws UnknownHostException {
        List<ServerAddress> seeds = new ArrayList<ServerAddress>();
        for (Object elem : seedsProperty) {
            JsonObject address = (JsonObject) elem;
            String host = address.getString("host");
            int port = address.getInteger("port");
            seeds.add(new ServerAddress(host, port));
        }
        return seeds;
    }

    @Override
    public void stop() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Override
    public void handle(Message<JsonObject> message) {
        String action = message.body().getString("action");

        System.out.println(message.body());
        if (action == null) {
            sendError(message, "action must be specified");
            return;
        }

        try {

            // Note actions should not be in camel case, but should use underscores
            // I have kept the version with camel case so as not to break compatibility

            switch (action) {
                case "save":
                    doSave(message);
                    break;
                case "insert":
                    doInsert(message);
                    break;
                case "update":
                    doUpdate(message);
                    break;
                case "delete":
                    doDelete(message);
                    break;
                case "find":
                    doFind(message);
                    break;
                case "findone":
                    doFindOne(message);
                    break;
                // no need for a backwards compatible "findAndModify" since this feature was added after
                case "find_and_modify":
                    doFindAndModify(message);
                    break;
                case "count":
                    doCount(message);
                    break;
                case "getCollections":
                case "get_collections":
                    getCollections(message);
                    break;
                case "dropCollection":
                case "drop_collection":
                    dropCollection(message);
                    break;
                case "collectionStats":
                case "collection_stats":
                    getCollectionStats(message);
                    break;
                case "aggregate":
                    doAggregation(message);
                    break;
                case "command":
                    runCommand(message);
                    break;
                default:
                    sendError(message, "Invalid action: " + action);
            }
        } catch (MongoException e) {
            sendError(message, e.getMessage(), e);
        }
    }

    private void doSave(Message<JsonObject> message) {
        sendError(message, "TO DO");
        /*String collectionName = getMandatoryString("collection", message);
        if (collectionName == null) {
            return;
        }
        JsonObject document = getMandatoryObject("document", message);
        if (document == null) {
            return;
        }

        JsonObject credentials = getMandatoryObject("credentials", message);
        if (credentials == null) {
            sendError(message, "Write credentials needed");
            return;
        }

        String usr = credentials.getString("admin_usr", null);
        String pwd = credentials.getString("admin_pwd", null);
        if (usr == null || pwd == null ) {
            sendError(message, "Some parameter is missing");
            return;
        }

        ServerAddress address = new ServerAddress(host, port);
        MongoCredential credential = MongoCredential.createCredential(usr, dbName, pwd.toCharArray());
        MongoClient mc = new MongoClient(address, Arrays.asList(credential));
        MongoDatabase mdb = mc.getDatabase(dbName);

        MongoCollection<BsonDocument> collection = mdb.getCollection(collectionName, BsonDocument.class);
        //MongoCollection<BsonDocument> collection = mongoDB.getCollection(collectionName, BsonDocument.class);

        WriteConcern writeConcern = WriteConcern.valueOf(getOptionalStringConfig("writeConcern", ""));
        // Backwards compatibility
        if (writeConcern == null) {
            writeConcern = WriteConcern.valueOf(getOptionalStringConfig("write_concern", ""));
        }
        if (writeConcern == null) {
            writeConcern = mdb.getWriteConcern();
        }
        if (collection != null && writeConcern != null) {
            collection = collection.withWriteConcern(writeConcern);
        }

        if (collection != null) {

            if (document.getField("_id") == null) {
                String genID = UUID.randomUUID().toString();
                document.putString("_id", genID);

                collection.insertOne(BsonDocument.parse(document.toString()));
                JsonObject reply = new JsonObject();
                reply.putString("_id", genID);
                sendOK(message, reply);

            } else {
                sendOK(message);
            }
        }

        mc.close();*/
    }

    private void doInsert(Message<JsonObject> message) {
        String collectionName = getMandatoryString("collection", message);
        if (collectionName == null) {
            sendError(message, "'collection' object is missing");
            return;
        }
        JsonObject document = getMandatoryObject("document", message);
        if (document == null) {
            sendError(message, "'document' object is missing");
            return;
        }
        JsonObject credentials = getMandatoryObject("credentials", message);
        if (credentials == null) { // todo modify it to make it try to execute the operation with the current connection with the db
            sendError(message, "'credentials' object is missing");
            return;
        }
        String usr = credentials.getString("admin_usr", null);
        String pwd = credentials.getString("admin_pwd", null);
        if (usr == null || pwd == null ) {
            sendError(message, "Some admin parameter is missing");
            return;
        }

        ServerAddress address = new ServerAddress(host, port);
        MongoCredential credential = MongoCredential.createCredential(usr, dbName, pwd.toCharArray());
        MongoClient mc = new MongoClient(address, Arrays.asList(credential));
        MongoDatabase mdb = mc.getDatabase(dbName);
        MongoCollection<BsonDocument> collection = mdb.getCollection(collectionName, BsonDocument.class);
        //MongoCollection<BsonDocument> collection = mongoDB.getCollection(collectionName, BsonDocument.class);

        WriteConcern writeConcern = WriteConcern.valueOf(message.body().getString("writeConcern", ""));
        // Backwards compatibility
        if (writeConcern == null) {
            writeConcern = WriteConcern.valueOf(message.body().getString("write_concern", ""));
        }
        if (writeConcern == null) {
            writeConcern = mdb.getWriteConcern();
        }
        if (collection != null && writeConcern != null) {
            collection = collection.withWriteConcern(writeConcern);
        }

        // Check the presence of the mongodb standard "_id" field and if not passed by the client a random one is generated
        String genID;
        if (document.getField("_id") == null) {
            genID = UUID.randomUUID().toString();
            document.putString("_id", genID);
        }
        else {
            genID = null;
        }

        // Try to insert the document
        if (collection != null) {
            try {
                collection.insertOne(jsonToBson(document));

                if (genID == null) {
                    sendOK(message);
                } else {
                    JsonObject reply = new JsonObject();
                    reply.putString("_id", genID);
                    sendOK(message, reply);
                }
            }
            catch (MongoException me) {
                sendError(message, me.getLocalizedMessage());
            }
            finally {
                mc.close();
            }
        }
        else {
            sendError(message, "Collection not existing");
        }

        mc.close();
    }

    private void doUpdate(Message<JsonObject> message) {
        String collectionName = getMandatoryString("collection", message);
        if (collectionName == null) {
            sendError(message, "'collection' object is missing");
            return;
        }
        JsonObject matcher = getMandatoryObject("matcher", message);
        if (matcher == null) {
            sendError(message, "'matcher' object is missing");
            return;
        }
        JsonObject update = getMandatoryObject("update", message);
        if (update == null) {
            sendError(message, "'update' object is missing");
            return;
        }

        JsonObject credentials = getMandatoryObject("credentials", message);
        if (credentials == null) { // todo modify it to make it try to execute the operation with the current connection with the db
            sendError(message, "'credentials' object is missing");
            return;
        }

        String usr = credentials.getString("admin_usr", null);
        String pwd = credentials.getString("admin_pwd", null);
        if (usr == null || pwd == null ) {
            sendError(message, "Some parameter is missing");
            return;
        }

        ServerAddress address = new ServerAddress(host, port);
        MongoCredential credential = MongoCredential.createCredential(usr, dbName, pwd.toCharArray());
        MongoClient mc = new MongoClient(address, Arrays.asList(credential));
        MongoDatabase mdb = mc.getDatabase(dbName);
        MongoCollection<BsonDocument> collection = mdb.getCollection(collectionName, BsonDocument.class);

        WriteConcern writeConcern = WriteConcern.valueOf(message.body().getString("writeConcern", ""));
        // Backwards compatibility
        if (writeConcern == null) {
            writeConcern = WriteConcern.valueOf(message.body().getString("write_concern", ""));
        }
        if (writeConcern == null) {
            writeConcern = mdb.getWriteConcern();
        }
        if (collection != null && writeConcern != null) {
            collection = collection.withWriteConcern(writeConcern);
        }

        UpdateOptions updateOptions = null;
        Boolean upsert = message.body().getBoolean("upsert", false);
        if (upsert) {
             updateOptions = new UpdateOptions().upsert(true);
        }

        Boolean multi = message.body().getBoolean("multi", false);

        if (collection != null) {
            try {
                UpdateResult res;
                if (!multi)
                    res = collection.updateOne(jsonToBson(matcher), jsonToBson(update), updateOptions);
                else
                    res = collection.updateMany(jsonToBson(matcher), jsonToBson(update), updateOptions);

                JsonObject number = new JsonObject().putNumber("number", res.getModifiedCount());
                JsonObject reply = new JsonObject();
                reply.putObject("result", number);
                sendOK(message, reply);
            }
            catch (MongoException me) {
                sendError(message, me.getLocalizedMessage());
            }
            finally {
                mc.close();
            }
        }
        else {
            sendError(message, "Collection not existing");
        }

        mc.close();
    }

    private void doDelete(Message<JsonObject> message) {
        String collectionName = getMandatoryString("collection", message);
        if (collectionName == null) {
            sendError(message, "'collection' object is missing");
            return;
        }
        JsonObject matcher = getMandatoryObject("matcher", message);
        if (matcher == null) {
            sendError(message, "'matcher' object is missing");
            return;
        }
        JsonObject credentials = getMandatoryObject("credentials", message);
        if (credentials == null) { // todo modify it to make it try to execute the operation with the current connection with the db
            sendError(message, "'credentials' object is missing");
            return;
        }
        String usr = credentials.getString("admin_usr", null);
        String pwd = credentials.getString("admin_pwd", null);
        if (usr == null || pwd == null ) {
            sendError(message, "Some parameter is missing");
            return;
        }

        ServerAddress address = new ServerAddress(host, port);
        MongoCredential credential = MongoCredential.createCredential(usr, dbName, pwd.toCharArray());
        MongoClient mc = new MongoClient(address, Arrays.asList(credential));
        MongoDatabase mdb = mc.getDatabase(dbName);
        MongoCollection<BsonDocument> collection = mdb.getCollection(collectionName, BsonDocument.class);
        //MongoCollection<BsonDocument> collection = mongoDB.getCollection(collectionName, BsonDocument.class);

        WriteConcern writeConcern = WriteConcern.valueOf(message.body().getString("writeConcern", ""));
        // Backwards compatibility
        if (writeConcern == null) {
            writeConcern = WriteConcern.valueOf(message.body().getString("write_concern", ""));
        }
        if (writeConcern == null) {
            writeConcern = mdb.getWriteConcern();
        }
        if (collection != null && writeConcern != null) {
            collection = collection.withWriteConcern(writeConcern);
        }

        Boolean justOne = message.body().getBoolean("justOne", false);

        if (collection != null) {
            try {
                DeleteResult res;
                if (justOne)
                    res = collection.deleteOne(jsonToBson(matcher));
                else
                    res = collection.deleteMany(jsonToBson(matcher));

                JsonObject number = new JsonObject().putNumber("number", res.getDeletedCount());
                JsonObject reply = new JsonObject();
                reply.putObject("result", number);
                sendOK(message, reply);
            }
            catch (MongoException me) {
                sendError(message, me.getLocalizedMessage());
            }
            finally {
                mc.close();
            }
        }
        else {
            sendError(message, "Collection not existing");
        }

        mc.close();
    }

    private void doFind(Message<JsonObject> message) {
        sendError(message, "TO DO");
        /*String collection = getMandatoryString("collection", message);
        if (collection == null) {
            return;
        }
        Integer limit = (Integer) message.body().getNumber("limit");
        if (limit == null) {
            limit = -1;
        }
        Integer skip = (Integer) message.body().getNumber("skip");
        if (skip == null) {
            skip = -1;
        }
        Integer batchSize = (Integer) message.body().getNumber("batch_size");
        if (batchSize == null) {
            batchSize = 100;
        }
        Integer timeout = (Integer) message.body().getNumber("timeout");
        if (timeout == null || timeout < 0) {
            timeout = 10000; // 10 seconds
        }
        JsonObject matcher = message.body().getObject("matcher");
        JsonObject keys = message.body().getObject("keys");

        Object hint = message.body().getField("hint");
        Object sort = message.body().getField("sort");

        DBCollection coll = db.getCollection(collection);
        DBCursor cursor;
        if (matcher != null) {
            cursor = (keys == null) ?
                    coll.find(jsonToDBObject(matcher)) :
                    coll.find(jsonToDBObject(matcher), jsonToDBObject(keys));
        } else {
            cursor = coll.find();
        }
        if (skip != -1) {
            cursor.skip(skip);
        }
        if (limit != -1) {
            cursor.limit(limit);
        }
        if (sort != null) {
            cursor.sort(sortObjectToDBObject(sort));
        }
        if (hint != null) {
            if (hint instanceof JsonObject) {
                cursor.hint(jsonToDBObject((JsonObject) hint));
            } else if (hint instanceof String) {
                cursor.hint((String) hint);
            } else {
                throw new IllegalArgumentException("Cannot handle type " + hint.getClass().getSimpleName());
            }
        }
        sendBatch(message, cursor, batchSize, timeout);*/
    }

    /*private DBObject sortObjectToDBObject(Object sortObj) {
        if (sortObj instanceof JsonObject) {
            // Backwards compatability and a simpler syntax for single-property sorting
            return jsonToDBObject((JsonObject) sortObj);
        } else if (sortObj instanceof JsonArray) {
            JsonArray sortJsonObjects = (JsonArray) sortObj;
            DBObject sortDBObject = new BasicDBObject();
            for (Object curSortObj : sortJsonObjects) {
                if (!(curSortObj instanceof JsonObject)) {
                    throw new IllegalArgumentException("Cannot handle type "
                            + curSortObj.getClass().getSimpleName());
                }

                sortDBObject.putAll(((JsonObject) curSortObj).toMap());
            }

            return sortDBObject;
        } else {
            throw new IllegalArgumentException("Cannot handle type " + sortObj.getClass().getSimpleName());
        }
    }*/

    private void sendBatch(Message<JsonObject> message, final DBCursor cursor, final int max, final int timeout) {
        sendError(message, "TO DO");
        /*int count = 0;
        JsonArray results = new JsonArray();
        while (cursor.hasNext() && count < max) {
            DBObject obj = cursor.next();
            JsonObject m = dbObjectToJsonObject(obj);
            results.add(m);
            count++;
        }
        if (cursor.hasNext()) {
            JsonObject reply = createBatchMessage("more-exist", results);

            // If the user doesn't reply within timeout, close the cursor
            final long timerID = vertx.setTimer(timeout, new Handler<Long>() {
                @Override
                public void handle(Long timerID) {
                    container.logger().warn("Closing DB cursor on timeout");
                    try {
                        cursor.close();
                    } catch (Exception ignore) {
                    }
                }
            });


            message.reply(reply, new Handler<Message<JsonObject>>() {
                @Override
                public void handle(Message<JsonObject> msg) {
                    vertx.cancelTimer(timerID);
                    // Get the next batch
                    sendBatch(msg, cursor, max, timeout);
                }
            });

        } else {
            JsonObject reply = createBatchMessage("ok", results);
            message.reply(reply);
            cursor.close();
        }*/
    }

    /*private JsonObject createBatchMessage(String status, JsonArray results) {
        JsonObject reply = new JsonObject();
        reply.putArray("results", results);
        reply.putString("status", status);
        reply.putNumber("number", results.size());
        return reply;
    }*/

    private void doFindOne(Message<JsonObject> message) {
        logger.debug(message.body());

        String collectionName = getMandatoryString("collection", message);
        if (collectionName == null) {
            return;
        }

        JsonObject matcher = message.body().getObject("matcher");
        JsonObject keys = message.body().getObject("keys");
        MongoCollection<BsonDocument> collection = mongoDB.getCollection(collectionName, BsonDocument.class);

        FindIterable<BsonDocument> findResults; //coll.find(bquery, JsonObject.class);
        if (matcher == null) {
            //res = keys != null ? collection.find()findOne(null, jsonToDBObject(keys)) : collection.findOne();
            findResults = (keys != null) ?
                    collection.find(BsonDocument.class).projection(jsonToBson(keys))
                    :
                    collection.find(BsonDocument.class);
        }
        else {
            //res = keys != null ? collection.findOne(jsonToDBObject(matcher), jsonToDBObject(keys)) : collection.findOne(jsonToDBObject(matcher));
            findResults = (keys != null) ?
                    collection.find(jsonToBson(matcher), BsonDocument.class).projection(jsonToBson(keys))
                    :
                    collection.find(jsonToBson(matcher), BsonDocument.class);
        }

        BsonDocument result = findResults.first();

        JsonObject reply = new JsonObject();
        if (result != null) {
            JsonObject m = new JsonObject(result.toJson());
            reply.putObject("result", m);
        }
        sendOK(message, reply);
    }

    private void doFindAndModify(Message<JsonObject> message) {
        sendError(message, "TO DO");
        /*String collectionName = getMandatoryString("collection", message);
        if (collectionName == null) {
            return;
        }
        JsonObject msgBody = message.body();
        DBObject update = jsonToDBObjectNullSafe(msgBody.getObject("update"));
        DBObject query = jsonToDBObjectNullSafe(msgBody.getObject("matcher"));
        DBObject sort = jsonToDBObjectNullSafe(msgBody.getObject("sort"));
        DBObject fields = jsonToDBObjectNullSafe(msgBody.getObject("fields"));
        boolean remove = msgBody.getBoolean("remove", false);
        boolean returnNew = msgBody.getBoolean("new", false);
        boolean upsert = msgBody.getBoolean("upsert", false);

        DBCollection collection = db.getCollection(collectionName);
        DBObject result = collection.findAndModify(query, fields, sort, remove,
                update, returnNew, upsert);

        JsonObject reply = new JsonObject();
        if (result != null) {
            JsonObject resultJson = dbObjectToJsonObject(result);
            reply.putObject("result", resultJson);
        }
        sendOK(message, reply);*/
    }

    private void doCount(Message<JsonObject> message) {
        sendError(message, "TO DO");
        /*String collection = getMandatoryString("collection", message);
        if (collection == null) {
            return;
        }
        JsonObject matcher = message.body().getObject("matcher");
        DBCollection coll = db.getCollection(collection);
        long count;
        if (matcher == null) {
            count = coll.count();
        } else {
            count = coll.count(jsonToDBObject(matcher));
        }
        JsonObject reply = new JsonObject();
        reply.putNumber("count", count);
        sendOK(message, reply);*/
    }

    private void getCollections(Message<JsonObject> message) {
        sendError(message, "TO DO");
        /*JsonObject reply = new JsonObject();
        reply.putArray("collections", new JsonArray(db.getCollectionNames().toArray()));
        sendOK(message, reply);*/
    }

    private void dropCollection(Message<JsonObject> message) {
        JsonObject reply = new JsonObject();
        String collectionName = getMandatoryString("collection", message);

        if (collectionName == null) {
            return;
        }

        MongoCollection collection = mongoDB.getCollection(collectionName);

        try {
            collection.drop();
            sendOK(message, reply);
        } catch (MongoException mongoException) {
            sendError(message, "exception thrown when attempting to drop collection: " + collectionName + " \n" + mongoException.getMessage());
        }
    }

    private void getCollectionStats(Message<JsonObject> message) {
        sendError(message, "TO DO");
        /*String collection = getMandatoryString("collection", message);

        if (collection == null) {
            return;
        }

        DBCollection coll = db.getCollection(collection);
        CommandResult stats = coll.getStats();

        JsonObject reply = new JsonObject();
        reply.putObject("stats", dbObjectToJsonObject(stats));
        sendOK(message, reply);*/

    }

    private void doAggregation(Message<JsonObject> message) {
        sendError(message, "TO DO");
        /*if (isCollectionMissing(message)) {
            sendError(message, "collection is missing");
            return;
        }
        if (isPipelinesMissing(message.body().getArray("pipelines"))) {
            sendError(message, "no pipeline operations found");
            return;
        }
        String collection = getMandatoryString("collection", message);
        JsonArray pipelinesAsJson = message.body().getArray("pipelines");
        List<DBObject> pipelines = jsonPipelinesToDbObjects(pipelinesAsJson);

        DBCollection dbCollection = db.getCollection(collection);
        // v2.11.1 of the driver has an inefficient method signature in terms
        // of parameters, so we have to remove the first one
        DBObject firstPipelineOp = pipelines.remove(0);
        AggregationOutput aggregationOutput = dbCollection.aggregate(firstPipelineOp, pipelines.toArray(new DBObject[] {}));

        JsonArray results = new JsonArray();
        for (DBObject dbObject : aggregationOutput.results()) {
            results.add(dbObjectToJsonObject(dbObject));
        }

        JsonObject reply = new JsonObject();
        reply.putArray("results", results);
        sendOK(message, reply);*/
    }

    /*private List<DBObject> jsonPipelinesToDbObjects(JsonArray pipelinesAsJson) {
        List<DBObject> pipelines = new ArrayList<>();
        for (Object pipeline : pipelinesAsJson) {
            DBObject dbObject = jsonToDBObject((JsonObject) pipeline);
            pipelines.add(dbObject);
        }
        return pipelines;
    }*/

    /*private boolean isCollectionMissing(Message<JsonObject> message) {
        return getMandatoryString("collection", message) == null;
    }*/

    /*private boolean isPipelinesMissing(JsonArray pipelines) {
        return pipelines == null || pipelines.size() == 0;
    }*/

    private void runCommand(Message<JsonObject> message) {
        JsonObject reply = new JsonObject();

        String command = getMandatoryString("command", message);

        if (command == null) {
            return;
        }

        Document result = mongoDB.runCommand(jsonToBson(command));

        reply.putObject("result", new JsonObject(result));
        sendOK(message, reply);
    }

    /*private JsonObject dbObjectToJsonObject(DBObject obj) {
        if (useMongoTypes) {
            return MongoUtil.convertBsonToJson(obj);
        } else {
            return new JsonObject(obj.toMap());
        }
    }*/

    /*private DBObject jsonToDBObject(JsonObject object) {
        if (useMongoTypes) {
            return MongoUtil.convertJsonToBson(object);
        } else {
            return new BasicDBObject(object.toMap());
        }
    }*/

    /*private DBObject jsonToDBObjectNullSafe(JsonObject object) {
        if (object != null) {
            return jsonToDBObject(object);
        } else {
            return null;
        }
    }*/

    private BsonDocument jsonToBson(JsonObject json) {
        //if (json == null) return null;

        return BsonDocument.parse(json.toString());
    }

    private BsonDocument jsonToBson(String json) {
        //if (json == null) return null;

        return BsonDocument.parse(json);
    }
}

