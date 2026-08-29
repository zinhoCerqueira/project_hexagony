package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.FetchSchoolUseCase;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;
import java.util.UUID;

public class FetchSchoolService implements FetchSchoolUseCase {

  private final SchoolRepositoryPort schoolRepositoryPort;

  public FetchSchoolService(SchoolRepositoryPort schoolRepositoryPort) {
    this.schoolRepositoryPort = schoolRepositoryPort;
  }

  @Override
  public School execute(UUID id) {
    return schoolRepositoryPort
        .findById(id)
        .orElseThrow(() -> new SchoolNotFoundException("Escola não encontrada"));
  }
}
