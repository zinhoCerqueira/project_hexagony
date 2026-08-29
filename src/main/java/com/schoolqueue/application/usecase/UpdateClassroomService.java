package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.ClassroomNotFoundException;
import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.Classroom;
import com.schoolqueue.domain.ports.in.UpdateClassroomUseCase;
import com.schoolqueue.domain.ports.in.UpdateClassroomUseCase.UpdateClassroomCommand;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;

public class UpdateClassroomService implements UpdateClassroomUseCase {

  private final ClassroomRepositoryPort classroomRepositoryPort;
  private final SchoolRepositoryPort schoolRepositoryPort;

  public UpdateClassroomService(
      ClassroomRepositoryPort classroomRepositoryPort, SchoolRepositoryPort schoolRepositoryPort) {
    this.classroomRepositoryPort = classroomRepositoryPort;
    this.schoolRepositoryPort = schoolRepositoryPort;
  }

  @Override
  public Classroom execute(UpdateClassroomCommand command) {
    Classroom classroom =
        classroomRepositoryPort
            .findById(command.id())
            .orElseThrow(() -> new ClassroomNotFoundException("Turma não encontrada"));
    if (!schoolRepositoryPort.findById(command.schoolId()).isPresent()) {
      throw new SchoolNotFoundException("Escola não encontrada");
    }
    classroom.setSchoolId(command.schoolId());
    classroom.setName(command.name());
    return classroomRepositoryPort.save(classroom);
  }
}
