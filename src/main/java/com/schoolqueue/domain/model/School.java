package com.schoolqueue.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class School {
  private final UUID id;
  private String name;
  private BigDecimal latitude;
  private BigDecimal longitude;

  public School(UUID id, String name, BigDecimal latitude, BigDecimal longitude) {
    this.id = id != null ? id : UUID.randomUUID();
    this.name = name;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public UUID id() {
    return id;
  }

  public String name() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BigDecimal latitude() {
    return latitude;
  }

  public void setLatitude(BigDecimal latitude) {
    this.latitude = latitude;
  }

  public BigDecimal longitude() {
    return longitude;
  }

  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
  }
}
