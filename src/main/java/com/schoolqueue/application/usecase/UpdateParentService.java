package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.ParentNotFoundException;
import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.ports.in.UpdateParentUseCase;
import com.schoolqueue.domain.ports.in.UpdateParentUseCase.UpdateParentCommand;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;

public class UpdateParentService implements UpdateParentUseCase {

  private final ParentRepositoryPort parentRepositoryPort;

  public UpdateParentService(ParentRepositoryPort parentRepositoryPort) {
    this.parentRepositoryPort = parentRepositoryPort;
  }

  @Override
  public Parent execute(UpdateParentCommand command) {
    Parent parent =
        parentRepositoryPort
            .findById(command.id())
            .orElseThrow(() -> new ParentNotFoundException("Responsável não encontrado"));
    parent.setName(command.name());
    parent.setPhone(command.phone());
    return parentRepositoryPort.save(parent);
  }
}
