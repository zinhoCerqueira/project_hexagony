package com.schoolqueue.infrastructure.adapters.in.web.mapper;

import com.schoolqueue.domain.model.Parent;
import com.schoolqueue.domain.ports.in.RegisterParentUseCase.RegisterParentCommand;
import com.schoolqueue.domain.ports.in.UpdateParentUseCase.UpdateParentCommand;
import com.schoolqueue.infrastructure.adapters.in.web.dto.ParentResponse;
import com.schoolqueue.infrastructure.adapters.in.web.dto.RegisterParentRequest;
import com.schoolqueue.infrastructure.adapters.in.web.dto.UpdateParentRequest;
import java.util.UUID;

public final class ParentDtoMapper {

  private ParentDtoMapper() {}

  public static RegisterParentCommand toCommand(RegisterParentRequest request) {
    return new RegisterParentCommand(request.name(), request.phone());
  }

  public static UpdateParentCommand toCommand(UUID id, UpdateParentRequest request) {
    return new UpdateParentCommand(id, request.name(), request.phone());
  }

  public static ParentResponse toResponse(Parent parent) {
    return new ParentResponse(parent.id(), parent.name(), parent.phone());
  }
}
