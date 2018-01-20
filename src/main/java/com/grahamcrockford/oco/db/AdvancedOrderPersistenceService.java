package com.grahamcrockford.oco.db;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

import org.mapdb.DB;
import org.mapdb.Serializer;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provider;
import com.grahamcrockford.oco.api.AdvancedOrder;

public class AdvancedOrderPersistenceService {

  private final Provider<DB> dbProvider;
  private final JsonSerializer<AdvancedOrder> jsonSerializer;

  @Inject
  AdvancedOrderPersistenceService(Provider<DB> dbProvider, JsonSerializer.Factory serializerFactory) {
    this.dbProvider = dbProvider;
    this.jsonSerializer = serializerFactory.create(AdvancedOrder.class);
  }

  public Collection<AdvancedOrder> listJobs() {
    try (DB db = dbProvider.get()) {
      return ImmutableList.copyOf(jobs(db).values());
    }
  }

  public void deleteJob(long id) {
    try (DB db = dbProvider.get()) {
      jobs(db).remove(id);
    }
  }

  public AdvancedOrder getJob(long id) {
    try (DB db = dbProvider.get()) {
      return jobs(db).get(id);
    }
  }

  public <T extends AdvancedOrder> T saveJob(T order) throws IOException {
    try (DB db = dbProvider.get()) {
      jobs(db).put(order.id(), order);
    }
    return order;
  }

  public long newJobId() {
    try (DB db = dbProvider.get()) {
      return nextId(db);
    }
  }

  private long nextId(DB db) {
    return db.atomicLong("orderId").createOrOpen().incrementAndGet();
  }

  private ConcurrentMap<Long, AdvancedOrder> jobs(DB db) {
    return db.hashMap("order", Serializer.LONG, jsonSerializer).createOrOpen();
  }
}