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
package com.gruelbox.orko.job.script;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import org.hibernate.validator.constraints.Length;

/**
 * A parameter for a {@link Script}.
 *
 * @author Graham Crockford
 */
@Entity(name = ScriptParameter.TABLE_NAME)
class ScriptParameter {

  static final String TABLE_NAME = "ScriptParameter";
  static final String SCRIPT_ID_FIELD = "scriptId";
  static final String NAME_FIELD = "name";
  static final String DESCRIPTION_FIELD = "description";
  static final String DEFAULT_VALUE_FIELD = "defaultValue";
  static final String MANDATORY_FIELD = "mandatory";

  @Embeddable
  public static class Id implements Serializable {

    private static final long serialVersionUID = -1103388707546786957L;

    @Length(min = 1, max = 45)
    @Column(name = SCRIPT_ID_FIELD, nullable = false, updatable = false, insertable = true)
    private String scriptId;

    @Length(min = 1, max = 255)
    @Column(name = NAME_FIELD, nullable = false, updatable = false, insertable = true)
    private String name;

    public Id() {}

    public Id(String scriptId, String name) {
      super();
      this.scriptId = scriptId;
      this.name = name;
    }

    @Override
    public String toString() {
      return "ScriptParameter.Id [scriptId=" + scriptId + ", name=" + name + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((scriptId == null) ? 0 : scriptId.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      Id other = (Id) obj;
      if (name == null) {
        if (other.name != null) return false;
      } else if (!name.equals(other.name)) return false;
      if (scriptId == null) {
        if (other.scriptId != null) return false;
      } else if (!scriptId.equals(other.scriptId)) return false;
      return true;
    }
  }

  @EmbeddedId private final Id id = new Id();

  @JsonProperty
  @Length(min = 1, max = 255)
  @Column(name = DESCRIPTION_FIELD, nullable = false)
  private String description;

  @JsonProperty(value = "default")
  @Length(max = 255)
  @Column(name = DEFAULT_VALUE_FIELD, nullable = true)
  private String defaultValue;

  @JsonProperty
  @Column(name = MANDATORY_FIELD, nullable = false)
  private boolean mandatory;

  @JsonProperty
  public String scriptId() {
    return id.scriptId;
  }

  @JsonProperty
  public String name() {
    return id.name;
  }

  public String description() {
    return description;
  }

  public String defaultValue() {
    return defaultValue;
  }

  public boolean mandatory() {
    return mandatory;
  }

  @JsonProperty
  void setScriptId(String scriptId) {
    this.id.scriptId = scriptId;
  }

  @JsonProperty
  void setName(String name) {
    this.id.name = name;
  }

  void setDescription(String description) {
    this.description = description;
  }

  void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }

  void setParent(Script script) {
    this.id.scriptId = script.id();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + id.hashCode();
    result = prime * result + (mandatory ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ScriptParameter other = (ScriptParameter) obj;
    if (defaultValue == null) {
      if (other.defaultValue != null) return false;
    } else if (!defaultValue.equals(other.defaultValue)) return false;
    if (description == null) {
      if (other.description != null) return false;
    } else if (!description.equals(other.description)) return false;
    if (!id.equals(other.id)) return false;
    if (mandatory != other.mandatory) return false;
    return true;
  }

  @Override
  public String toString() {
    return "ScriptParameter [id="
        + id
        + ", description="
        + description
        + ", defaultValue="
        + defaultValue
        + ", mandatory="
        + mandatory
        + "]";
  }
}
