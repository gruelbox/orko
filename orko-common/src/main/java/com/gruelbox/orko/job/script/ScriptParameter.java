package com.gruelbox.orko.job.script;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A parameter for a {@link Script}.
 *
 * @author Graham Crockford
 */
@Entity(name = ScriptParameter.TABLE_NAME)
class ScriptParameter {

  static final String TABLE_NAME = "ScriptParameter";
  static final String SCRIPT_ID = "scriptId";
  static final String NAME = "name";
  static final String DESCRIPTION = "description";
  static final String DEFAULT_VALUE = "defaultValue";
  static final String MANDATORY = "mandatory";

  @Embeddable
  public static class Id implements Serializable {

    private static final long serialVersionUID = -1103388707546786957L;

    @Length(min = 1, max = 45)
    @Column(name = SCRIPT_ID, nullable = false, updatable=false)
    private String scriptId;

    @Length(min = 1, max = 255)
    @Column(name = NAME, nullable = false, updatable=false)
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
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Id other = (Id) obj;
      if (name == null) {
        if (other.name != null)
          return false;
      } else if (!name.equals(other.name))
        return false;
      if (scriptId == null) {
        if (other.scriptId != null)
          return false;
      } else if (!scriptId.equals(other.scriptId))
        return false;
      return true;
    }
  }

  @EmbeddedId
  private final Id id = new Id();

  @JsonProperty
  @Length(min = 1, max = 255)
  @Column(name = DESCRIPTION, nullable = false)
  private String description;

  @JsonProperty(value = "default")
  @Length(min = 1, max = 255)
  @Column(name = DEFAULT_VALUE, nullable = false)
  private String defaultValue;

  @JsonProperty
  @Column(name = MANDATORY, nullable = false)
  private boolean mandatory;

  @ManyToOne
  @JoinColumn(name=SCRIPT_ID, insertable = false, updatable = false)
  private Script parent;

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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + (mandatory ? 1231 : 1237);
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
    ScriptParameter other = (ScriptParameter) obj;
    if (defaultValue == null) {
      if (other.defaultValue != null)
        return false;
    } else if (!defaultValue.equals(other.defaultValue))
      return false;
    if (description == null) {
      if (other.description != null)
        return false;
    } else if (!description.equals(other.description))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (mandatory != other.mandatory)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ScriptParameter [id=" + id + ", description=" + description + ", defaultValue=" + defaultValue
        + ", mandatory=" + mandatory + "]";
  }
}