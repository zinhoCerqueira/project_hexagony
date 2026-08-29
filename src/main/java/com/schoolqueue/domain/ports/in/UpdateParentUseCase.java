package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.Parent;
import java.util.UUID;

public interface UpdateParentUseCase {

  Parent execute(UpdateParentCommand command);

  record UpdateParentCommand(UUID id, String name, String phone) {}
}
