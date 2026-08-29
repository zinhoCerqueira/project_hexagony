package com.schoolqueue.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "parent_student")
public class ParentStudentEntity {

  @EmbeddedId private ParentStudentId id;

  protected ParentStudentEntity() {}

  public ParentStudentEntity(UUID parentId, UUID studentId) {
    this.id = new ParentStudentId(parentId, studentId);
  }

  public ParentStudentId getId() {
    return id;
  }

  public UUID getParentId() {
    return id.parentId;
  }

  public UUID getStudentId() {
    return id.studentId;
  }

  @Embeddable
  public static class ParentStudentId implements Serializable {

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "student_id")
    private UUID studentId;

    protected ParentStudentId() {}

    public ParentStudentId(UUID parentId, UUID studentId) {
      this.parentId = parentId;
      this.studentId = studentId;
    }

    public UUID getParentId() {
      return parentId;
    }

    public UUID getStudentId() {
      return studentId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ParentStudentId other)) return false;
      return Objects.equals(parentId, other.parentId) && Objects.equals(studentId, other.studentId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(parentId, studentId);
    }
  }
}
