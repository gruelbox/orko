package com.gruelbox.orko.job.script;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A reusable template for a {@link ScriptJob}.
 *
 * @author Graham Crockford
 */
@Entity(name = Script.TABLE_NAME)
class Script {

  static final String TABLE_NAME = "Script";
  static final String ID = "id";
  static final String NAME = "name";
  static final String SCRIPT = "script";
  static final String SCRIPT_HASH = "scriptHash";

  @Id
  @JsonProperty
  @Length(min = 1, max = 45)
  @Column(name = ID, nullable = false, updatable=false)
  private String id;

  @JsonProperty
  @Length(min = 1, max = 255)
  @Column(name = NAME, nullable = false)
  private String name;

  @JsonProperty
  @Length(min = 1)
  @Column(name = SCRIPT, nullable = false)
  private String script;

  @JsonProperty
  @Length(min = 1, max = 255)
  @Column(name = SCRIPT_HASH, nullable = false)
  private String scriptHash;

  @JsonProperty
  @OneToMany(mappedBy="parent", fetch=FetchType.EAGER)
  private List<ScriptParameter> parameters;

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

  public List<ScriptParameter> parameters() {
    return parameters;
  }

  void setParameters(List<ScriptParameter> parameters) {
    this.parameters = parameters;
  }

  @Override
  public String toString() {
    return "Script{"
         + "id=" + id + ", "
         + "name=" + name + ", "
         + "script=" + script + ", "
         + "scriptHash=" + scriptHash + ", "
         + "parameters=" + parameters
        + "}";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
    result = prime * result + ((script == null) ? 0 : script.hashCode());
    result = prime * result + ((scriptHash == null) ? 0 : scriptHash.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Script other = (Script) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (parameters == null) {
      if (other.parameters != null)
        return false;
    } else if (!parameters.equals(other.parameters))
      return false;
    if (script == null) {
      if (other.script != null)
        return false;
    } else if (!script.equals(other.script))
      return false;
    if (scriptHash == null) {
      if (other.scriptHash != null)
        return false;
    } else if (!scriptHash.equals(other.scriptHash))
      return false;
    return true;
  }
}