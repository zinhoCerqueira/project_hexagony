package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.Parent;
import java.util.UUID;

public interface RegisterParentUseCase {

  Parent execute(RegisterParentCommand command);

  record RegisterParentCommand(String name, String phone, String email, UUID schoolId) {}
}
