package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.domain.ports.in.RegisterClassroomUseCase;
import com.schoolqueue.domain.ports.in.RegisterClassroomUseCase.RegisterClassroomCommand;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;

public class RegisterClassroomService implements RegisterClassroomUseCase {

  private final SchoolRepositoryPort schoolRepositoryPort;
  private final com.schoolqueue.domain.ports.out.ClassroomRepositoryPort classroomRepositoryPort;

  public RegisterClassroomService(
      SchoolRepositoryPort schoolRepositoryPort,
      com.schoolqueue.domain.ports.out.ClassroomRepositoryPort classroomRepositoryPort) {
    this.schoolRepositoryPort = schoolRepositoryPort;
    this.classroomRepositoryPort = classroomRepositoryPort;
  }

  @Override
  public Classroom execute(RegisterClassroomCommand command) {
    if (!schoolRepositoryPort.findById(command.schoolId()).isPresent()) {
      throw new SchoolNotFoundException("Escola não encontrada");
    }
    Classroom classroom = new Classroom(null, command.schoolId(), command.name());
    return classroomRepositoryPort.save(classroom);
  }
}
