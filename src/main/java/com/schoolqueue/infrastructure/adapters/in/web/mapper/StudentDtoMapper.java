package com.schoolqueue.infrastructure.adapters.in.web.mapper;

import com.schoolqueue.domain.model.Student;
import com.schoolqueue.domain.ports.in.RegisterStudentUseCase.RegisterStudentCommand;
import com.schoolqueue.domain.ports.in.UpdateStudentUseCase.UpdateStudentCommand;
import com.schoolqueue.domain.ports.out.ParentStudentLinkRepositoryPort;
import com.schoolqueue.infrastructure.adapters.in.web.dto.RegisterStudentRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.StudentResponse;
import com.schoolqueue.infrastructure.adapters.in.web.dto.UpdateStudentRequest;
import java.util.UUID;

public final class StudentDtoMapper {

  private StudentDtoMapper() {}

  public static RegisterStudentCommand toCommand(RegisterStudentRequest request) {
    return new RegisterStudentCommand(
        request.schoolId(), request.classroomId(), request.name(), request.parentIds());
  }

  public static UpdateStudentCommand toCommand(UUID id, UpdateStudentRequest request) {
    return new UpdateStudentCommand(
        id, request.schoolId(), request.classroomId(), request.name(), request.parentIds());
  }

  public static StudentResponse toResponse(Student student, ParentStudentLinkRepositoryPort linkPort) {
    return new StudentResponse(
        student.id(),
        student.schoolId(),
        student.classroomId(),
        student.name(),
        linkPort.findParentsOfStudent(student.id()));
  }
}
