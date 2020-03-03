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
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import org.hibernate.validator.constraints.Length;

/**
 * A reusable template for a {@link ScriptJob}.
 *
 * @author Graham Crockford
 */
@Entity(name = Script.TABLE_NAME)
class Script {

  static final String TABLE_NAME = "Script";
  static final String ID_FIELD = "id";
  static final String NAME_FIELD = "name";
  static final String SCRIPT_FIELD = "script";
  static final String SCRIPT_HASH_FIELD = "scriptHash";

  @Id
  @JsonProperty
  @Length(min = 1, max = 45)
  @Column(name = ID_FIELD, nullable = false, updatable = false)
  private String id;

  @JsonProperty
  @Length(min = 1, max = 255)
  @Column(name = NAME_FIELD, nullable = false)
  private String name;

  @JsonProperty
  @Length(min = 1)
  @Column(name = SCRIPT_FIELD, nullable = false)
  private String script;

  @JsonProperty
  @Length(min = 1, max = 255)
  @Column(name = SCRIPT_HASH_FIELD, nullable = false)
  private String scriptHash;

  // Hibernate's associations really don't handle stateless disconnected
  // POJOs well. For the time being, hitting abort and dealing with the child
  // collection manually.
  @Transient
  //  @OneToMany(mappedBy="parent", fetch = FetchType.EAGER, cascade = CascadeType.ALL,
  // orphanRemoval = true)
  private List<ScriptParameter> params = new ArrayList<>();

  public String id() {
    return id;
  }

  void setId(String id) {
    this.id = id;
  }

  public String name() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  public String script() {
    return script;
  }

  void setScript(String script) {
    this.script = script;
  }

  public String scriptHash() {
    return scriptHash;
  }

  void setScriptHash(String scriptHash) {
    this.scriptHash = scriptHash;
  }

  @JsonProperty
  public List<ScriptParameter> parameters() {
    return params;
  }

  @JsonProperty
  void setParameters(List<ScriptParameter> parameters) {
    this.params = parameters;
  }

  @Override
  public String toString() {
    return "Script{"
        + "id="
        + id
        + ", "
        + "name="
        + name
        + ", "
        + "script="
        + script
        + ", "
        + "scriptHash="
        + scriptHash
        + ", "
        + "parameters="
        + params
        + "}";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((params == null) ? 0 : params.hashCode());
    result = prime * result + ((script == null) ? 0 : script.hashCode());
    result = prime * result + ((scriptHash == null) ? 0 : scriptHash.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Script other = (Script) obj;
    if (id == null) {
      if (other.id != null) return false;
    } else if (!id.equals(other.id)) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (params == null) {
      if (other.params != null) return false;
    } else if (!params.equals(other.params)) return false;
    if (script == null) {
      if (other.script != null) return false;
    } else if (!script.equals(other.script)) return false;
    if (scriptHash == null) {
      if (other.scriptHash != null) return false;
    } else if (!scriptHash.equals(other.scriptHash)) return false;
    return true;
  }
}
