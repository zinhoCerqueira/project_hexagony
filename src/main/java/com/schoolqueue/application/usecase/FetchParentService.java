package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.ParentNotFoundException;
import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.ports.in.FetchParentUseCase;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;
import java.util.UUID;

public class FetchParentService implements FetchParentUseCase {

  private final ParentRepositoryPort parentRepositoryPort;

  public FetchParentService(ParentRepositoryPort parentRepositoryPort) {
    this.parentRepositoryPort = parentRepositoryPort;
  }

  @Override
  public Parent execute(UUID id) {
    return parentRepositoryPort
        .findById(id)
        .orElseThrow(() -> new ParentNotFoundException("Responsável não encontrado"));
  }
}
