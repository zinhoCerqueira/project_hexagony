package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.UpdateSchoolUseCase;
import com.schoolqueue.domain.ports.in.UpdateSchoolUseCase.UpdateSchoolCommand;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;

public class UpdateSchoolService implements UpdateSchoolUseCase {

  private final SchoolRepositoryPort schoolRepositoryPort;

  public UpdateSchoolService(SchoolRepositoryPort schoolRepositoryPort) {
    this.schoolRepositoryPort = schoolRepositoryPort;
  }

  @Override
  public School execute(UpdateSchoolCommand command) {
    School school =
        schoolRepositoryPort
            .findById(command.id())
            .orElseThrow(() -> new SchoolNotFoundException("Escola não encontrada"));
    school.setName(command.name());
    school.setLatitude(command.latitude());
    school.setLongitude(command.longitude());
    return schoolRepositoryPort.save(school);
  }
}
