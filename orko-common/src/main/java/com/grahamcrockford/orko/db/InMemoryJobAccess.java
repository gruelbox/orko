package com.grahamcrockford.orko.db;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

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

import jersey.repackaged.com.google.common.collect.Maps;

/**
 * In-memory implementation of {@link JobAccess} and {@link JobLocker}, so you can try
 * the application out without having to set up Mongo.
 */
@Singleton
class InMemoryJobAccess implements JobAccess, JobLocker {

  private final ConcurrentMap<String, Job> jobs = Maps.newConcurrentMap();
  private final Set<String> running = Sets.newConcurrentHashSet();
  private final Cache<String, UUID> locks = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).build();

  @Inject
  InMemoryJobAccess(EventBus eventBus) {
    eventBus.register(this);
  }

  @Subscribe
  void keepAlive(KeepAliveEvent keepAliveEvent) {
    locks.cleanUp();
  }

  @Override
  public void insert(Job job) throws JobAlreadyExistsException {
    if (jobs.putIfAbsent(job.id(), job) != null) {
      throw new JobAlreadyExistsException();
    }
    running.add(job.id());
  }

  @Override
  public void update(Job job) {
    if (!running.contains(job.id())) {
      throw new JobDoesNotExistException();
    }
    jobs.put(job.id(), job);
  }

  @Override
  public Job load(String id) {
    if (!running.contains(id)) {
      throw new JobDoesNotExistException();
    }
    return jobs.get(id);
  }

  @Override
  public Iterable<Job> list() {
    return FluentIterable.from(jobs.values()).filter(job -> running.contains(job.id())).toSet();
  }

  @Override
  public void delete(String jobId) {
    if (!running.remove(jobId)) {
      throw new JobDoesNotExistException();
    }
    releaseAnyLock(jobId);
  }

  @Override
  public void deleteAll() {
    running.clear();
    releaseAllLocks();
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