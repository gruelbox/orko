package com.gruelbox.orko.jobrun;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.index;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.alfasoftware.morf.metadata.DataType;
import org.alfasoftware.morf.metadata.Table;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.db.ConnectionSource;
import com.gruelbox.orko.jobrun.spi.JobRunConfiguration;
import com.gruelbox.orko.util.SafelyDispose;

import io.dropwizard.lifecycle.Managed;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Singleton
class JobLockerImpl implements JobLocker, Managed, TableContribution {

  private static final String JOB_LOCK = "JobLock";
  private static final org.jooq.Table<Record> TABLE = DSL.table(JOB_LOCK);

  private static final String OWNER_ID = "ownerId";
  private static final Field<Object> OWNER_ID_FIELD = DSL.field(OWNER_ID);

  private static final String JOB_ID = "jobId";
  private static final Field<Object> JOB_ID_FIELD = DSL.field(JOB_ID);

  private static final String EXPIRES = "expires";
  private static final Field<Long> EXPIRES_FIELD = DSL.field(EXPIRES, Long.class);

  private final JobRunConfiguration configuration;
  private final ConnectionSource connectionSource;

  private Disposable interval;

  @Inject
  JobLockerImpl(JobRunConfiguration configuration, ConnectionSource connectionSource) {
    this.configuration = configuration;
    this.connectionSource = connectionSource;
  }

  @Override
  public void start() throws Exception {
    interval = Observable.interval(configuration.getGuardianLoopSeconds(), TimeUnit.SECONDS)
        .observeOn(Schedulers.single())
        .subscribe(x -> cleanup(now()));
  }

  @VisibleForTesting
  void cleanup(LocalDateTime localDateTime) {
    long expiry = localDateTime.toEpochSecond(UTC);
    connectionSource.withNewConnection(dsl -> dsl.deleteFrom(TABLE).where(EXPIRES_FIELD.lessOrEqual(expiry)).execute());
  }

  @Override
  public void stop() throws Exception {
    SafelyDispose.of(interval);
  }

  @Override
  public boolean attemptLock(String jobId, UUID uuid) {
    return attemptLock(jobId, uuid, LocalDateTime.now());
  }

  @VisibleForTesting
  boolean attemptLock(String jobId, UUID uuid, LocalDateTime dateTime) {
    try {
      return connectionSource.getWithNewConnection(dsl -> dsl.insertInto(TABLE).values(jobId, uuid.toString(), newExpiryDate(dateTime)).execute()) == 1;
    } catch (DataAccessException e) {
      return false;
    }
  }

  @Override
  public boolean updateLock(String jobId, UUID uuid) {
    return updateLock(jobId, uuid, LocalDateTime.now());
  }

  @VisibleForTesting
  boolean updateLock(String jobId, UUID uuid, LocalDateTime dateTime) {
    return connectionSource.getWithNewConnection(dsl -> dsl.update(TABLE).set(EXPIRES_FIELD, newExpiryDate(dateTime)).where(fullKeyMatch(jobId, uuid)).execute()) != 0;
  }

  private long newExpiryDate(LocalDateTime dateTime) {
    return dateTime.plusSeconds(configuration.getDatabaseLockSeconds()).toEpochSecond(UTC);
  }

  @Override
  public void releaseLock(String jobId, UUID uuid) {
    connectionSource.withNewConnection(dsl -> dsl.delete(TABLE).where(fullKeyMatch(jobId, uuid)).execute());
  }

  private Condition fullKeyMatch(String jobId, UUID uuid) {
    return JOB_ID_FIELD.eq(jobId).and(OWNER_ID_FIELD.eq(uuid.toString()));
  }

  @Override
  public void releaseAnyLock(String jobId) {
    connectionSource.withNewConnection(dsl -> dsl.delete(TABLE).where(JOB_ID_FIELD.eq(jobId)).execute());
  }

  @Override
  public void releaseAllLocks() {
    connectionSource.withNewConnection(dsl -> dsl.delete(TABLE).execute());
  }

  @Override
  public Collection<Table> tables() {
    return tablesStatic();
  }

  @VisibleForTesting
  static Collection<Table> tablesStatic() {
    return ImmutableList.of(
      table(JOB_LOCK)
        .columns(
          column(JOB_ID, DataType.STRING, 45).primaryKey(),
          column(OWNER_ID, DataType.STRING, 255),
          column(EXPIRES, DataType.BIG_INTEGER)
        )
        .indexes(
          index(JOB_LOCK + "_1").columns(OWNER_ID)
        )
    );
  }

  @Override
  public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
    return ImmutableList.of();
  }
}