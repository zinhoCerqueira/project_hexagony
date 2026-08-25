package com.schoolqueue.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class School {
  private final UUID id;
  private String name;
  private BigDecimal latitude;
  private BigDecimal longitude;

  public School(UUID id, String name, BigDecimal latitude, BigDecimal longitude) {
    if (latitude == null || longitude == null) {
      throw new IllegalArgumentException("School must have GPS coordinates");
    }
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
    if (latitude == null) {
      throw new IllegalArgumentException("School must have GPS coordinates");
    }
    this.latitude = latitude;
  }

  public BigDecimal longitude() {
    return longitude;
  }

  public void setLongitude(BigDecimal longitude) {
    if (longitude == null) {
      throw new IllegalArgumentException("School must have GPS coordinates");
    }
    this.longitude = longitude;
  }
}
