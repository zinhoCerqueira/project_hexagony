package com.schoolqueue.infrastructure.adapters.out.persistence.mapper;

import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.ClassroomEntity;

public final class ClassroomEntityMapper {

  private ClassroomEntityMapper() {}

  public static ClassroomEntity toEntity(Classroom classroom) {
    return new ClassroomEntity(classroom.id(), classroom.schoolId(), classroom.name());
  }

  public static Classroom toDomain(ClassroomEntity entity) {
    return new Classroom(entity.getId(), entity.getSchoolId(), entity.getName());
  }
}
