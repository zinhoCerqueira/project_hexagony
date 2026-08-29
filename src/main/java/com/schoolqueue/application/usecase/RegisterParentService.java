package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.ports.in.RegisterParentUseCase;
import com.schoolqueue.domain.ports.in.RegisterParentUseCase.RegisterParentCommand;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;

public class RegisterParentService implements RegisterParentUseCase {

  private final ParentRepositoryPort parentRepositoryPort;

  public RegisterParentService(ParentRepositoryPort parentRepositoryPort) {
    this.parentRepositoryPort = parentRepositoryPort;
  }

  @Override
  public Parent execute(RegisterParentCommand command) {
    Parent parent = new Parent(null, command.name(), command.phone());
    return parentRepositoryPort.save(parent);
  }
}
