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

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * Persistence object for jobs.
 *
 * @author Graham Crockford
 */
@Entity(name = JobRecord.TABLE_NAME)
final class JobRecord {

  static final String TABLE_NAME = "Job";
  static final String ID_FIELD = "id";
  static final String CONTENT_FIELD = "content";
  static final String PROCESSED_FIELD = "processed";

  @Id
  @Column(name = ID_FIELD, nullable = false)
  @NotNull
  @JsonProperty
  private String id;

  @Column(name = CONTENT_FIELD, nullable = false)
  @NotNull
  @JsonProperty
  private String content;

  @Column(name = PROCESSED_FIELD, nullable = false)
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
