package com.grahamcrockford.orko.submit;

import static java.util.stream.Collectors.toList;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.index;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import java.io.IOException;
import java.util.Collection;

import org.alfasoftware.morf.metadata.DataType;
import org.alfasoftware.morf.metadata.Table;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.NoDataFoundException;
import org.jooq.impl.DSL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.grahamcrockford.orko.db.ConnectionSource;
import com.grahamcrockford.orko.spi.Job;

@Singleton
class JobAccessImpl implements JobAccess, TableContribution {
  
  private static final String JOB = "Job";
  private static final org.jooq.Table<Record> TABLE = DSL.table(JOB);
  private static final String ID = "id";
  private static final Field<String> ID_FIELD = DSL.field(ID, String.class);
  private static final String CONTENT = "content";
  private static final Field<String> CONTENT_FIELD = DSL.field(CONTENT, String.class);
  private static final String PROCESSED = "processed";
  private static final Field<Boolean> PROCESSED_FIELD = DSL.field(PROCESSED, Boolean.class);
  
  private ConnectionSource connectionSource;
  private ObjectMapper objectMapper;

  @Inject
  JobAccessImpl(ConnectionSource connectionSource, ObjectMapper objectMapper) {
    this.connectionSource = connectionSource;
    this.objectMapper = objectMapper;
  }
  
  @Override
  public void insert(Job job) throws JobAlreadyExistsException {
    MutableBoolean exists = new MutableBoolean();
    connectionSource.runInTransaction(dsl -> {
      try {
        dsl.insertInto(TABLE).values(job.id(), encode(job), false).execute();
      } catch (DataAccessException e) {
        if (!dsl.select(DSL.val(1)).from(TABLE).where(ID_FIELD.eq(job.id())).fetch().isEmpty()) {
          exists.setTrue();
        }
      }
    });
    if (exists.getValue())
      throw new JobAlreadyExistsException();
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

  @Override
  public void update(Job job) {
    int updated = connectionSource.getInTransaction(dsl -> dsl.update(TABLE).set(CONTENT_FIELD, encode(job)).where(ID_FIELD.eq(job.id())).execute());
    if (updated == 0) {
      throw new JobDoesNotExistException();
    }
  }

  @Override
  public Job load(String id) {
    try {
      return decode(connectionSource.getInTransaction(dsl -> dsl
          .select(CONTENT_FIELD)
          .from(TABLE)
          .where(ID_FIELD.eq(id).and(PROCESSED_FIELD.eq(false)))
          .fetchSingle(CONTENT_FIELD)
      ));
    } catch (NoDataFoundException e) {
      throw new JobDoesNotExistException();
    }
  }

  @Override
  public Iterable<Job> list() {
    return connectionSource.getInTransaction(dsl -> dsl
        .select(CONTENT_FIELD)
        .from(TABLE)
        .where(PROCESSED_FIELD.eq(false))
        .fetch(CONTENT_FIELD)
        .stream()
        .map(this::decode)
        .collect(toList())
    );
  }

  @Override
  public void delete(String jobId) {
    int updated = connectionSource.getInTransaction(dsl -> dsl.update(TABLE).set(PROCESSED_FIELD, true).where(ID_FIELD.eq(jobId)).execute());
    if (updated == 0) {
      throw new JobDoesNotExistException();
    }
  }

  @Override
  public void deleteAll() {
    connectionSource.runInTransaction(dsl -> dsl.update(TABLE).set(PROCESSED_FIELD, true).execute());
  }

  @Override
  public Collection<Table> tables() {
    return tablesStatic();
  }
  
  @VisibleForTesting
  static Collection<Table> tablesStatic() {
    return ImmutableList.of(
      table(JOB)
        .columns(
          column(ID, DataType.STRING, 45).primaryKey(),
          column(CONTENT, DataType.CLOB).nullable(),
          column(PROCESSED, DataType.BOOLEAN)
        )
        .indexes(
          index(JOB + "_1").columns(PROCESSED)
        )
    );
  }

  @Override
  public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
    return ImmutableList.of();
  }
}