package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.ports.in.RegisterParentUseCase;
import com.schoolqueue.domain.ports.in.RegisterParentUseCase.RegisterParentCommand;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;
import com.schoolqueue.domain.ports.out.ParentSchoolLinkRepositoryPort;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;

public class RegisterParentService implements RegisterParentUseCase {

  private final ParentRepositoryPort parentRepositoryPort;
  private final SchoolRepositoryPort schoolRepositoryPort;
  private final ParentSchoolLinkRepositoryPort parentSchoolLinkRepositoryPort;

  public RegisterParentService(
      ParentRepositoryPort parentRepositoryPort,
      SchoolRepositoryPort schoolRepositoryPort,
      ParentSchoolLinkRepositoryPort parentSchoolLinkRepositoryPort) {
    this.parentRepositoryPort = parentRepositoryPort;
    this.schoolRepositoryPort = schoolRepositoryPort;
    this.parentSchoolLinkRepositoryPort = parentSchoolLinkRepositoryPort;
  }

  @Override
  public Parent execute(RegisterParentCommand command) {
    if (!schoolRepositoryPort.findById(command.schoolId()).isPresent()) {
      throw new SchoolNotFoundException("Escola não encontrada");
    }
    Parent parent = new Parent(null, command.name(), command.phone(), command.email());
    Parent saved = parentRepositoryPort.save(parent);
    parentSchoolLinkRepositoryPort.linkSchool(saved.id(), command.schoolId());
    return saved;
  }
}
