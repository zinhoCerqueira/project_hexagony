package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.Parent;

public interface RegisterParentUseCase {

  Parent execute(RegisterParentCommand command);

  record RegisterParentCommand(String name, String phone) {}
}
