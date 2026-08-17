package com.schoolqueue.domain.model;

import java.util.UUID;

public class School {
  private final UUID id;
  private final String name;

  public School(UUID id, String name) {
    this.id = id != null ? id : UUID.randomUUID();
    this.name = name;
  }

  public UUID id() {
    return id;
  }

  public String name() {
    return name;
  }
}
