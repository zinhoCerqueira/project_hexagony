package com.schoolqueue.domain.model;

import java.util.UUID;

public class Classroom {
  private final UUID id;
  private final UUID schoolId;
  private final String name;

  public Classroom(UUID id, UUID schoolId, String name) {
    this.id = id != null ? id : UUID.randomUUID();
    this.schoolId = schoolId;
    this.name = name;
  }

  public UUID id() {
    return id;
  }

  public UUID schoolId() {
    return schoolId;
  }

  public String name() {
    return name;
  }
}
