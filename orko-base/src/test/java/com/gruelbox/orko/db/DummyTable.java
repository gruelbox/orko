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
package com.gruelbox.orko.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity(name = DummyTable.TABLE_NAME)
final class DummyTable {

  static final String ID = "id";
  static final String FIELD_1 = "field1";
  static final String TABLE_NAME = "DummyTable";

  @Id
  @Column(name = ID, nullable = false)
  @NotNull
  @JsonProperty
  private String ip;

  @Column(name = FIELD_1, nullable = false)
  @NotNull
  @JsonProperty
  private long field1;

  DummyTable() {
    // Nothing to do
  }

  DummyTable(String ip, long field1) {
    super();
    this.ip = ip;
    this.field1 = field1;
  }
}
