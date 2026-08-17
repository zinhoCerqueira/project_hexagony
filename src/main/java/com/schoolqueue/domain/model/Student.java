package com.schoolqueue.domain.model;

import java.util.UUID;

public class Student {
  private final UUID id;
  private UUID schoolId;
  private UUID classroomId;
  private String name;

  public Student(UUID id, UUID schoolId, UUID classroomId, String name) {
    this.id = id != null ? id : UUID.randomUUID();
    this.schoolId = schoolId;
    this.classroomId = classroomId;
    this.name = name;
  }

  public UUID id() {
    return id;
  }

  public UUID schoolId() {
    return schoolId;
  }

  public UUID classroomId() {
    return classroomId;
  }

  public String name() {
    return name;
  }

  public void setSchoolId(UUID schoolId) {
    this.schoolId = schoolId;
  }

  public void setClassroomId(UUID classroomId) {
    this.classroomId = classroomId;
  }

  public void setName(String name) {
    this.name = name;
  }
}
