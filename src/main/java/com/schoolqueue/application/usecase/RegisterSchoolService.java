package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;

public class RegisterSchoolService implements RegisterSchoolUseCase {

  private final SchoolRepositoryPort schoolRepositoryPort;

  public RegisterSchoolService(SchoolRepositoryPort schoolRepositoryPort) {
    this.schoolRepositoryPort = schoolRepositoryPort;
  }

  @Override
  public School execute(RegisterSchoolCommand command) {
    School school = new School(null, command.name(), command.latitude(), command.longitude());
    return schoolRepositoryPort.save(school);
  }
}
