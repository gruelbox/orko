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

import static com.gruelbox.orko.jobrun.JobRecord.TABLE_NAME;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.gruelbox.orko.jobrun.spi.Job;
import java.io.IOException;
import java.util.List;
import javax.persistence.PersistenceException;
import org.hibernate.LockMode;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;

@Singleton
class JobAccessImpl implements JobAccess {

  private final ObjectMapper objectMapper;
  private final Provider<SessionFactory> sessionFactory;
  private final JobLocker joblocker;

  @Inject
  JobAccessImpl(
      Provider<SessionFactory> sessionFactory, ObjectMapper objectMapper, JobLocker joblocker) {
    this.sessionFactory = sessionFactory;
    this.objectMapper = objectMapper;
    this.joblocker = joblocker;
  }

  @Override
  public void insert(Job job) throws JobAlreadyExistsException {
    JobRecord record = new JobRecord(job.id(), encode(job), false);
    try {
      session().save(record);
      session().flush();
    } catch (NonUniqueObjectException e) {
      throw new JobAlreadyExistsException();
    } catch (PersistenceException e) {
      if (e.getCause() instanceof ConstraintViolationException) {
        throw new JobAlreadyExistsException();
      }
      throw e;
    }
  }

  @Override
  public void update(Job job) {
    JobRecord jobRecord = fetchAndLockRecord(job.id());
    jobRecord.setContent(encode(job));
    session().update(jobRecord);
  }

  @Override
  public Job load(String id) {
    return decode(fetchRecord(id).getContent());
  }

  @Override
  public Iterable<Job> list() {
    List<JobRecord> results =
        session()
            .createQuery("from " + TABLE_NAME + " where processed = false", JobRecord.class)
            .list();
    return FluentIterable.from(results).transform(JobRecord::getContent).transform(this::decode);
  }

  @Override
  public void delete(String jobId) {
    int updated =
        session()
            .createQuery(
                "update "
                    + TABLE_NAME
                    + " set processed = true where id = :id and processed = false")
            .setParameter("id", jobId)
            .executeUpdate();
    if (updated == 0) {
      throw new JobDoesNotExistException();
    }
    joblocker.releaseAnyLock(jobId);
  }

  @Override
  public void deleteAll() {
    session()
        .createQuery("update " + TABLE_NAME + " set processed = true where processed = false")
        .executeUpdate();
  }

  private Session session() {
    return sessionFactory.get().getCurrentSession();
  }

  private String encode(Job job) {
    try {
      return objectMapper.writeValueAsString(job);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private Job decode(String str) {
    try {
      return objectMapper.readValue(str, Job.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private JobRecord fetchAndLockRecord(String id) {
    JobRecord jobRecord = session().get(JobRecord.class, id, LockMode.PESSIMISTIC_WRITE);
    if (jobRecord == null || jobRecord.isProcessed()) {
      throw new JobDoesNotExistException();
    }
    return jobRecord;
  }

  private JobRecord fetchRecord(String id) {
    JobRecord jobRecord = session().get(JobRecord.class, id);
    if (jobRecord == null || jobRecord.isProcessed()) {
      throw new JobDoesNotExistException();
    }
    return jobRecord;
  }
}
