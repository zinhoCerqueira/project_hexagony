package com.schoolqueue.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "classrooms")
public class ClassroomEntity {

  @Id private UUID id;

  @Column(name = "school_id")
  private UUID schoolId;

  private String name;

  protected ClassroomEntity() {}

  public ClassroomEntity(UUID id, UUID schoolId, String name) {
    this.id = id;
    this.schoolId = schoolId;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
