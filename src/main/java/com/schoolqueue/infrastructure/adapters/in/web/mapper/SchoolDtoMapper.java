package com.schoolqueue.infrastructure.adapters.in.web.mapper;

import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.RegisterSchoolUseCase.RegisterSchoolCommand;
import com.schoolqueue.infrastructure.adapters.in.web.dto.RegisterSchoolRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.SchoolResponse;

public final class SchoolDtoMapper {

  private SchoolDtoMapper() {}

  public static RegisterSchoolCommand toCommand(RegisterSchoolRequest request) {
    return new RegisterSchoolCommand(request.name(), request.latitude(), request.longitude());
  }

  public static SchoolResponse toResponse(School school) {
    return new SchoolResponse(school.id(), school.name(), school.latitude(), school.longitude());
  }
}
