package com.schoolqueue.infrastructure.adapters.out.persistence.mapper;

import com.schoolqueue.domain.model.Student;
import com.schoolqueue.infrastructure.adapters.out.persistence.entity.StudentEntity;

public final class StudentEntityMapper {

  private StudentEntityMapper() {}

  public static StudentEntity toEntity(Student student) {
    return new StudentEntity(
        student.id(), student.schoolId(), student.classroomId(), student.name());
  }

  public static Student toDomain(StudentEntity entity) {
    return new Student(
        entity.getId(), entity.getSchoolId(), entity.getClassroomId(), entity.getName());
  }
}
