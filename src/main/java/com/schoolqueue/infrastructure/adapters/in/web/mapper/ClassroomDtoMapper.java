package com.schoolqueue.infrastructure.adapters.in.web.mapper;

import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.domain.ports.in.RegisterClassroomUseCase.RegisterClassroomCommand;
import com.schoolqueue.domain.ports.in.UpdateClassroomUseCase.UpdateClassroomCommand;
import com.schoolqueue.infrastructure.adapters.in.web.dto.ClassroomResponse;
import com.schoolqueue.infrastructure.adapters.in.web.dto.RegisterClassroomRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.UpdateClassroomRequest;
import java.util.UUID;

public final class ClassroomDtoMapper {

  private ClassroomDtoMapper() {}

  public static RegisterClassroomCommand toCommand(RegisterClassroomRequest request) {
    return new RegisterClassroomCommand(request.schoolId(), request.name());
  }

  public static UpdateClassroomCommand toCommand(UUID id, UpdateClassroomRequest request) {
    return new UpdateClassroomCommand(id, request.schoolId(), request.name());
  }

  public static ClassroomResponse toResponse(Classroom classroom) {
    return new ClassroomResponse(classroom.id(), classroom.schoolId(), classroom.name());
  }
}
