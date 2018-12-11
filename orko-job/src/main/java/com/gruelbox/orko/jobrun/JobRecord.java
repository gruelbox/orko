package com.gruelbox.orko.jobrun;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Persistence object for jobs.
 *
 * @author Graham Crockford
 */
@Entity(name = JobRecord.TABLE_NAME)
final class JobRecord {

  static final String TABLE_NAME = "Job";
  static final String ID = "id";
  static final String CONTENT = "content";
  static final String PROCESSED = "processed";

  @Id
  @Column(name = ID, nullable = false)
  @NotNull
  @JsonProperty
  private String id;

  @Column(name = CONTENT, nullable = false)
  @NotNull
  @JsonProperty
  private String content;

  @Column(name = PROCESSED, nullable = false)
  @NotNull
  @JsonProperty
  private boolean processed;

  JobRecord() {
    // Nothing to do
  }

  JobRecord(String id, String content, boolean processed) {
    super();
    this.id = id;
    this.content = content;
    this.processed = processed;
  }

  String getContent() {
    return content;
  }

  void setContent(String content) {
    this.content = content;
  }

  boolean isProcessed() {
    return processed;
  }

  void setProcessed(boolean processed) {
    this.processed = processed;
  }
}