package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.ports.in.ListParentsUseCase;
import com.schoolqueue.domain.ports.out.ParentRepositoryPort;
import java.util.List;

public class ListParentsService implements ListParentsUseCase {

  private final ParentRepositoryPort parentRepositoryPort;

  public ListParentsService(ParentRepositoryPort parentRepositoryPort) {
    this.parentRepositoryPort = parentRepositoryPort;
  }

  @Override
  public List<Parent> execute() {
    return parentRepositoryPort.findAll();
  }
}
