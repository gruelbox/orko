package com.gruelbox.orko.auth.ipwhitelisting;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Persistence object for IP whitelisting.
 *
 * @author Graham Crockford
 */
@Entity(name = IpWhitelist.TABLE_NAME)
final class IpWhitelist {

  static final String IP = "ip";
  static final String EXPIRES = "expires";
  static final String TABLE_NAME = "IpWhitelist";

  @Id
  @Column(name = IP, nullable = false)
  @NotNull
  @JsonProperty
  String ip;

  @Column(name = EXPIRES, nullable = false)
  @NotNull
  @JsonProperty
  long expires;

  IpWhitelist() {

  }

  IpWhitelist(String ip, long expires) {
    super();
    this.ip = ip;
    this.expires = expires;
  }
}