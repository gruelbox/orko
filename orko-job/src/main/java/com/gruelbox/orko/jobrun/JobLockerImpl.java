/**
 * Orko - Copyright © 2018-2019 Graham Crockford
 *
 * <p>This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.gruelbox.orko.jobrun;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.orko.db.ConnectionSource;
import com.gruelbox.orko.db.Transactionally;
import com.gruelbox.orko.jobrun.spi.JobRunConfiguration;
import com.gruelbox.orko.util.SafelyDispose;
import io.dropwizard.lifecycle.Managed;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class JobLockerImpl implements JobLocker, Managed {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobLockerImpl.class);

  private static final org.jooq.Table<Record> TABLE = DSL.table(JobLockContribution.TABLE_NAME);
  private static final Field<Object> OWNER_ID_FIELD = DSL.field(JobLockContribution.OWNER_ID);
  private static final Field<Object> JOB_ID_FIELD = DSL.field(JobLockContribution.JOB_ID);
  private static final Field<Long> EXPIRES_FIELD =
      DSL.field(JobLockContribution.EXPIRES, Long.class);

  private final JobRunConfiguration configuration;

  private Disposable interval;
  private final Transactionally transactionally;
  private final ConnectionSource connectionSource;

  @Inject
  JobLockerImpl(
      JobRunConfiguration configuration,
      ConnectionSource connectionSource,
      Transactionally transactionally) {
    this.configuration = configuration;
    this.connectionSource = connectionSource;
    this.transactionally = transactionally;
  }

  @Override
  public void start() throws Exception {
    interval =
        Observable.interval(configuration.getGuardianLoopSeconds(), TimeUnit.SECONDS)
            .subscribe(x -> transactionally.run(() -> cleanup(now())));
  }

  @VisibleForTesting
  void cleanup(LocalDateTime localDateTime) {
    long expiry = localDateTime.toEpochSecond(UTC);
    int deleted =
        connectionSource.getWithCurrentConnection(
            dsl -> dsl.deleteFrom(TABLE).where(EXPIRES_FIELD.lessOrEqual(expiry)).execute());
    if (deleted != 0) {
      LOGGER.info("Expired {} locks on active jobs", deleted);
    }
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
      LOGGER.debug("Attempting to lock {} for {}", jobId, uuid);
      boolean result =
          connectionSource.getWithCurrentConnection(
                  dsl ->
                      dsl.insertInto(TABLE)
                          .values(jobId, uuid.toString(), newExpiryDate(dateTime))
                          .execute())
              == 1;
      if (result) {
        LOGGER.debug("Locked {} for {}", jobId, uuid);
      } else {
        LOGGER.debug("Failed to lock {} for {}", jobId, uuid);
      }
      return result;
    } catch (DataAccessException e) {
      LOGGER.debug("Failed to lock {} for {}", jobId, uuid);
      return false;
    }
  }

  @Override
  public boolean updateLock(String jobId, UUID uuid) {
    return updateLock(jobId, uuid, LocalDateTime.now());
  }

  @VisibleForTesting
  boolean updateLock(String jobId, UUID uuid, LocalDateTime dateTime) {
    LOGGER.debug("Updating lock on {} for {}", jobId, uuid);
    return connectionSource.getWithCurrentConnection(
            dsl ->
                dsl.update(TABLE)
                    .set(EXPIRES_FIELD, newExpiryDate(dateTime))
                    .where(fullKeyMatch(jobId, uuid))
                    .execute())
        != 0;
  }

  private long newExpiryDate(LocalDateTime dateTime) {
    return dateTime.plusSeconds(configuration.getDatabaseLockSeconds()).toEpochSecond(UTC);
  }

  @Override
  public void releaseLock(String jobId, UUID uuid) {
    LOGGER.debug("Releasing lock on {} by {}", jobId, uuid);
    connectionSource.withCurrentConnection(
        dsl -> dsl.delete(TABLE).where(fullKeyMatch(jobId, uuid)).execute());
  }

  private Condition fullKeyMatch(String jobId, UUID uuid) {
    return JOB_ID_FIELD.eq(jobId).and(OWNER_ID_FIELD.eq(uuid.toString()));
  }

  @Override
  public void releaseAnyLock(String jobId) {
    connectionSource.withCurrentConnection(
        dsl -> dsl.delete(TABLE).where(JOB_ID_FIELD.eq(jobId)).execute());
  }

  @Override
  public void releaseAllLocks() {
    connectionSource.withCurrentConnection(dsl -> dsl.delete(TABLE).execute());
  }
}
