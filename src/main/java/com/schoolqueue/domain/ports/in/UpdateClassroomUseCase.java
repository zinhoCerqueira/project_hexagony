package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.Classroom;
import java.util.UUID;

public interface UpdateClassroomUseCase {

  Classroom execute(UpdateClassroomCommand command);

  record UpdateClassroomCommand(UUID id, UUID schoolId, String name) {}
}
