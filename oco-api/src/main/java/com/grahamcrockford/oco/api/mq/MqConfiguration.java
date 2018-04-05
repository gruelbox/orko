package com.grahamcrockford.oco.api.mq;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class MqConfiguration extends Configuration {

  @NotEmpty
  @JsonProperty
  private String host;

  String getHost() {
    return host;
  }

}