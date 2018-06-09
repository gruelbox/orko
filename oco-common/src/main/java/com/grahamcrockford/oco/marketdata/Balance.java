package com.grahamcrockford.oco.marketdata;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Balance {

  public static Balance create(org.knowm.xchange.dto.account.Balance source) {
    return new AutoValue_Balance(source.getCurrency().getCurrencyCode(), source.getTotal(), source.getAvailable());
  }

  @JsonIgnore public abstract String currency();
  @JsonProperty public abstract BigDecimal total();
  @JsonProperty public abstract BigDecimal available();
}