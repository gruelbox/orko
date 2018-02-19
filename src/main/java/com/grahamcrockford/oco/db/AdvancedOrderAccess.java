package com.grahamcrockford.oco.db;

import org.bson.types.ObjectId;
import org.mongojack.DBQuery;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.oco.api.AdvancedOrder;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;


/**
 * Simple wrapper for sending orders to the queue.
 */
@Singleton
public class AdvancedOrderAccess {

  private final MongoClient mongoClient;
  private final JobLocker jobLocker;

  @Inject
  AdvancedOrderAccess(MongoClient mongoClient, JobLocker jobLocker) {
    this.mongoClient = mongoClient;
    this.jobLocker = jobLocker;
  }

  /**
   * Enqueues the order for immediate action.
   *
   * @param order The order.
   */
  @SuppressWarnings("unchecked")
  public <T extends AdvancedOrder> T insert(T order, Class<T> clazz) {
    JacksonDBCollection<T, org.bson.types.ObjectId> coll = collection(clazz);
    WriteResult<T, org.bson.types.ObjectId> result = coll.insert(order);
    org.bson.types.ObjectId savedId = result.getSavedId();
    return (T) order.toBuilder().id(savedId.toHexString()).build();
  }

  /**
   * Updates the order.
   *
   * @param order The order.
   */
  public <T extends AdvancedOrder> void update(T order, Class<T> clazz) {
    JacksonDBCollection<T, org.bson.types.ObjectId> coll = collection(clazz);
    coll.update(DBQuery.is("_id", order.id()), order);
  }

  @SuppressWarnings("unchecked")
  public <T extends AdvancedOrder> T load(String id) {
    JacksonDBCollection<AdvancedOrder, org.bson.types.ObjectId> coll = collection(AdvancedOrder.class);
    return (T) coll.findOneById(new ObjectId(id));
  }

  public Iterable<AdvancedOrder> list() {
    JacksonDBCollection<AdvancedOrder, org.bson.types.ObjectId> coll = collection(AdvancedOrder.class);
    return coll.find(new BasicDBObject());
  }

  public void delete(String orderId) {
    collection(AdvancedOrder.class).removeById(new ObjectId(orderId));
    jobLocker.releaseAnyLock(orderId);
  }


  public void delete() {
    collection(AdvancedOrder.class).remove(new BasicDBObject());
    jobLocker.releaseAllLocks();
  }

  private <T extends AdvancedOrder> JacksonDBCollection<T, ObjectId> collection(Class<T> clazz) {
    return JacksonDBCollection.wrap(mongoClient.getDB(DbModule.DB_NAME).getCollection("job"), clazz, org.bson.types.ObjectId.class);
  }
}