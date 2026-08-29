package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.ListSchoolsUseCase;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import java.util.List;

public class ListSchoolsService implements ListSchoolsUseCase {

  private final SchoolRepositoryPort schoolRepositoryPort;

  public ListSchoolsService(SchoolRepositoryPort schoolRepositoryPort) {
    this.schoolRepositoryPort = schoolRepositoryPort;
  }

  @Override
  public List<School> execute() {
    return schoolRepositoryPort.findAll();
  }
}
