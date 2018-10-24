package com.grahamcrockford.orko.db;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.mapdb.DB;
import org.mapdb.Serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.spi.Job;
import com.grahamcrockford.orko.spi.KeepAliveEvent;
import com.grahamcrockford.orko.submit.JobAccess;
import com.grahamcrockford.orko.submit.JobLocker;

/**
 * MapDb implementation of {@link JobAccess} and {@link JobLocker}, for single-node applications only.
 */
@Singleton
class MapDbJobAccess implements JobAccess, JobLocker {

  private final ConcurrentMap<String, String> jobs;
  private final Set<String> running = Sets.newConcurrentHashSet();
  private final Cache<String, UUID> locks = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).build();
  private final ObjectMapper objectMapper;
  private final DB db;

  @Inject
  MapDbJobAccess(EventBus eventBus, MapDbMakerFactory dbMakerFactory, ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.db = dbMakerFactory.create("jobs").make();
    this.jobs = db.hashMap("jobs", Serializer.STRING, Serializer.STRING).createOrOpen();
    eventBus.register(this);
  }

  @Subscribe
  void keepAlive(KeepAliveEvent keepAliveEvent) {
    locks.cleanUp();
  }

  @Override
  public void insert(Job job) throws JobAlreadyExistsException {
    if (jobs.putIfAbsent(job.id(), serialise(job)) != null) {
      throw new JobAlreadyExistsException();
    }
    running.add(job.id());
    db.commit();
  }

  @Override
  public void update(Job job) {
    if (!running.contains(job.id())) {
      throw new JobDoesNotExistException();
    }
    jobs.put(job.id(), serialise(job));
    db.commit();
  }

  @Override
  public Job load(String id) {
    if (!running.contains(id)) {
      throw new JobDoesNotExistException();
    }
    return deserialise(jobs.get(id));
  }

  @Override
  public Iterable<Job> list() {
    return FluentIterable.from(jobs.values()).transform(this::deserialise).filter(job -> running.contains(job.id())).toSet();
  }

  @Override
  public void delete(String jobId) {
    if (!running.remove(jobId)) {
      throw new JobDoesNotExistException();
    }
    db.commit();
    releaseAnyLock(jobId);
  }

  @Override
  public void deleteAll() {
    running.clear();
    db.commit();
    releaseAllLocks();
  }

  private String serialise(Job job) {
    try {
      return objectMapper.writeValueAsString(job);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private Job deserialise(String content) {
    try {
      return objectMapper.readValue(content, Job.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean attemptLock(String jobId, UUID uuid) {
    return locks.asMap().putIfAbsent(jobId, uuid) == null;
  }

  @Override
  public boolean updateLock(String jobId, UUID uuid) {
    if (!uuid.equals(locks.getIfPresent(jobId))) {
      return false;
    }
    locks.put(jobId, uuid);
    return true;
  }

  @Override
  public void releaseLock(String jobId, UUID uuid) {
    if (!locks.getIfPresent(jobId).equals(uuid)) {
      return;
    }
    releaseAnyLock(jobId);
  }

  @Override
  public void releaseAnyLock(String jobId) {
    locks.invalidate(jobId);
  }

  @Override
  public void releaseAllLocks() {
    locks.invalidateAll();
  }
}