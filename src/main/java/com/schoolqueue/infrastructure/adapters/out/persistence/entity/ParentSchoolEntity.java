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
@Table(name = "parent_school")
public class ParentSchoolEntity {

  @EmbeddedId private ParentSchoolId id;

  protected ParentSchoolEntity() {}

  public ParentSchoolEntity(UUID parentId, UUID schoolId) {
    this.id = new ParentSchoolId(parentId, schoolId);
  }

  public ParentSchoolId getId() {
    return id;
  }

  public UUID getParentId() {
    return id.parentId;
  }

  public UUID getSchoolId() {
    return id.schoolId;
  }

  @Embeddable
  public static class ParentSchoolId implements Serializable {

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "school_id")
    private UUID schoolId;

    protected ParentSchoolId() {}

    public ParentSchoolId(UUID parentId, UUID schoolId) {
      this.parentId = parentId;
      this.schoolId = schoolId;
    }

    public UUID getParentId() {
      return parentId;
    }

    public UUID getSchoolId() {
      return schoolId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ParentSchoolId other)) return false;
      return Objects.equals(parentId, other.parentId) && Objects.equals(schoolId, other.schoolId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(parentId, schoolId);
    }
  }
}
