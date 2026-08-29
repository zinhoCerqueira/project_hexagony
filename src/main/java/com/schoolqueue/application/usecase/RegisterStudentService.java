package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.ClassroomNotFoundException;
import com.schoolqueue.domain.exception.ParentNotFoundException;
import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.Student;
import com.schoolqueue.domain.ports.in.RegisterStudentUseCase;
import com.schoolqueue.domain.ports.in.RegisterStudentUseCase.RegisterStudentCommand;
import com.schoolqueue.domain.ports.out.ClassroomRepositoryPort;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;
import com.schoolqueue.domain.ports.out.ParentStudentLinkRepositoryPort;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import com.schoolqueue.domain.ports.out.StudentRepositoryPort;

public class RegisterStudentService implements RegisterStudentUseCase {

  private final StudentRepositoryPort studentRepositoryPort;
  private final SchoolRepositoryPort schoolRepositoryPort;
  private final ClassroomRepositoryPort classroomRepositoryPort;
  private final ParentRepositoryPort parentRepositoryPort;
  private final ParentStudentLinkRepositoryPort parentStudentLinkRepositoryPort;

  public RegisterStudentService(
      StudentRepositoryPort studentRepositoryPort,
      SchoolRepositoryPort schoolRepositoryPort,
      ClassroomRepositoryPort classroomRepositoryPort,
      ParentRepositoryPort parentRepositoryPort,
      ParentStudentLinkRepositoryPort parentStudentLinkRepositoryPort) {
    this.studentRepositoryPort = studentRepositoryPort;
    this.schoolRepositoryPort = schoolRepositoryPort;
    this.classroomRepositoryPort = classroomRepositoryPort;
    this.parentRepositoryPort = parentRepositoryPort;
    this.parentStudentLinkRepositoryPort = parentStudentLinkRepositoryPort;
  }

  @Override
  public Student execute(RegisterStudentCommand command) {
    if (!schoolRepositoryPort.findById(command.schoolId()).isPresent()) {
      throw new SchoolNotFoundException("Escola não encontrada");
    }
    if (!classroomRepositoryPort.findById(command.classroomId()).isPresent()) {
      throw new ClassroomNotFoundException("Turma não encontrada");
    }
    for (var parentId : command.parentIds()) {
      if (!parentRepositoryPort.findById(parentId).isPresent()) {
        throw new ParentNotFoundException("Responsável não encontrado");
      }
    }

    Student student =
        new Student(null, command.schoolId(), command.classroomId(), command.name());
    Student saved = studentRepositoryPort.save(student);
    parentStudentLinkRepositoryPort.replaceParentsOfStudent(saved.id(), command.parentIds());
    return saved;
  }
}
