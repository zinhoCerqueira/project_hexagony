package com.schoolqueue.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "students")
public class StudentEntity {

  @Id private UUID id;

  @Column(name = "school_id")
  private UUID schoolId;

  @Column(name = "classroom_id")
  private UUID classroomId;

  private String name;

  protected StudentEntity() {}

  public StudentEntity(UUID id, UUID schoolId, UUID classroomId, String name) {
    this.id = id;
    this.schoolId = schoolId;
    this.classroomId = classroomId;
    this.name = name;
  }

  public UUID getId() {
    return id;
  }

  public UUID getSchoolId() {
    return schoolId;
  }

  public void setSchoolId(UUID schoolId) {
    this.schoolId = schoolId;
  }

  public UUID getClassroomId() {
    return classroomId;
  }

  public void setClassroomId(UUID classroomId) {
    this.classroomId = classroomId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
